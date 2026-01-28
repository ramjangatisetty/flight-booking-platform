# Loyalty Service - SOAP API

Legacy SOAP-based microservice for airline loyalty program operations.

## Overview

- **Port**: 8084
- **WSDL**: http://localhost:8084/ws/loyalty.wsdl
- **Database**: PostgreSQL (port 5436)
- **Protocol**: SOAP 1.1 (Document/Literal)

## Operations

### 1. EnrollMember
Enrolls a new member in the loyalty program.

**Request**:
- firstName (string, required)
- lastName (string, required)
- email (string, required)

**Response**:
- memberId (UUID)
- tier (BASIC/SILVER/GOLD/PLATINUM)
- status (ACTIVE)

**Faults**:
- VALIDATION_ERROR: Missing or invalid input
- INTERNAL_ERROR: Server error

### 2. GetMemberStatus
Retrieves the status of an existing member.

**Request**:
- memberId (UUID, required)

**Response**:
- memberId (UUID)
- tier (BASIC/SILVER/GOLD/PLATINUM)
- status (ACTIVE/INACTIVE/SUSPENDED)
- pointsBalance (integer)

**Faults**:
- MEMBER_NOT_FOUND: Member does not exist
- VALIDATION_ERROR: Invalid memberId format
- INTERNAL_ERROR: Server error

## Quick Start

### 1. Start Infrastructure
```bash
docker-compose -f infra/docker-compose.yml up -d loyalty-db
```

### 2. Build and Run Service
```bash
./gradlew :services:loyalty-service:build
./gradlew :services:loyalty-service:bootRun
```

### 3. Verify WSDL
```bash
curl http://localhost:8084/ws/loyalty.wsdl
```

### 4. Seed Demo Data
```bash
curl -X POST http://localhost:8084/loyalty/admin/seed
```

This creates 3 members:
- John Silver (SILVER, 5000 points)
- Jane Gold (GOLD, 15000 points)
- Bob Platinum (PLATINUM, 50000 points)

## SOAP Request Examples

### EnrollMember

```bash
curl -X POST http://localhost:8084/ws \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: http://letzautomate.com/loyalty/v1/EnrollMember" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:loy="http://letzautomate.com/loyalty/v1">
  <soapenv:Header/>
  <soapenv:Body>
    <loy:EnrollMemberRequest>
      <loy:firstName>Alice</loy:firstName>
      <loy:lastName>Johnson</loy:lastName>
      <loy:email>alice.johnson@example.com</loy:email>
    </loy:EnrollMemberRequest>
  </soapenv:Body>
</soapenv:Envelope>'
```

**Expected Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:EnrollMemberResponse xmlns:ns2="http://letzautomate.com/loyalty/v1">
      <ns2:memberId>a1b2c3d4-e5f6-7890-abcd-ef1234567890</ns2:memberId>
      <ns2:tier>BASIC</ns2:tier>
      <ns2:status>ACTIVE</ns2:status>
    </ns2:EnrollMemberResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### GetMemberStatus

First, get a memberId from the seed data or enrollment response, then:

```bash
curl -X POST http://localhost:8084/ws \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: http://letzautomate.com/loyalty/v1/GetMemberStatus" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:loy="http://letzautomate.com/loyalty/v1">
  <soapenv:Header/>
  <soapenv:Body>
    <loy:GetMemberStatusRequest>
      <loy:memberId>REPLACE_WITH_ACTUAL_MEMBER_ID</loy:memberId>
    </loy:GetMemberStatusRequest>
  </soapenv:Body>
</soapenv:Envelope>'
```

**Expected Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:GetMemberStatusResponse xmlns:ns2="http://letzautomate.com/loyalty/v1">
      <ns2:memberId>a1b2c3d4-e5f6-7890-abcd-ef1234567890</ns2:memberId>
      <ns2:tier>SILVER</ns2:tier>
      <ns2:status>ACTIVE</ns2:status>
      <ns2:pointsBalance>5000</ns2:pointsBalance>
    </ns2:GetMemberStatusResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### SOAP Fault Example (Member Not Found)

```bash
curl -X POST http://localhost:8084/ws \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: http://letzautomate.com/loyalty/v1/GetMemberStatus" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:loy="http://letzautomate.com/loyalty/v1">
  <soapenv:Header/>
  <soapenv:Body>
    <loy:GetMemberStatusRequest>
      <loy:memberId>00000000-0000-0000-0000-000000000000</loy:memberId>
    </loy:GetMemberStatusRequest>
  </soapenv:Body>
</soapenv:Envelope>'
```

**Expected Fault Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <SOAP-ENV:Fault>
      <faultcode>SOAP-ENV:Server</faultcode>
      <faultstring xml:lang="en">Member not found: 00000000-0000-0000-0000-000000000000</faultstring>
    </SOAP-ENV:Fault>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

## Database Schema

```sql
CREATE TABLE loyalty_members (
  member_id uuid PRIMARY KEY,
  first_name varchar(100) NOT NULL,
  last_name varchar(100) NOT NULL,
  email varchar(255) NOT NULL UNIQUE,
  tier varchar(20) NOT NULL,
  status varchar(20) NOT NULL,
  points_balance integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL
);
```

## Tier Levels

- **BASIC**: 0-4,999 points (default for new members)
- **SILVER**: 5,000-14,999 points
- **GOLD**: 15,000-49,999 points
- **PLATINUM**: 50,000+ points

## Admin Endpoints (REST)

### Seed Demo Data
```bash
POST /loyalty/admin/seed
```

Creates 3 demo members with different tiers.

### Reset Data
```bash
POST /loyalty/admin/reset
```

Deletes all loyalty members (for testing).

## Testing with SoapUI or Postman

1. Import WSDL: http://localhost:8084/ws/loyalty.wsdl
2. Use the generated request templates
3. Replace placeholder values with actual data

## Architecture

Follows hexagonal architecture pattern:
- **api/controller**: REST admin endpoints
- **application**: Business logic (LoyaltyService)
- **domain**: (minimal - no complex domain logic needed)
- **infrastructure/soap**: SOAP endpoint and configuration
- **infrastructure/persistence**: JPA entities and repositories

## Troubleshooting

### WSDL Not Accessible
- Ensure service is running: `curl http://localhost:8084/actuator/health`
- Check logs for startup errors

### Database Connection Issues
- Verify PostgreSQL is running: `docker ps | grep loyalty-db`
- Check connection string in application-local.yml

### SOAP Faults
- Check request XML is well-formed
- Verify namespace URIs match: `http://letzautomate.com/loyalty/v1`
- Ensure SOAPAction header is set correctly
