# Prompt Registry (Version Control for AI Agents)

> **Supreme Authority**: TEST_GENERATION_BLUEPRINT.md  
> **Last Updated**: 2026-02-03  
> **Owner**: QA Platform Team  

---

## 1. Active Prompt Versions

| Prompt ID | File | Version | Status | Last Verified |
|-----------|------|---------|--------|---------------|
| AGENT-RULES | 00-agent-operating-rules.md | 2.0.0 | Active | 2024-01-20 |
| BLUEPRINT | TEST_GENERATION_BLUEPRINT.md | 2.1.0 | Active | 2026-02-03 |
| GHERKIN | GHERKIN_STYLE_GUIDE.md | 2.0.0 | Active | 2024-01-20 |
| GEN-FRAME | genframework.md | 2.0.0 | Active | 2024-01-20 |
| GEN-TESTS | gentests.md | 2.1.0 | Active | 2026-02-03 |
| GEN-STEPS | gensteps.md | 1.0.0 | Active | 2024-01-20 |
| DISCOVERY | 01-discovery.md | 2.1.0 | Active | 2026-02-03 |
| MODULE-CREATE | 02-create-api-tests-module.md | 2.0.0 | Active | 2024-01-20 |
| FRAMEWORK | 03-generate-framework-skeleton.md | 2.0.0 | Active | 2024-01-20 |
| BOOKING-TESTS | 05-generate-booking-tests.md | 2.1.0 | Active | 2026-02-03 |
| INVENTORY-TESTS | 07-generate-inventory-tests.md | 2.0.0 | Active | 2024-01-20 |
| TESTKIT | 08-local-testkit-tests.md | 2.0.0 | Active | 2024-01-20 |
| SNAPSHOT | 09-snapshot-refresh.md | 2.0.0 | Active | 2024-01-20 |
| BAGGAGE-TESTS | 10-generate-baggage-tests.md | 1.0.0 | Active | 2024-01-20 |
| LOYALTY-TESTS | 11-generate-loyalty-tests.md | 1.0.0 | Active | 2024-01-20 |
| E2E-SAGA | 12-generate-e2e-saga-tests.md | 2.1.0 | Active | 2026-02-03 |
| KAFKA-EVENTS | 13-generate-kafka-event-tests.md | 2.0.0 | Active | 2026-02-03 |

---

## 2. Model Configuration

### Amazon Bedrock with Claude Sonnet (RECOMMENDED)

```yaml
# Amazon Bedrock Configuration
provider: amazon-bedrock
region: us-east-1  # or your preferred region
model_id: anthropic.claude-3-sonnet-20240229-v1:0
# Alternative models:
#   anthropic.claude-3-5-sonnet-20241022-v2:0  (Claude 3.5 Sonnet v2)
#   anthropic.claude-3-haiku-20240307-v1:0    (faster, cheaper)
#   anthropic.claude-3-opus-20240229-v1:0     (most capable)

# Inference Parameters
inference_config:
  max_tokens: 4096           # Maximum output tokens
  temperature: 0.2           # Low creativity, high consistency
  top_p: 0.95               # Nucleus sampling
  stop_sequences: []         # Optional stop sequences

# Bedrock-specific settings
bedrock_config:
  guardrail_id: null         # Optional: Bedrock Guardrails ID
  guardrail_version: null    # Optional: Guardrails version
  trace: DISABLED            # ENABLED for debugging
```

### AWS Credentials Setup

```bash
# Option 1: Environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1

# Option 2: AWS CLI profile
aws configure --profile bedrock-agent
export AWS_PROFILE=bedrock-agent

# Option 3: IAM Role (recommended for EC2/Lambda)
# Attach AmazonBedrockFullAccess policy to your role
```

### Required IAM Permissions

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": [
        "arn:aws:bedrock:*::foundation-model/anthropic.claude-3-sonnet*",
        "arn:aws:bedrock:*::foundation-model/anthropic.claude-3-5-sonnet*"
      ]
    }
  ]
}
```

### Alternative: OpenAI GPT-4

```yaml
# OpenAI Configuration (legacy)
provider: openai
model: gpt-4-1106-preview
temperature: 0.2
top_p: 0.95
seed: 42
max_tokens: 4000
frequency_penalty: 0.0
presence_penalty: 0.0
```

---

## 3. Claude-Specific Prompt Optimization

### System Prompt Template

When using Claude via Bedrock, structure your prompts as follows:

```
<system>
You are an API test automation agent. Follow these rules strictly:
1. Read and follow TEST_GENERATION_BLUEPRINT.md
2. Read and follow 00-agent-operating-rules.md
3. Output ONLY diffs unless asked otherwise
4. Do NOT modify production code
5. Use TestNG (not JUnit)
</system>

<context>
Service: {service_name}
Service Type: {JSON REST | XML REST | SOAP}
Mode: {audit | delta | full}
Contract Source: {snapshot | runtime | code | wsdl}
</context>

<task>
{specific task instructions from the prompt file}
</task>
```

### Claude Best Practices

1. **Use XML tags for structure**: Claude responds well to XML-tagged sections
2. **Be explicit about output format**: Specify "Output ONLY diffs" or "Output JSON"
3. **Provide examples**: Claude learns well from examples in prompts
4. **Use thinking tags for complex tasks**: `<thinking>` for reasoning steps
5. **Avoid ambiguity**: Claude follows instructions literally

### Token Limits by Model

| Model | Input Tokens | Output Tokens | Context Window |
|-------|-------------|---------------|----------------|
| Claude 3.5 Sonnet v2 | 200K | 8K | 200K |
| Claude 3 Sonnet | 200K | 4K | 200K |
| Claude 3 Haiku | 200K | 4K | 200K |
| Claude 3 Opus | 200K | 4K | 200K |

---

## 4. Kiro Agent Integration

### Agent Directory Structure

Place agent prompts in `.kiro/agents/` for Kiro to discover and execute them:

```
.kiro/
├── agents/
│   ├── api-test-discovery.md
│   ├── api-test-framework.md
│   ├── api-test-booking.md
│   ├── api-test-inventory.md
│   ├── api-test-baggage.md
│   ├── api-test-loyalty.md
│   └── api-test-snapshot.md
└── specs/
    └── api-test-automation-framework/
        ├── requirements.md
        ├── design.md
        └── tasks.md
```

### Agent Prompt Template

Each agent file should follow this structure:

```markdown
# Agent Name

## Context
- Service: {service_name}
- Service Type: {JSON REST | XML REST | SOAP}
- Mode: {audit | delta | full}

## Governing Documents
Follow these documents strictly:
1. TEST_GENERATION_BLUEPRINT.md
2. 00-agent-operating-rules.md

## Task
{specific task instructions}

## Output Format
{expected output format}
```

### Prompt Files Location

The detailed prompts remain in `prompts/` directory as reference documentation.
Kiro agents in `.kiro/agents/` can reference these prompts or include them inline.

---

## 5. Quality Gates (Must Pass Before Merge)

### Automated Gates (CI/CD Enforced)

| Gate | Tool | Threshold | Action on Failure |
|------|------|-----------|-------------------|
| Syntax Validation | `javac` | 100% compilation | Block PR |
| Contract Alignment | `openapi-diff` | 100% endpoint match | Block PR |
| Security Scan | `trufflehog` | 0 secrets found | Block PR |
| Unit Tests | `gradle test` | >90% pass rate | Block PR |
| Flakiness Check | `rerun tests 3x` | <5% variance | Block PR |
| Checkstyle | `checkstyle` | 0 violations | Warn |

### Manual Gates (Human Required)

- [ ] Business logic correctness review
- [ ] Assertion appropriateness (not just 200 OK checks)
- [ ] Data cleanup safety verification
- [ ] Correlation-ID validation present

---

## 6. Service-Specific Configurations

| Service | Type | Base URL Env Var | Special Rules |
|---------|------|-----------------|---------------|
| booking-service | JSON REST | BASE_URL_BOOKING | Idempotency-Key required for POST |
| inventory-service | JSON REST | BASE_URL_INVENTORY | High throughput, add rate-limit checks |
| payment-service | JSON REST | BASE_URL_PAYMENT | Standard |
| baggage-service | XML REST | BASE_URL_BAGGAGE | XML namespace required |
| loyalty-service | SOAP | BASE_URL_LOYALTY | SOAPAction headers required |

---

## 7. Change Log

### v2.1.0 (2026-02-03)
- **CRITICAL**: Added mandatory cross-service integration discovery phase
- Updated TEST_GENERATION_BLUEPRINT.md with service interaction map
- Updated 01-discovery.md with cross-service integration discovery steps
- Updated gentests.md with integration test category
- Updated 05-generate-booking-tests.md with Loyalty and Baggage integration requirements
- Updated 12-generate-e2e-saga-tests.md with complete integration test scenarios
- Added new E2E tests: LoyaltyBookingIntegrationTest, BaggageBookingIntegrationTest
- Key lesson: Optional fields in DTOs often represent integration points - DO NOT IGNORE THEM

### v2.0.0 (2024-01-20)
- Added Amazon Bedrock with Claude Sonnet support
- Added XML REST support (Baggage Service)
- Added SOAP support (Loyalty Service)
- Updated all prompts for multi-service-type support
- Added new prompts: 10-generate-baggage-tests.md, 11-generate-loyalty-tests.md
- Updated discovery, framework, and test generation prompts

### v1.0.0 (2024-01-10)
- Initial production release
- JSON REST services only
- OpenAI GPT-4 configuration

---

## 8. Baseline Metrics (v2.0.0)

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| Generation Success Rate | 97% | >95% | OK |
| Compilation Rate | 98% | 100% | WARNING |
| First-Run Pass Rate | 94% | >90% | OK |
| Flakiness Rate | 2.1% | <3% | OK |
| Coverage Improvement | +12% | +10% | OK |
| Cost per 1000 Tests (Bedrock) | $4.20 | <$10 | OK |
| False Positive Rate | 1.8% | <2% | OK |

---

## 9. Troubleshooting

### Bedrock-Specific Issues

**Error: AccessDeniedException**
- Check IAM permissions for bedrock:InvokeModel
- Verify model access is enabled in Bedrock console
- Check region matches your configuration

**Error: ThrottlingException**
- Implement exponential backoff
- Request quota increase via AWS Support
- Consider using Claude 3 Haiku for high-volume tasks

**Error: ModelTimeoutException**
- Reduce max_tokens
- Split large prompts into smaller chunks
- Use streaming for long responses

### Generated code doesn't compile
**Check**: Temperature (should be 0.2), prompt structure  
**Action**: Re-run with MODE=audit, compare outputs

### Tests are flaky
**Check**: Correlation-ID handling, test data isolation  
**Action**: Review genframework.md for request/response cleanup

### Hallucinated endpoints
**Check**: OpenAPI/WSDL snapshot freshness  
**Action**: Run 09-snapshot-refresh.md to update contracts

---

## 10. Contact & Escalation

| Issue Type | Contact | SLA |
|------------|---------|-----|
| Prompt quality degradation | @qa-architect | 4 hours |
| Security concerns | @security-team | 1 hour |
| CI/CD pipeline failures | @devops-team | 2 hours |
| Bedrock API issues | @platform-team | 8 hours |

---

*This file is auto-checked by CI/CD. Last validation: 2024-01-20*
