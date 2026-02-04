# Flight Booking Platform - Internal Team Presentation Guide

**Date**: February 4, 2026  
**Purpose**: Internal review of AI-generated test automation implementation using Kiro

---

## Executive Summary

This document provides a step-by-step guide for presenting the Flight Booking Platform to your internal team, highlighting what has been implemented and which components were automatically generated using **Kiro's AI-powered test automation capabilities**.

### Key Kiro Features Demonstrated
- **Steering Files**: Project-wide rules that guide all AI interactions
- **Agents**: Specialized AI prompts for specific tasks (discovery, framework, tests)
- **Hooks**: User-triggered automation that invokes agents with one click
- **Specs**: Structured requirements → design → tasks workflow

---

## 1. Platform Overview

### What We Built
An event-driven microservices platform for airline seat reservations with:
- **5 Backend Services** (Java 17, Spring Boot 3.3.2)
- **1 Frontend Application** (React 18, TypeScript, Material-UI v7)
- **1 API Test Automation Framework** (TestNG, RestAssured, SOAP support)

### Architecture Highlights
```
┌─────────────────────────────────────────────────────────────────────┐
│                        BOOKING SERVICE (8081)                        │
│  Orchestrates the booking saga via Kafka events                      │
└─────────────────────────────────────────────────────────────────────┘
         │                    │                      │
         ▼                    ▼                      ▼
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  INVENTORY  │    │     PAYMENT     │    │    BAGGAGE      │
│  (8082)     │    │     (8083)      │    │    (8084)       │
│  JSON REST  │    │    JSON REST    │    │    XML REST     │
└─────────────┘    └─────────────────┘    └─────────────────┘
                                                  │
                                                  ▼
                                         ┌─────────────────┐
                                         │    LOYALTY      │
                                         │    (8085)       │
                                         │    SOAP/WSDL    │
                                         └─────────────────┘
```

---

## 2. Kiro Implementation Deep Dive

### 2.1 Project Structure for Kiro

```
.kiro/
├── steering/                    # Always-included project rules
│   ├── product.md              # Domain knowledge (services, events, saga)
│   ├── structure.md            # Architecture rules (hexagonal, packages)
│   └── tech.md                 # Technology stack (Spring Boot, Kafka, ports)
│
├── agents/                      # Specialized AI agent prompts
│   ├── api-test-discovery.md   # Discovers API surface (read-only)
│   ├── api-test-framework.md   # Generates framework skeleton
│   ├── api-test-generate.md    # Generates service-specific tests
│   ├── api-test-audit.md       # Audits test coverage
│   ├── api-test-snapshot.md    # Refreshes OpenAPI/WSDL snapshots
│   ├── gen-e2e-integration-tests.md  # Cross-service E2E tests
│   └── gen-kafka-event-tests.md      # Kafka event tests
│
├── hooks/                       # One-click automation triggers
│   ├── api-test-discovery.kiro.hook
│   ├── api-test-framework.kiro.hook
│   ├── gen-e2e-integration.kiro.hook
│   ├── gen-tests-booking.kiro.hook
│   ├── gen-tests-inventory.kiro.hook
│   ├── gen-tests-baggage.kiro.hook
│   ├── gen-tests-loyalty.kiro.hook
│   └── gen-kafka-event-tests.kiro.hook
│
└── specs/                       # Structured feature specifications
    ├── api-test-automation-framework/
    │   ├── requirements.md     # 20 formal requirements
    │   └── design.md           # Architecture, interfaces, properties
    └── baggage-service/
        ├── requirements.md
        ├── design.md
        └── tasks.md
```

### 2.2 Steering Files (Always-Included Context)

Steering files provide **persistent context** that Kiro includes in every AI interaction. This ensures consistency across all generated code.

#### `.kiro/steering/product.md` - Domain Knowledge
```markdown
# What it contains:
- Service responsibilities (Booking, Inventory, Payment, Baggage, Loyalty)
- Event types and Kafka topics
- Saga orchestration pattern
- EventEnvelope structure (meta.eventId, meta.correlationId, data)
- State machine rules (PENDING_PAYMENT → CONFIRMED/REJECTED)
```

**Why it matters**: AI knows the exact event structure before generating tests:
```json
{
  "meta": {
    "eventId": "uuid",
    "eventType": "booking.confirmed",
    "correlationId": "uuid"
  },
  "data": { ... }
}
```

#### `.kiro/steering/structure.md` - Architecture Rules
```markdown
# What it contains:
- Hexagonal architecture layers (api → application → domain ← infrastructure)
- Package naming conventions
- Class naming patterns (BookingController, BookingAppService, BookingEntity)
- Anti-patterns to avoid
```

**Why it matters**: AI generates code that follows project conventions automatically.

#### `.kiro/steering/tech.md` - Technology Stack
```markdown
# What it contains:
- Port assignments (Booking=8081, Inventory=8082, etc.)
- Dependency versions (Spring Boot 3.3.2, RestAssured 5.5.0)
- Build commands
- Database migration rules
```

**Why it matters**: AI uses correct ports and dependencies without being told.

### 2.3 Agents (Specialized AI Prompts)

Agents are **task-specific prompts** stored in `.kiro/agents/`. Each agent has a single responsibility.

#### Agent: `api-test-discovery.md`
**Purpose**: Read-only API surface discovery
**Output**: Structured inventory of controllers, endpoints, DTOs
**Key Rule**: "Do NOT generate code. Do NOT modify files."

```markdown
# Example output from discovery agent:
## booking-service (JSON REST)
Contract source: code
Port: 8081

### Endpoints
| Method | Path | Request | Response | Headers |
|--------|------|---------|----------|---------|
| POST | /bookings | CreateBookingRequest | BookingResponse | Idempotency-Key |
```

#### Agent: `api-test-framework.md`
**Purpose**: Generate shared test framework
**Scope**: `api-tests/src/test/java/framework/**`
**Creates**: ApiClient, SoapClient, XmlApiClient, asserters, utilities

#### Agent: `api-test-generate.md`
**Purpose**: Generate service-specific tests
**Parameters**: SERVICE (booking/inventory/baggage/loyalty), MODE (delta/full)
**Modes**:
- `delta`: Add missing tests only (default)
- `full`: Refactoring allowed

#### Agent: `gen-e2e-integration-tests.md`
**Purpose**: Generate cross-service E2E tests
**Key Feature**: Mandatory discovery phase before generating tests

```markdown
# MANDATORY DISCOVERY PHASE (from agent prompt):
1. Scan DTOs for foreign key references (memberId, bagTag)
2. Read event listeners in services/*/messaging/consumer/
3. Read event publishers in services/*/messaging/producer/
4. Map complete flows before generating tests
```

### 2.4 Hooks (Event-Driven Automation)

Hooks are **event-triggered automations** that run automatically when something happens in the IDE. Unlike agents (which you invoke manually), hooks react to file changes, saves, and other events.

#### Hook Structure
```json
{
  "name": "Audit Tests on Controller Change",
  "version": "1.0.0",
  "when": {
    "type": "fileEdited",                                    // ← Event trigger
    "patterns": ["services/*/src/main/java/**/controller/*.java"]
  },
  "then": {
    "type": "askAgent",
    "prompt": "A controller file was just modified. Please audit..."
  }
}
```

#### Event Types for Hooks

| Event Type | When It Fires | Use Case |
|------------|---------------|----------|
| `fileEdited` | User saves a file | Audit coverage after API changes |
| `fileCreated` | New file created | Scaffold tests for new controllers |
| `fileDeleted` | File deleted | Check for orphaned tests |
| `promptSubmit` | User sends message | Pre-process or validate prompts |
| `agentStop` | Agent completes | Run follow-up tasks |
| `userTriggered` | Manual button click | On-demand tasks |

#### Automation Hooks (3 examples)

| Hook | Trigger | Action |
|------|---------|--------|
| `audit-on-controller-change` | Controller file saved | Audit test coverage for affected service |
| `audit-on-dto-change` | DTO file saved | Check if test models need updating |
| `lint-on-test-save` | Test file saved | Validate follows framework conventions |

#### Key Difference: Agents vs Hooks

| Aspect | Agents | Hooks |
|--------|--------|-------|
| **Invocation** | Manual (chat or command) | Automatic (event-driven) |
| **Purpose** | Generate/modify code | React to changes |
| **When to use** | "Generate tests for booking" | "When controller changes, audit" |
| **User action** | User asks Kiro | No user action needed |

### 2.5 How to Invoke Agents

Agents are invoked **manually through Kiro chat**. You simply ask Kiro to follow an agent's instructions:

#### Method 1: Reference the Agent File
```
Follow .kiro/agents/gen-e2e-integration-tests.md to generate E2E tests
```

#### Method 2: Describe the Task (Kiro finds the right agent)
```
Generate E2E integration tests for the booking and loyalty services
```

#### Method 3: Reference Prompt Files Directly
```
Follow prompts/12-generate-e2e-saga-tests.md to generate saga tests
```

### 2.6 Specs (Structured Feature Development)

Specs provide a **requirements → design → tasks** workflow for complex features.

#### Spec: `api-test-automation-framework`

**Location**: `.kiro/specs/api-test-automation-framework/`

**requirements.md** (20 formal requirements):
```markdown
### Requirement 1: Gradle Module Setup
THE Framework SHALL create an `api-tests` Gradle module...

### Requirement 5: XML REST Client Support
THE XmlApiClient SHALL set Content-Type header to "application/xml"...

### Requirement 6: SOAP Client Support
THE SoapClient interface SHALL define methods for each SOAP operation...
```

**design.md** (Architecture):
```markdown
## Components and Interfaces

### 1. Configuration Component
public class TestConfig {
    public String getBaseUrl(ServiceType service);
    public boolean isLocal();
}

### 4. SOAP Client
public interface SoapClient {
    SoapResponse sendRequest(String soapAction, String envelope);
}

## Correctness Properties
Property 1: Production Code Isolation
*For any* generated test file, scanning its imports SHALL NOT find 
any packages matching `*.domain.*` or `*.application.*`.
```

### 2.7 Prompt Files (Detailed Instructions)

The `prompts/` directory contains **detailed generation instructions** referenced by agents.

```
prompts/
├── TEST_GENERATION_BLUEPRINT.md    # Supreme authority (wins all conflicts)
├── 00-agent-operating-rules.md     # Operational constraints
├── 00-prompt-registry.md           # Version control for prompts
├── 00-security-guardrails.md       # Security policies
├── 01-discovery.md                 # Discovery process
├── 03-generate-framework-skeleton.md
├── 05-generate-booking-tests.md
├── 07-generate-inventory-tests.md
├── 10-generate-baggage-tests.md    # XML REST
├── 11-generate-loyalty-tests.md    # SOAP
├── 12-generate-e2e-saga-tests.md   # Cross-service
└── 13-generate-kafka-event-tests.md
```

#### Key Governance Rules (from TEST_GENERATION_BLUEPRINT.md)

```markdown
## Sources of Truth (Ordered)
1. OpenAPI Snapshots (Contract truth)
2. WSDL/XSD Snapshots (for SOAP)
3. Controller + DTO code (Fallback)

## Agent Model
- GenFramework: Creates framework only
- GenTests: Generates service tests
- SnapshotRefresh: Updates contracts

## Modes
- audit: Read-only, no file changes
- delta: Add missing tests only (default)
- full: Refactoring allowed

## Output Rules
Agents MUST output:
1. Agent name
2. Mode
3. Contract source used
4. Files created/modified
5. Diffs only (no extra prose)
```

---

## 3. Backend Services (Manually Implemented)

### Services Overview

| Service | Port | API Type | Key Responsibilities |
|---------|------|----------|---------------------|
| Booking | 8081 | JSON REST | Booking lifecycle, saga orchestration |
| Inventory | 8082 | JSON REST | Seat availability, reservations |
| Payment | 8083 | JSON REST | Payment processing simulation |
| Baggage | 8084 | XML REST | Baggage tracking, XML payloads |
| Loyalty | 8085 | SOAP | Member enrollment, points accrual |

### Key Implementation Patterns
- **Hexagonal Architecture**: api → application → domain ← infrastructure
- **Event-Driven Saga**: Choreography-based distributed transactions via Kafka
- **Multi-Protocol Support**: JSON REST, XML REST, and SOAP in one platform

---

## 4. Frontend Application (AI-Assisted Implementation)

### Implementation Status: ✅ Phase 0 & 1 Complete

**Location**: `frontend/`

### What Was Built
- Home page with hero section and flight search widget
- Form validation (origin, destination, dates, passengers)
- Mock results page (4 flights generated from search criteria)
- Responsive design with Material-UI v7 theming

### Key Files (27 total)
```
frontend/src/
├── components/
│   ├── home/FlightSearchWidget.tsx    # 350+ lines, full validation
│   ├── home/HeroSection.tsx           # Hero banner with background
│   └── layout/AppShell.tsx            # Main layout wrapper
├── pages/
│   ├── HomePage.tsx                   # Home page container
│   └── ResultsPage.tsx                # Mock search results
├── domain/
│   ├── enums.ts                       # TripType, CabinClass, Currency
│   └── types.ts                       # TypeScript interfaces
└── testing/
    └── testIds.ts                     # Centralized test IDs for automation
```

### Demo Points
1. Run `cd frontend && npm run dev` → Open http://localhost:5173
2. Show flight search form with validation
3. Demonstrate test ID strategy in `testIds.ts`

---

## 5. API Test Automation Framework (AI-Generated via Kiro)

### 🤖 This is the AI-Generated Component

**Location**: `api-tests/`

### How It Was Generated (Step-by-Step)

#### Step 1: Create Steering Files
First, we created steering files to give Kiro domain knowledge:
```bash
.kiro/steering/product.md   # Event types, saga flow, service ports
.kiro/steering/structure.md # Hexagonal architecture rules
.kiro/steering/tech.md      # Technology stack, dependencies
```

#### Step 2: Create Spec with Requirements
We created a formal spec with 20 requirements:
```bash
.kiro/specs/api-test-automation-framework/requirements.md
.kiro/specs/api-test-automation-framework/design.md
```

#### Step 3: Create Agents
We created specialized agents for each task:
```bash
.kiro/agents/api-test-discovery.md      # Read-only discovery
.kiro/agents/api-test-framework.md      # Framework generation
.kiro/agents/api-test-generate.md       # Test generation
.kiro/agents/gen-e2e-integration-tests.md  # E2E tests
```

#### Step 4: Create Hooks for One-Click Execution
We created hooks to trigger agents:
```bash
.kiro/hooks/api-test-framework.kiro.hook
.kiro/hooks/gen-tests-booking.kiro.hook
.kiro/hooks/gen-e2e-integration.kiro.hook
# ... 10 hooks total
```

#### Step 5: Run Hooks to Generate Code
User clicks hooks in Kiro's Agent Hooks panel:
1. **"Generate Test Framework"** → Creates framework skeleton
2. **"Generate Booking Tests"** → Creates booking smoke tests
3. **"Generate E2E Integration Tests"** → Creates cross-service tests

### Framework Architecture (Generated)

```
api-tests/src/test/java/
├── framework/                    # Shared test infrastructure
│   ├── clients/                  # ApiClient, RestAssuredApiClient
│   ├── soap/                     # SoapClient, SoapEnvelopeBuilder
│   ├── xml/                      # XmlApiClient, XML builders
│   ├── config/                   # TestConfig, ServiceType enum
│   ├── endpoints/                # Endpoint constants per service
│   ├── headers/                  # CorrelationId, IdempotencyKey support
│   ├── asserters/                # ErrorAsserter, SoapFaultAsserter
│   ├── models/                   # Request/Response POJOs
│   ├── reporting/                # ExtentReports integration
│   └── testkit/                  # LocalTestClient for test utilities
│
└── tests/                        # Generated test classes
    ├── smoke/                    # Service health checks
    │   ├── BookingOpenApiSmokeTest.java
    │   ├── InventoryOpenApiSmokeTest.java
    │   ├── PaymentOpenApiSmokeTest.java
    │   ├── BaggageHealthSmokeTest.java
    │   └── LoyaltyWsdlSmokeTest.java
    │
    └── e2e/                      # Cross-service integration tests
        ├── LoyaltyBookingIntegrationTest.java
        └── BaggageBookingIntegrationTest.java
```

### What Was AI-Generated

| Component | Description | Lines of Code |
|-----------|-------------|---------------|
| Framework Layer | 14 packages with clients, utilities, asserters | ~2,000+ |
| Smoke Tests | 5 service health check tests | ~200 |
| E2E Integration Tests | 2 cross-service saga tests | ~400 |
| Configuration | TestNG XML, build.gradle | ~50 |

### Key AI-Generated Test Examples

#### 1. Smoke Tests (Generated by `gen-tests-booking` hook)
```java
// BookingOpenApiSmokeTest.java
@Test(groups = "smoke")
public void apiDocsShouldReturn200() {
    var response = client.get(BookingEndpoints.API_DOCS, Collections.emptyMap());
    assertThat(response.getStatusCode()).isEqualTo(200);
}
```

#### 2. E2E Integration: Loyalty + Booking (Generated by `gen-e2e-integration` hook)
```java
// LoyaltyBookingIntegrationTest.java
@Test(groups = {"e2e", "loyalty"})
public void shouldAccrueLoyaltyPointsWhenBookingConfirmed() {
    // AI discovered this integration by:
    // 1. Finding memberId field in CreateBookingRequest
    // 2. Reading LoyaltySoapClient in booking-service
    // 3. Understanding the SOAP call flow
    
    // Step 1: Enroll member via SOAP
    String enrollEnvelope = LoyaltySoapRequestBuilder.enrollMember(firstName, lastName, email);
    SoapResponse enrollResponse = loyaltySoapClient.sendRequest(
            LoyaltyEndpoints.SOAP_ACTION_ENROLL, enrollEnvelope);
    String memberId = SoapResponseParser.extractElement(response, "memberId", String.class);
    
    // Step 2: Create booking WITH memberId
    CreateBookingRequest bookingRequest = new CreateBookingRequest(
            flightId, "ECONOMY", price, "USD", memberId);
    
    // Step 3: Poll for CONFIRMED status
    BookingResponse confirmedBooking = pollForTerminalStatus(bookingId, headers);
    
    // Step 4: Verify loyalty accrual
    assertThat(confirmedBooking.getLoyaltyAccrualStatus()).isEqualTo("SUCCEEDED");
    assertThat(confirmedBooking.getLoyaltyPoints()).isGreaterThan(0);
}
```

#### 3. E2E Integration: Baggage + Booking (Generated by `gen-e2e-integration` hook)
```java
// BaggageBookingIntegrationTest.java
@Test(groups = {"e2e", "baggage"})
public void shouldAutoCreateBaggageWhenBookingConfirmed() {
    // AI discovered this integration by:
    // 1. Finding bagTag field in BookingResponse
    // 2. Reading BookingEventsListener in baggage-service
    // 3. Understanding the event-driven flow
    
    // Step 1: Create booking
    // Step 2: Poll for CONFIRMED status
    // Step 3: Poll for bagTag population (async)
    BookingResponse bookingWithBagTag = pollForBagTag(bookingId, headers);
    assertThat(bookingWithBagTag.getBagTag()).isNotNull();
    
    // Step 4: Verify baggage trackable via Baggage Service
    String trackPath = BaggageEndpoints.TRACK.replace("{bagTag}", bagTag);
    Response trackResponse = baggageClient.get(trackPath, headers);
    assertThat(trackResponse.getStatusCode()).isEqualTo(200);
}
```

---

## 6. Kiro Workflow Demonstration

### Demo Script: Generate Tests from Scratch

#### Step 1: Show Steering Files
```bash
# Open .kiro/steering/ in Kiro
# Explain: "These files are ALWAYS included in AI context"
```

#### Step 2: Show Agent Hooks Panel
```
1. Open Kiro's Agent Hooks panel (sidebar)
2. Show the 10 available hooks
3. Explain: "Each hook triggers a specialized agent"
```

#### Step 3: Invoke Discovery Agent (via Chat)
```
In Kiro chat, type:
"Follow .kiro/agents/api-test-discovery.md to discover all APIs"

Watch AI analyze all 5 services and output structured inventory
```

#### Step 4: Invoke Framework Generation Agent (via Chat)
```
In Kiro chat, type:
"Follow .kiro/agents/api-test-framework.md to generate the test framework"

Watch AI create:
- api-tests/build.gradle
- framework/clients/ApiClient.java
- framework/soap/SoapClient.java
- framework/config/TestConfig.java
- ... 14 packages total
```

#### Step 5: Invoke E2E Integration Agent (via Chat)
```
In Kiro chat, type:
"Follow .kiro/agents/gen-e2e-integration-tests.md to generate E2E integration tests"

Watch AI:
- Discover memberId → Loyalty integration
- Discover bagTag → Baggage integration
- Generate LoyaltyBookingIntegrationTest.java
- Generate BaggageBookingIntegrationTest.java
```

#### Step 6: Run Generated Tests
```bash
export BASE_URL_BOOKING=http://localhost:8081
export BASE_URL_INVENTORY=http://localhost:8082
export BASE_URL_PAYMENT=http://localhost:8083
export BASE_URL_BAGGAGE=http://localhost:8084
export BASE_URL_LOYALTY=http://localhost:8085
export ENV=local

./gradlew :api-tests:test
```

---

## 7. Deep Dive: What Happens When You Invoke an Agent

This section explains the **exact execution flow** when you ask Kiro to follow the E2E Integration Tests agent.

### 7.1 Invoking the Agent

In Kiro chat, you type:
```
Follow .kiro/agents/gen-e2e-integration-tests.md to generate E2E integration tests
```

Kiro reads the agent file:

**File**: `.kiro/agents/gen-e2e-integration-tests.md`
```markdown
# Generate E2E Cross-Service Integration Tests

## Context
- Service Type: Cross-Service E2E
- Mode: delta
- Contract Source: DTOs + Event Listeners/Publishers + WSDL

## Governing Documents
Follow these documents strictly:
1. `prompts/TEST_GENERATION_BLUEPRINT.md`
2. `prompts/00-agent-operating-rules.md`
3. `prompts/12-generate-e2e-saga-tests.md`

## Task
Generate end-to-end cross-service integration tests...

## MANDATORY DISCOVERY PHASE (DO THIS FIRST)
Before generating ANY tests, you MUST:
1. Scan DTOs for foreign key references (memberId, bagTag)
2. Read event listeners in services/*/messaging/consumer/
3. Read event publishers in services/*/messaging/producer/
...
```

### 7.2 Context Assembly (What AI Receives)

When the agent is invoked, Kiro assembles the **complete context** sent to the AI:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CONTEXT SENT TO AI                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. STEERING FILES (Always Included)                                 │
│     ├── .kiro/steering/product.md (domain knowledge)                │
│     │   • Service ports (8081-8085)                                 │
│     │   • Event types (booking.confirmed.v1, etc.)                  │
│     │   • EventEnvelope structure (meta.eventId, data)              │
│     │   • Saga flow diagram                                         │
│     │                                                                │
│     ├── .kiro/steering/structure.md (architecture rules)            │
│     │   • Hexagonal architecture                                    │
│     │   • Package naming conventions                                │
│     │                                                                │
│     └── .kiro/steering/tech.md (technology stack)                   │
│         • Spring Boot 3.3.2, TestNG, RestAssured                    │
│         • Port assignments                                          │
│                                                                      │
│  2. HOOK PROMPT (From the hook file)                                │
│     "Generate E2E cross-service integration tests..."               │
│                                                                      │
│  3. REFERENCED PROMPT FILES (AI reads these)                        │
│     ├── prompts/TEST_GENERATION_BLUEPRINT.md                        │
│     └── prompts/12-generate-e2e-saga-tests.md                       │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.3 AI Execution Flow (Step-by-Step)

Once the AI receives the context, it follows this execution flow:

```
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 1: READ GOVERNING DOCUMENTS                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AI reads prompts/TEST_GENERATION_BLUEPRINT.md:                     │
│  • Sources of truth hierarchy                                       │
│  • Agent model (GenTests scope)                                     │
│  • Output rules (diffs only)                                        │
│  • Governance rules (no production code imports)                    │
│                                                                      │
│  AI reads prompts/12-generate-e2e-saga-tests.md:                    │
│  • Required test scenarios                                          │
│  • Service interaction map                                          │
│  • Polling strategy (500ms, 20 attempts)                            │
│  • SoapResponse API (getStatusCode, not statusCode)                 │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 2: MANDATORY DISCOVERY (Cross-Service Integration Points)    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Step 2.1: Scan DTOs for Foreign Key References                     │
│  ─────────────────────────────────────────────────────────────────  │
│  AI reads: services/booking-service/src/main/java/**/dto/*.java     │
│                                                                      │
│  Discovers in CreateBookingRequest:                                 │
│  • memberId (String, optional) → "This references Loyalty Service!" │
│                                                                      │
│  Discovers in BookingResponse:                                      │
│  • bagTag (String, optional) → "This is populated by Baggage!"      │
│  • loyaltyAccrualStatus → "This tracks Loyalty integration!"        │
│  • loyaltyPoints → "Points from Loyalty Service!"                   │
│                                                                      │
│  Step 2.2: Read Event Listeners                                     │
│  ─────────────────────────────────────────────────────────────────  │
│  AI reads: services/booking-service/.../BaggageEventsListener.java  │
│                                                                      │
│  Discovers:                                                         │
│  • @KafkaListener(topics = "baggage.events")                        │
│  • Handles "baggage.checked_in.v1" event                            │
│  • Updates booking with bagTag                                      │
│                                                                      │
│  AI reads: services/baggage-service/.../BookingEventsListener.java  │
│                                                                      │
│  Discovers:                                                         │
│  • @KafkaListener(topics = "booking.events")                        │
│  • Handles "booking.confirmed.v1" event                             │
│  • Auto-creates baggage for confirmed booking                       │
│                                                                      │
│  Step 2.3: Read Event Publishers                                    │
│  ─────────────────────────────────────────────────────────────────  │
│  AI reads: services/booking-service/.../BookingEventPublisher.java  │
│                                                                      │
│  Discovers published events:                                        │
│  • booking.confirmed.v1 → Triggers baggage auto-creation            │
│                                                                      │
│  Step 2.4: Read SOAP Client (for Loyalty)                           │
│  ─────────────────────────────────────────────────────────────────  │
│  AI reads: services/booking-service/.../LoyaltySoapClient.java      │
│                                                                      │
│  Discovers:                                                         │
│  • accruePoints() method called when booking confirmed              │
│  • Uses SOAP to call Loyalty Service                                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 3: MAP INTEGRATION FLOWS                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AI constructs mental model:                                        │
│                                                                      │
│  Flow 1: Loyalty Integration                                        │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 1. User provides memberId in CreateBookingRequest           │   │
│  │ 2. Booking reaches CONFIRMED status                         │   │
│  │ 3. BookingAppService calls LoyaltySoapClient.accruePoints() │   │
│  │ 4. Loyalty Service returns points                           │   │
│  │ 5. Booking updated with loyaltyAccrualStatus=SUCCEEDED      │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  Flow 2: Baggage Integration                                        │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 1. Booking reaches CONFIRMED status                         │   │
│  │ 2. BookingEventPublisher publishes booking.confirmed.v1     │   │
│  │ 3. Baggage Service's BookingEventsListener receives event   │   │
│  │ 4. Baggage auto-created, publishes baggage.checked_in.v1    │   │
│  │ 5. Booking Service's BaggageEventsListener receives event   │   │
│  │ 6. Booking updated with bagTag                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 4: READ EXISTING FRAMEWORK                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AI reads existing framework classes to understand available APIs:  │
│                                                                      │
│  • framework/clients/RestAssuredApiClient.java                      │
│    → get(), post() methods                                          │
│                                                                      │
│  • framework/soap/SoapClientImpl.java                               │
│    → sendRequest(soapAction, envelope)                              │
│                                                                      │
│  • framework/soap/LoyaltySoapRequestBuilder.java                    │
│    → enrollMember(), getMemberStatus(), accruePoints()              │
│                                                                      │
│  • framework/soap/SoapResponseParser.java                           │
│    → extractElement(response, "memberId", String.class)             │
│                                                                      │
│  • framework/endpoints/BookingEndpoints.java                        │
│    → BASE = "/bookings", STATUS = "/bookings/{id}/status"           │
│                                                                      │
│  • framework/endpoints/LoyaltyEndpoints.java                        │
│    → SOAP_ACTION_ENROLL, SOAP_ACTION_STATUS                         │
│                                                                      │
│  • framework/headers/CorrelationIdSupport.java                      │
│    → generate(), withCorrelationId()                                │
│                                                                      │
│  • framework/reporting/ReportLogger.java                            │
│    → logStep(), logAssertion(), info()                              │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 5: GENERATE TEST CODE                                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AI generates LoyaltyBookingIntegrationTest.java:                   │
│                                                                      │
│  @Test(groups = {"e2e", "loyalty"})                                 │
│  public void shouldAccrueLoyaltyPointsWhenBookingConfirmed() {      │
│      // Step 1: Enroll member via SOAP                              │
│      String enrollEnvelope = LoyaltySoapRequestBuilder              │
│          .enrollMember(firstName, lastName, email);                 │
│      SoapResponse enrollResponse = loyaltySoapClient                │
│          .sendRequest(SOAP_ACTION_ENROLL, enrollEnvelope);          │
│      String memberId = SoapResponseParser                           │
│          .extractElement(response, "memberId", String.class);       │
│                                                                      │
│      // Step 2: Seed inventory                                      │
│      // Step 3: Create booking WITH memberId                        │
│      // Step 4: Poll for CONFIRMED status                           │
│      // Step 5: Verify loyaltyAccrualStatus = SUCCEEDED             │
│      // Step 6: Verify points via SOAP GetMemberStatus              │
│  }                                                                   │
│                                                                      │
│  AI generates BaggageBookingIntegrationTest.java:                   │
│                                                                      │
│  @Test(groups = {"e2e", "baggage"})                                 │
│  public void shouldAutoCreateBaggageWhenBookingConfirmed() {        │
│      // Step 1: Seed inventory                                      │
│      // Step 2: Create booking                                      │
│      // Step 3: Poll for CONFIRMED status                           │
│      // Step 4: Poll for bagTag population (async event)            │
│      // Step 5: Verify baggage trackable via Baggage Service        │
│  }                                                                   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 6: WRITE FILES                                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AI creates/modifies files:                                         │
│                                                                      │
│  Created: api-tests/src/test/java/tests/e2e/                        │
│           LoyaltyBookingIntegrationTest.java (~200 lines)           │
│                                                                      │
│  Created: api-tests/src/test/java/tests/e2e/                        │
│           BaggageBookingIntegrationTest.java (~180 lines)           │
│                                                                      │
│  Output: Diffs shown in Kiro chat                                   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.4 Why This Works: The Knowledge Chain

```
┌─────────────────────────────────────────────────────────────────────┐
│                     KNOWLEDGE CHAIN                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  STEERING FILES provide:                                            │
│  ├── Service ports (8081-8085)                                      │
│  ├── Event structure (meta.eventId, meta.correlationId, data)       │
│  ├── Saga flow (Booking → Inventory → Payment)                      │
│  └── Architecture rules (hexagonal, package naming)                 │
│                                                                      │
│                              ↓                                       │
│                                                                      │
│  PROMPT FILES provide:                                              │
│  ├── Test scenarios to generate                                     │
│  ├── Polling strategy (500ms, 20 attempts)                          │
│  ├── API method names (getStatusCode, not statusCode)               │
│  └── Output format (diffs only, TestNG)                             │
│                                                                      │
│                              ↓                                       │
│                                                                      │
│  DISCOVERY PHASE provides:                                          │
│  ├── Integration points (memberId → Loyalty, bagTag → Baggage)      │
│  ├── Event flows (booking.confirmed.v1 → baggage auto-creation)     │
│  └── SOAP operations (EnrollMember, GetMemberStatus)                │
│                                                                      │
│                              ↓                                       │
│                                                                      │
│  FRAMEWORK READING provides:                                        │
│  ├── Available clients (RestAssuredApiClient, SoapClientImpl)       │
│  ├── Helper classes (LoyaltySoapRequestBuilder, SoapResponseParser) │
│  └── Endpoint constants (BookingEndpoints.STATUS)                   │
│                                                                      │
│                              ↓                                       │
│                                                                      │
│  GENERATED CODE uses all of the above to produce:                   │
│  └── Working, compilable, runnable integration tests                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.5 Key Insight: Why Discovery is Mandatory

Without the discovery phase, the AI would NOT know:

| Without Discovery | With Discovery |
|-------------------|----------------|
| ❌ memberId is just a string field | ✅ memberId triggers Loyalty SOAP call |
| ❌ bagTag is just a string field | ✅ bagTag is populated by async Kafka event |
| ❌ No idea about event listeners | ✅ Knows BaggageEventsListener updates bagTag |
| ❌ Would generate isolated tests | ✅ Generates cross-service integration tests |

This is why the prompt says **"MANDATORY: First discover cross-service integration points"**.

---

## 8. AI Generation Methodology

### How Kiro Discovers Cross-Service Integrations

The AI doesn't just generate tests blindly. It follows a **mandatory discovery phase**:

```markdown
# From gen-e2e-integration-tests.md agent:

## MANDATORY DISCOVERY PHASE (DO THIS FIRST)

### Step 1: Scan DTOs for Foreign Key References
Read ALL request/response DTOs and identify fields that reference other services:
- `memberId` → Loyalty Service integration
- `bagTag` → Baggage Service integration

### Step 2: Read Event Listeners
For each service, read:
services/{service}-service/src/main/java/**/messaging/consumer/*Listener.java

### Step 3: Read Event Publishers
For each service, read:
services/{service}-service/src/main/java/**/messaging/producer/*Publisher.java

### Step 4: Map Complete Flows
Document the complete flow for each business scenario before generating tests.
```

### Governance Hierarchy

```
TEST_GENERATION_BLUEPRINT.md (Supreme Authority)
├── 00-agent-operating-rules.md (Operational constraints)
├── 00-prompt-registry.md (Version control & config)
├── 00-security-guardrails.md (Security policies)
└── Agent-specific prompts
    ├── api-test-framework.md
    ├── api-test-generate.md
    └── gen-e2e-integration-tests.md
```

**Rule**: When conflicting instructions exist, higher file wins.

---

## 9. Demo Walkthrough

### Step 1: Show Kiro Configuration
```bash
# Show steering files (always-included context)
ls .kiro/steering/

# Show agents (task-specific prompts)
ls .kiro/agents/

# Show hooks (event-driven automation)
ls .kiro/hooks/
```

### Step 2: Run Backend Services
```bash
# Start infrastructure
docker-compose -f infra/docker-compose.yml up -d

# Start services (in separate terminals)
./gradlew :services:booking-service:bootRun
./gradlew :services:inventory-service:bootRun
./gradlew :services:payment-service:bootRun
./gradlew :services:baggage-service:bootRun
./gradlew :services:loyalty-service:bootRun
```

### Step 3: Demonstrate Agent Invocation (via Chat)
```
1. Open Kiro IDE
2. In chat, type: "Follow .kiro/agents/api-test-discovery.md"
3. Watch AI discover all 5 services
4. In chat, type: "Follow .kiro/agents/gen-e2e-integration-tests.md"
5. Watch AI generate E2E tests
```

### Step 4: Demonstrate Automation Hooks
```
1. Open a controller file: services/booking-service/.../BookingController.java
2. Make a small change and save
3. Watch the "Audit Tests on Controller Change" hook fire automatically
4. AI reports test coverage gaps without you asking
```

### Step 5: Run AI-Generated Tests
```bash
# Set environment variables
export BASE_URL_BOOKING=http://localhost:8081
export BASE_URL_INVENTORY=http://localhost:8082
export BASE_URL_PAYMENT=http://localhost:8083
export BASE_URL_BAGGAGE=http://localhost:8084
export BASE_URL_LOYALTY=http://localhost:8085
export ENV=local

# Run smoke tests
./gradlew :api-tests:test --tests "tests.smoke.*"

# Run E2E tests
./gradlew :api-tests:test --tests "tests.e2e.*"
```

---

## 10. Key Talking Points

### What Makes This Special - Kiro Capabilities Demonstrated

| Kiro Feature | How We Used It | Benefit |
|--------------|----------------|---------|
| **Steering Files** | 3 files with domain, architecture, tech rules | AI always knows project context |
| **Agents** | 7 specialized agents for different tasks | Single-responsibility, reusable prompts |
| **Hooks** | 3 event-driven automations | Automatic audits on file changes |
| **Specs** | Formal requirements → design → tasks | Structured feature development |

### Key Distinction: Agents vs Hooks

| Aspect | Agents | Hooks |
|--------|--------|-------|
| **Purpose** | Generate code on demand | React to events automatically |
| **Invocation** | User asks in chat | Triggered by file changes |
| **Example** | "Generate E2E tests" | "When controller changes, audit" |
| **Use case** | Active development | Continuous validation |

### AI Generation Benefits

| Benefit | Evidence |
|---------|----------|
| **Consistency** | All tests follow same patterns (TestNG, AssertJ, ReportLogger) |
| **Discovery** | AI found Loyalty + Baggage integrations by analyzing DTOs |
| **Multi-Protocol** | Same framework handles JSON REST, XML REST, SOAP |
| **Governance** | Strict rules prevent production code access |

### Lessons Learned

1. **Steering files are critical** - Without `product.md`, AI wouldn't know event structure
2. **Optional fields reveal integrations** - `memberId` and `bagTag` were optional but important
3. **Hooks enable non-developers** - QA team can generate tests without writing prompts
4. **Specs provide traceability** - 20 requirements map to generated code

---

## 11. Files to Highlight During Review

### Kiro Configuration (Show First)
```
.kiro/steering/product.md              # Domain knowledge (always included)
.kiro/agents/gen-e2e-integration-tests.md  # E2E agent (invoked via chat)
.kiro/hooks/audit-on-controller-change.kiro.hook  # Automation hook (event-driven)
```

### AI-Generated Framework
```
api-tests/src/test/java/framework/clients/ApiClient.java
api-tests/src/test/java/framework/soap/SoapClient.java
api-tests/src/test/java/framework/config/TestConfig.java
```

### AI-Generated Tests
```
api-tests/src/test/java/tests/smoke/BookingOpenApiSmokeTest.java
api-tests/src/test/java/tests/e2e/LoyaltyBookingIntegrationTest.java
api-tests/src/test/java/tests/e2e/BaggageBookingIntegrationTest.java
```

### Governing Prompts
```
prompts/TEST_GENERATION_BLUEPRINT.md   # Supreme authority
prompts/12-generate-e2e-saga-tests.md  # E2E generation rules
```

---

## 12. Q&A Preparation

### Expected Questions

**Q: How does Kiro know what tests to generate?**
A: Three layers of context:
1. Steering files (always included) - domain, architecture, tech
2. Agent prompts - task-specific instructions
3. Prompt files - detailed generation rules

**Q: Can the AI modify production code?**
A: No. The `TEST_GENERATION_BLUEPRINT.md` explicitly forbids imports from `*.domain.*` or `*.application.*`.

**Q: How are cross-service integrations discovered?**
A: The `gen-e2e-integration-tests.md` agent has a mandatory discovery phase:
1. Scan DTOs for foreign key patterns (memberId, bagTag)
2. Read event listeners in `messaging/consumer/`
3. Read event publishers in `messaging/producer/`

**Q: What if the API changes?**
A: Two options:
1. Invoke the snapshot agent: "Follow .kiro/agents/api-test-snapshot.md"
2. The `audit-on-controller-change` hook automatically alerts you to coverage gaps

**Q: What's the difference between steering, agents, and hooks?**
A: 
- **Steering** = Context (always included, provides domain knowledge)
- **Agents** = Instructions (invoked via chat, generate code)
- **Hooks** = Automation (triggered by events, react to changes)

**Q: How do I invoke an agent?**
A: In Kiro chat, type:
```
Follow .kiro/agents/gen-e2e-integration-tests.md
```
Or simply describe what you want - Kiro will find the right agent.

**Q: When do hooks run?**
A: Hooks run automatically when their trigger event occurs:
- `audit-on-controller-change` → When you save a controller file
- `audit-on-dto-change` → When you save a DTO file
- `lint-on-test-save` → When you save a test file

**Q: How does the AI know to use getStatusCode() instead of statusCode()?**
A: The prompt file `12-generate-e2e-saga-tests.md` explicitly documents the SoapResponse API with correct method names. This prevents the AI from guessing.

---

## 13. Summary

### What Was Implemented

| Component | Type | AI-Generated | Lines |
|-----------|------|--------------|-------|
| Backend Services (5) | Java/Spring Boot | ❌ Manual | ~10,000 |
| Frontend | React/TypeScript | Partial | ~2,000 |
| **Steering Files (3)** | Markdown | ❌ Manual | ~500 |
| **Agents (7)** | Markdown | ❌ Manual | ~400 |
| **Hooks (3)** | JSON | ❌ Manual | ~50 |
| **Specs (2)** | Markdown | ❌ Manual | ~600 |
| **Prompts (15)** | Markdown | ❌ Manual | ~2,000 |
| **Test Framework** | Java | ✅ AI-Generated | ~2,000 |
| **Smoke Tests (5)** | Java | ✅ AI-Generated | ~200 |
| **E2E Tests (2)** | Java | ✅ AI-Generated | ~400 |

### Kiro Capabilities Demonstrated

| Capability | Files | Purpose |
|------------|-------|---------|
| **Steering** | `.kiro/steering/*.md` | Always-included project context |
| **Agents** | `.kiro/agents/*.md` | Task-specific prompts (invoked via chat) |
| **Hooks** | `.kiro/hooks/*.kiro.hook` | Event-driven automation |
| **Specs** | `.kiro/specs/*/` | Requirements → Design → Tasks |

### How to Use

| Task | Method |
|------|--------|
| Generate tests | Chat: "Follow .kiro/agents/gen-e2e-integration-tests.md" |
| Discover APIs | Chat: "Follow .kiro/agents/api-test-discovery.md" |
| Auto-audit on change | Save a controller file → Hook fires automatically |

### Total AI-Generated Code
- **Framework**: 14 packages, ~2,000 lines
- **Tests**: 7 test classes, ~600 lines
- **Total**: ~2,600 lines of production-quality test code

---

## 14. Appendix: Complete Kiro File Inventory

### Steering Files (3) - Always Included
| File | Purpose | Inclusion |
|------|---------|-----------|
| `product.md` | Domain knowledge, events, saga | Always |
| `structure.md` | Architecture rules, packages | Always |
| `tech.md` | Technology stack, ports | Always |

### Agents (7) - Invoked via Chat
| Agent | Purpose | How to Invoke |
|-------|---------|---------------|
| `api-test-discovery.md` | Read-only API discovery | "Follow .kiro/agents/api-test-discovery.md" |
| `api-test-framework.md` | Framework generation | "Follow .kiro/agents/api-test-framework.md" |
| `api-test-generate.md` | Service-specific tests | "Generate tests for booking-service" |
| `api-test-audit.md` | Coverage audit | "Follow .kiro/agents/api-test-audit.md" |
| `api-test-snapshot.md` | Contract refresh | "Follow .kiro/agents/api-test-snapshot.md" |
| `gen-e2e-integration-tests.md` | Cross-service E2E | "Follow .kiro/agents/gen-e2e-integration-tests.md" |
| `gen-kafka-event-tests.md` | Kafka event tests | "Follow .kiro/agents/gen-kafka-event-tests.md" |

### Hooks (3) - Event-Driven Automation
| Hook | Trigger Event | Action |
|------|---------------|--------|
| `audit-on-controller-change` | Controller file saved | Audit test coverage |
| `audit-on-dto-change` | DTO file saved | Check test model sync |
| `lint-on-test-save` | Test file saved | Validate conventions |

---

*Document prepared for internal team review - February 4, 2026*
*Showcasing Kiro AI-powered test automation capabilities*
