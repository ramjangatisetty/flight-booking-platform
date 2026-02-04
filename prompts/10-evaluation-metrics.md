# Evaluation Metrics & Success Criteria

> **Purpose**: Measure AI test generation effectiveness  
> **Review Cycle**: Weekly for metrics, Monthly for targets  
> **Owner**: QA Platform Team  

---

## 1. Generation Quality Metrics

### 1.1 Compilation Success Rate

| Attribute | Definition |
|-----------|------------|
| **Name** | Compilation Success Rate |
| **Definition** | Percentage of generated files that compile without errors on first attempt |
| **Formula** | `(successful_compilations / total_generations) x 100` |
| **Target** | 100% |
| **Measurement** | CI/CD pipeline (`javac` exit code) |
| **Frequency** | Every generation |
| **Alert Threshold** | <95% |

---

### 1.2 Contract Alignment Score

| Attribute | Definition |
|-----------|------------|
| **Name** | Contract Alignment Score |
| **Definition** | Percentage of generated test endpoints that exist in OpenAPI snapshot |
| **Formula** | `(valid_endpoints / total_endpoints_tested) x 100` |
| **Target** | 100% |
| **Measurement** | Cross-reference with `openapi-snapshots/` |
| **Frequency** | Every generation |

---

### 1.3 Assertion Appropriateness

| Attribute | Definition |
|-----------|------------|
| **Name** | Assertion Appropriateness |
| **Definition** | Percentage of assertions that validate business behavior (not just HTTP 200) |
| **Formula** | `(meaningful_assertions / total_assertions) x 100` |
| **Target** | >80% |
| **Measurement** | Static analysis + Manual sampling |
| **Review** | Weekly spot checks |

**Examples**:
```java
// GOOD - Validates business logic
assertThat(response.getBookingStatus()).isEqualTo("CONFIRMED");

// BAD - Only checks HTTP status
assertThat(response.getStatusCode()).isEqualTo(200);
```

---

## 2. Test Effectiveness Metrics

### 2.1 First-Run Pass Rate

| Attribute | Definition |
|-----------|------------|
| **Name** | First-Run Pass Rate |
| **Definition** | Percentage of generated tests passing on first execution in clean environment |
| **Formula** | `(tests_passed_first_run / total_tests) x 100` |
| **Target** | >90% |
| **Measurement** | Clean Docker environment, no retries |

---

### 2.2 Flakiness Rate

| Attribute | Definition |
|-----------|------------|
| **Name** | Flakiness Rate |
| **Definition** | Percentage of tests with inconsistent results across multiple runs |
| **Formula** | `(flaky_tests / total_tests) x 100` |
| **Target** | <3% |
| **Measurement** | Run same test suite 3 times, compare results |
| **Failure Action** | Quarantine test, analyze timing issues |

**Classification**:
- **Stable**: Same result all 3 runs
- **Flaky**: Different results across runs
- **Consistently Failing**: Fails all 3 runs

---

### 2.3 False Positive Rate

| Attribute | Definition |
|-----------|------------|
| **Name** | False Positive Rate |
| **Definition** | Percentage of tests that pass but should fail (bad oracle) |
| **Formula** | `(false_positives / total_defects) x 100` |
| **Target** | <2% |
| **Measurement** | Mutation testing, intentional bug introduction |
| **Review** | Monthly quality audit |

---

## 3. Business Impact Metrics

### 3.1 Coverage Improvement

| Attribute | Definition |
|-----------|------------|
| **Name** | Coverage Improvement |
| **Definition** | Line/branch coverage increase per sprint attributable to AI generation |
| **Formula** | `(current_coverage - baseline_coverage)` |
| **Target** | +10% per month until 80% achieved |
| **Measurement** | JaCoCo reports, compared to pre-AI baseline |

---

### 3.2 Defect Detection Rate

| Attribute | Definition |
|-----------|------------|
| **Name** | Defect Detection Rate |
| **Definition** | Percentage of production bugs that should have been caught by generated tests |
| **Formula** | `(caught_by_tests / total_production_defects) x 100` |
| **Target** | >70% of API contract violations |
| **Measurement** | Post-mortem analysis of production incidents |

---

### 3.3 Maintenance Reduction

| Attribute | Definition |
|-----------|------------|
| **Name** | Maintenance Reduction |
| **Definition** | Hours saved vs. manual test writing |
| **Formula** | `(manual_hours - ai_hours) / manual_hours x 100` |
| **Target** | 60% reduction in boilerplate test creation |
| **Measurement** | Time tracking + engineer surveys |

**Example Calculation**:
```
Manual: 100 tests x 30 min = 50 hours x $100/hr = $5,000
AI: 10 hours review + $50 API = $1,050
Savings: $3,950 (79% reduction)
```

---

## 4. Operational Metrics

### 4.1 Generation Latency

| Attribute | Definition |
|-----------|------------|
| **Name** | Generation Latency |
| **Definition** | Time from prompt submission to validated code output |
| **Formula** | `timestamp_output - timestamp_input` |
| **Target** | <30 seconds for single endpoint |
| **Measurement** | Pipeline timestamps |
| **Alert Threshold** | >60 seconds |

---

### 4.2 Cost Efficiency

| Attribute | Definition |
|-----------|------------|
| **Name** | Cost Efficiency |
| **Definition** | LLM API cost per test case generated |
| **Formula** | `total_api_cost / total_test_cases` |
| **Target** | <$0.10 per test case |
| **Measurement** | OpenAI API dashboard |

**Cost Tracking**:
```yaml
month: 2026-01
total_cost_usd: 450.00
total_tests_generated: 5000
cost_per_test: 0.09
```

---

### 4.3 Prompt Effectiveness

| Attribute | Definition |
|-----------|------------|
| **Name** | Prompt Effectiveness |
| **Definition** | Number of iterations to acceptable output |
| **Formula** | `total_iterations / total_generations` |
| **Target** | <1.5 (most first-try) |
| **Measurement** | Human intervention count |
| **Review** | Quarterly prompt refinement |

---

## 5. Dashboard Layout

```
AI TEST GENERATION QUALITY DASHBOARD

Quality Gates (Last 7 Days)
Compilation      | 100%  [OK]
Contract Align   | 100%  [OK]
First-Run Pass   | 94%   [WARNING]
Flakiness        | 2.1%  [OK]
False Positives  | 1.8%  [OK]

Business Impact (MTD)
Coverage         | +12%
Defect Detection | 75%
Maintenance      | 65%

Operations (Realtime)
Avg Latency      | 18s   [OK]
Cost/Test        | $0.08 [OK]
Success Rate     | 97%   [OK]

Alerts (Last 24h)
- 3 Flakiness warnings
- 1 Contract drift detected
- 0 Security findings
```

---

## 6. Review Cadence

| Review Type | Frequency | Participants | Focus |
|-------------|-----------|--------------|-------|
| Daily Standup | Daily | QA Team | Blockers, flakiness issues |
| Weekly Metrics | Weekly | QA + Dev | Trends, coverage progress |
| Monthly Review | Monthly | QA Architect | Prompt effectiveness, ROI |
| Quarterly Audit | Quarterly | All stakeholders | Strategy, tool evaluation |

---

## 7. Continuous Improvement

### Feedback Loop
```
Measure -> Analyze -> Improve -> Measure
```

### Metric-Driven Prompt Updates

| Metric Threshold | Action | Owner |
|------------------|--------|-------|
| Compilation <95% | Emergency prompt review | QA Architect |
| Flakiness >5% | Add stability requirements | Senior QA |
| Coverage stagnant | Increase scenario depth | QA Engineer |
| Cost >$0.15/test | Optimize prompt efficiency | Platform Team |

---

*Metrics are the foundation of AI test generation quality. Review weekly, act on trends.*
