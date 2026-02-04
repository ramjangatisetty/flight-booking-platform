# Agent Operating Rules (Read First)

You are operating in this repository to help generate API automation tests.
These rules are NON-NEGOTIABLE.

---

## 1) Governing Documents (Mandatory)
You MUST read and follow, in order:
1. TEST_GENERATION_BLUEPRINT.md
2. 00-prompt-registry.md
3. 00-security-guardrails.md
4. The active agent prompt (GenFramework / GenTests / SnapshotRefresh)
5. gentests.md (command router)

If instructions conflict:
- TEST_GENERATION_BLUEPRINT.md ALWAYS wins.
- 00-prompt-registry.md wins for model configuration conflicts
- 00-security-guardrails.md wins for security-related conflicts

---

## 2) Agent Scope Enforcement (Mandatory)

You MUST operate under exactly ONE agent at a time.

### GenFramework
Allowed:
- api-tests/build.gradle.kts
- api-tests/src/test/java/framework/**
- api-tests/src/test/resources/** (config, reporting)

Forbidden:
- service-specific tests
- OpenAPI snapshot updates
- production code

---

### GenTests
Allowed:
- api-tests/src/test/java/tests/**/$SERVICE/**
- api-tests/src/test/resources/testdata/$SERVICE/** (if applicable)

Forbidden:
- modifying framework code
- modifying OpenAPI snapshots
- modifying WSDL snapshots
- modifying production code
- modifying other services' tests

Service Type Rules:
- JSON REST services (booking, inventory, payment): Use ApiClient
- XML REST services (baggage): Use XmlApiClient, Content-Type: application/xml
- SOAP services (loyalty): Use SoapClient for SOAP operations, ApiClient for REST admin endpoints

---

### SnapshotRefresh
Allowed:
- api-tests/src/test/resources/openapi-snapshots/** (for REST services)
- api-tests/src/test/resources/wsdl-snapshots/** (for SOAP services)

Forbidden:
- generating tests
- modifying framework code
- modifying production code

---

If you detect a task outside your current agent scope:
- STOP immediately
- Report the violation
- Do NOT attempt a workaround

---

## 3) Contract Discovery Priority (Mandatory)

You MUST discover API contracts in this order based on service type:

### JSON REST Services (booking, inventory, payment)
1. OpenAPI snapshot (if present)
2. Runtime /api-docs (ONLY if explicitly allowed)
3. Controller + DTO code (fallback)

### XML REST Services (baggage)
1. OpenAPI snapshot (if present)
2. XSD schema from DTOs
3. Controller + DTO code (fallback)

### SOAP Services (loyalty)
1. WSDL/XSD snapshot (if present)
2. Service WSDL/XSD files
3. Endpoint + DTO code (fallback)

You MUST always report:
- Contract source used: snapshot | runtime | code | wsdl
- Service type: JSON REST | XML REST | SOAP

---

## 4) Discovery First, Generation Second
- Discovery tasks MUST NOT generate code
- Generation tasks MUST be based on completed discovery

---

## 5) Mode Enforcement (Mandatory)

If operating under GenTests:

MODE=audit
- Read-only
- NO file changes
- NO diffs
- Output report only

MODE=delta
- Add missing tests ONLY
- Do NOT refactor or rewrite existing tests

MODE=full
- Refactor allowed
- Scope strictly limited to the target service

If MODE is not specified, assume MODE=delta.

---

## 6) Network & Command Rules

- You MUST NOT run:
    - gradle
    - mvn
    - curl
    - docker
    - tests
- READ-ONLY file inspection is allowed:
    - open/read files
    - list directories
    - search/grep

Runtime network access (e.g. /api-docs) is allowed ONLY when:
- explicitly stated in the active prompt
- AND guarded by an explicit flag (e.g. ALLOW_NETWORK=true)

---

## 7) Code Location Rules

- ALL generated code must live under api-tests/
- You MUST NOT:
    - import *.domain.*
    - import *.application.*
    - perform DB validations
    - perform message-broker validations

---

## 8) Output Rules

Your output MUST include:
1. Agent used
2. Mode used (if applicable)
3. Contract source used
4. Service type (JSON REST | XML REST | SOAP)
5. List of files created/modified
6. Diffs/patches OR full file contents (as requested)

No extra prose unless explicitly requested.

## 9) Security & Compliance (Mandatory)

Before generating any code:
- You MUST scan for secrets using patterns from 00-security-guardrails.md
- You MUST validate no blocked patterns in output
- You MUST NOT generate code that:
    - Contains hardcoded passwords, API keys, or tokens
    - Imports from *.domain.* or *.application.*
    - Uses System.exit(), Runtime.exec(), or similar
    - Accesses databases or message brokers directly

If security violations detected:
- STOP generation immediately
- Report violation in output
- Do NOT proceed until resolved
