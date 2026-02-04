# CI/CD Integration for AI Test Generation

> **Applies To**: GitHub Actions, GitLab CI, Jenkins  
> **Last Updated**: 2026-01-20  
> **Owner**: DevOps + QA Platform Team  

---

## 1. Overview

This document defines the CI/CD pipeline for AI-generated test automation.  
**Goal**: Automated, safe, auditable test generation integrated into the development lifecycle.

### Pipeline Philosophy

| Principle | Implementation |
|-----------|----------------|
| Safety First | All generated code validated before merge |
| Human Oversight | MODE=full requires approval, MODE=delta auto-merges if safe |
| Auditability | All generations logged, versioned, and traceable |
| Rollback Ready | Instant revert to last known good state |

---

## 2. GitHub Actions Workflow

### 2.1 Main Pipeline: AI Test Generation

File: `.github/workflows/ai-test-generation.yml`

```yaml
name: AI Test Generation Pipeline

on:
  workflow_dispatch:
    inputs:
      service:
        description: 'Target service'
        required: true
        type: choice
        options:
          - flight-service
          - booking-service
          - passenger-service
          - inventory-service
      mode:
        description: 'Generation mode'
        required: true
        default: 'delta'
        type: choice
        options:
          - audit
          - delta
          - full
      allow_network:
        description: 'Allow runtime /api-docs fetch'
        required: false
        default: false
        type: boolean

env:
  JAVA_VERSION: '21'
  JAVA_DISTRIBUTION: 'temurin'
  GRADLE_VERSION: '8.5'

jobs:
  validate:
    name: Validate Inputs
    runs-on: ubuntu-latest
    outputs:
      should_generate: ${{ steps.check.outputs.should_generate }}
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Check Agent Scope
        id: check
        run: |
          echo "Service: ${{ github.event.inputs.service }}"
          echo "Mode: ${{ github.event.inputs.mode }}"

          if [[ ! "${{ github.event.inputs.service }}" =~ ^(flight-service|booking-service|passenger-service|inventory-service)$ ]]; then
            echo "Invalid service name"
            exit 1
          fi

          if [[ ! "${{ github.event.inputs.mode }}" =~ ^(audit|delta|full)$ ]]; then
            echo "Invalid mode"
            exit 1
          fi

          echo "should_generate=true" >> $GITHUB_OUTPUT

  generate:
    name: Generate Tests via AI Agent
    needs: validate
    if: needs.validate.outputs.should_generate == 'true'
    runs-on: ubuntu-latest
    environment: ai-generation
    permissions:
      contents: write
      pull-requests: write
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
        with:
          ref: main
          fetch-depth: 0

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: ${{ env.GRADLE_VERSION }}

      - name: Configure AI Agent
        env:
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
          OPENAI_MODEL: ${{ secrets.OPENAI_MODEL || 'gpt-4-1106-preview' }}
        run: |
          mkdir -p .agent-config
          cat > .agent-config/env <<EOF
          MODEL=$OPENAI_MODEL
          TEMPERATURE=0.2
          SEED=42
          MAX_TOKENS=4000
          EOF

      - name: Execute AI Agent
        env:
          SERVICE: ${{ github.event.inputs.service }}
          MODE: ${{ github.event.inputs.mode }}
          ALLOW_NETWORK: ${{ github.event.inputs.allow_network }}
          BASE_URL_FLIGHT: http://localhost:8080
          BASE_URL_BOOKING: http://localhost:8081
          BASE_URL_PASSENGER: http://localhost:8082
          BASE_URL_INVENTORY: http://localhost:8083
        run: |
          echo "Starting AI Agent: GenTests"
          ./scripts/run-agent.sh gentests $SERVICE $MODE

      - name: Upload Generation Artifacts
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: generation-logs-${{ github.run_id }}
          path: |
            .agent-logs/
            api-tests/src/test/java/tests/${{ github.event.inputs.service }}/
            !**/*.class

  validate-gates:
    name: Quality Gates
    needs: [validate, generate]
    if: needs.generate.result == 'success'
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java & Gradle
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Download Generated Code
        uses: actions/download-artifact@v4
        with:
          name: generation-logs-${{ github.run_id }}
          path: ./download

      - name: Gate 1 - Compilation Check
        run: |
          ./gradlew :api-tests:compileTestJava --no-daemon

      - name: Gate 2 - Security Scan (Secrets)
        uses: trufflesecurity/trufflehog@main
        with:
          path: ./api-tests/src/test/java
          base: main
          head: HEAD
          extra_args: --debug --only-verified

      - name: Gate 3 - Security Scan (Code Patterns)
        run: |
          FAIL=0
          if grep -r "System.exit" api-tests/src/test/java/; then FAIL=1; fi
          if grep -r "Runtime.getRuntime().exec" api-tests/src/test/java/; then FAIL=1; fi
          if grep -r "import.*\.domain\." api-tests/src/test/java/; then FAIL=1; fi
          if [ $FAIL -eq 1 ]; then exit 1; fi

      - name: Gate 4 - Unit Tests
        run: |
          ./gradlew :api-tests:test --tests "${{ github.event.inputs.service }}.*" --no-daemon

      - name: Gate 5 - Flakiness Check
        run: |
          RESULTS=()
          for i in 1 2 3; do
            ./gradlew :api-tests:test --tests "${{ github.event.inputs.service }}.*" --no-daemon
            RESULTS+=($?)
          done
          UNIQUE_RESULTS=$(echo "${RESULTS[@]}" | tr ' ' \n | sort -u | wc -l)
          if [ $UNIQUE_RESULTS -gt 1 ]; then echo "Flakiness detected"; fi

  create-pr:
    name: Create Pull Request
    needs: [validate, generate, validate-gates]
    if: github.event.inputs.mode != 'audit'
    runs-on: ubuntu-latest
    permissions:
      contents: write
      pull-requests: write
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Download Generated Code
        uses: actions/download-artifact@v4
        with:
          name: generation-logs-${{ github.run_id }}
          path: ./download

      - name: Apply Generated Changes
        run: |
          cp -r ./download/api-tests/src/test/java/tests/${{ github.event.inputs.service }}/*                 api-tests/src/test/java/tests/${{ github.event.inputs.service }}/

      - name: Create Pull Request
        uses: peter-evans/create-pull-request@v6
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
          commit-message: "[AI-Generated] Add tests for ${{ github.event.inputs.service }}"
          branch: ai-tests/${{ github.event.inputs.service }}-${{ github.run_id }}
          delete-branch: true
          title: "[AI-Generated] Tests for ${{ github.event.inputs.service }}"
          body: |
            ## AI Test Generation Report

            - **Service**: ${{ github.event.inputs.service }}
            - **Mode**: ${{ github.event.inputs.mode }}
            - **Agent**: GenTests
            - **Run ID**: ${{ github.run_id }}

            ### Validation Results
            - Compilation: Passed
            - Security Scan: Passed

            ### Manual Review Checklist
            - [ ] Business logic correctness verified
            - [ ] Assertions validate behavior
            - [ ] Data cleanup is safe
          labels: |
            ai-generated
            tests
            ${{ github.event.inputs.service }}
```

---

## 3. Quality Gates Reference

| Gate | Command | Threshold | On Failure |
|------|---------|-----------|------------|
| Compilation | `javac` | 100% | Block PR |
| Secrets | `trufflehog` | 0 findings | Block PR + Alert |
| Security Patterns | Custom regex | 0 matches | Block PR |
| Unit Tests | `gradle test` | >90% pass | Warn |
| Flakiness | Rerun 3x | <5% variance | Warn + Flag |

---

## 4. Rollback Procedures

### Automated Rollback
```yaml
- name: Auto-Rollback on Critical Failure
  if: failure() && github.event.inputs.mode == 'delta'
  run: |
    git revert HEAD --no-edit
    git push origin HEAD --force
```

### Manual Rollback
```bash
git revert <merge-commit-hash>
```

---

## 5. Monitoring & Alerting

| Metric | Collection Method | Alert Threshold |
|--------|-------------------|-----------------|
| Generation Success Rate | GitHub Actions API | <95% |
| Compilation Rate | Build logs | <100% |
| LLM API Cost | OpenAI dashboard | >$50/day |

---

*This pipeline ensures safe, auditable, automated test generation.*
