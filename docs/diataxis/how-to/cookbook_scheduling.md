# Event Scheduling Guide

This guide details how to schedule study events for enrolled subjects.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Database

    Client->>API: POST /api/schedule
    alt Subject Exists & Valid Event
        API->>Database: Create Event
        Database-->>API: Success
        API-->>Client: 201 Created (Event OID)
    else Subject Not Found
        API-->>Client: 404 Not Found
    else Invalid State Transition (Edge Case)
        API-->>Client: 400 Bad Request (Validation Error)
    end
```

## Runnable Payload

The following JSON payload illustrates how to schedule an event. All dates and identifiers are fictitious.

```json
{
  "schedule": {
    "subject_oid": "SS_SUB9999",
    "event_oid": "SE_BASELINE",
    "start_date": "2023-10-02",
    "end_date": "2023-10-02",
    "location": "Fictitious Clinic A"
  }
}
```

## Response Payload

```json
{
  "status": "success",
  "study_event_oid": "SE_BASELINE_1",
  "message": "Event scheduled successfully"
}
```
