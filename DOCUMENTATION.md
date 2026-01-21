# OpenClinica Codebase Documentation

## Overview
OpenClinica is an open-source software for Electronic Data Capture (EDC) and Clinical Data Management (CDM). It is a Java-based application built using Maven, Spring Framework, Hibernate, and other technologies.

## Architecture
The project is a multi-module Maven project consisting of three main modules:
- **core**: Contains the business logic, data model, DAOs, and service layer.
- **web**: Contains the web application (Servlets, JSPs, Controllers) and UI logic.
- **ws**: Contains the Web Services (SOAP/REST) for external integration.

### High-Level Architecture Diagram
```mermaid
graph TD
    User[User/Browser] --> WebApp[Web Application (web)]
    ExtSys[External System] --> WS[Web Services (ws)]

    subgraph "OpenClinica Application"
        WebApp --> Core[Core Module (core)]
        WS --> Core

        subgraph "Core Module"
            Service[Service Layer]
            DAO[DAO Layer]
            Domain[Domain Model]
        end

        Core --> DB[(Database)]
    end
```

## Data Model
The core data model revolves around Studies, Subjects, Events, and CRFs (Case Report Forms).

### Key Entities
- **Study**: Represents a clinical study or a site within a study.
- **Subject**: A participant in a study.
- **StudySubject**: The association between a Study and a Subject.
- **StudyEvent**: An event that occurs for a subject in a study (e.g., "Visit 1").
- **EventCRF**: A specific CRF filled out for a study event.
- **Item**: A question or data point in a CRF.
- **ItemData**: The actual answer or value provided for an Item.
- **UserAccount**: Represents a system user.
- **StudyUserRole**: Represents the role of a user within a specific study.

### Class Diagram
```mermaid
classDiagram
    class StudyBean {
        +int id
        +String name
        +Date datePlannedStart
        +Date datePlannedEnd
    }
    class SubjectBean {
        +int id
        +Date dateOfBirth
        +String gender
    }
    class StudySubjectBean {
        +int id
        +String label
        +Date enrollmentDate
    }
    class StudyEventBean {
        +int id
        +Date dateStarted
        +Date dateEnded
    }
    class EventCRFBean {
        +int id
        +Date dateInterviewed
    }
    class ItemBean {
        +int id
        +String name
        +String description
    }
    class ItemDataBean {
        +int id
        +String value
        +int ordinal
    }
    class UserAccountBean {
        +int id
        +String name
        +String type
    }
    class StudyUserRoleBean {
        +int studyId
        +String roleName
    }

    StudyBean "1" -- "*" StudySubjectBean : contains
    SubjectBean "1" -- "*" StudySubjectBean : participates
    StudySubjectBean "1" -- "*" StudyEventBean : has
    StudyEventBean "1" -- "*" EventCRFBean : contains
    EventCRFBean "1" -- "*" ItemDataBean : contains
    ItemBean "1" -- "*" ItemDataBean : defines
    UserAccountBean "1" -- "*" StudyUserRoleBean : has
    StudyBean "1" -- "*" StudyUserRoleBean : defines
```

## Web Layer
The web layer is primarily built on top of the Servlet API, with a custom MVC framework.

### SecureController
The `SecureController` class extends `HttpServlet` and serves as the base class for most controllers in the application. It handles:
- Authentication and Authorization
- Session Management (via `SessionManager`)
- Localization (via `LocaleResolver`)
- Request Processing (via abstract `processRequest` method)

```mermaid
classDiagram
    class HttpServlet
    class SecureController {
        +processRequest()
        +mayProceed()
        #SessionManager sm
        #UserAccountBean ub
    }
    class SubmitDataServlet {
        +processRequest()
    }
    class DataEntryServlet {
        +processRequest()
    }
    class ViewStudyServlet {
        +processRequest()
    }

    HttpServlet <|-- SecureController
    SecureController <|-- SubmitDataServlet
    SecureController <|-- DataEntryServlet
    SecureController <|-- ViewStudyServlet
```

## Web Services
The `ws` module provides endpoints for external systems to interact with OpenClinica. It uses Spring Web Services.

### Key Components
- **StudyEndpoint**: Handles study-related requests. Uses `StudyDAO` to fetch data and `MetaDataCollector` to generate ODM XML.
- **StudyMetadataRequestValidator**: Validates incoming requests.

## Sequence Flows

### Data Submission Flow (DataEntryServlet)
This diagram illustrates how data submission is handled in the application, specifically within the `DataEntryServlet` hierarchy.

```mermaid
sequenceDiagram
    participant User
    participant DataEntryServlet
    participant FormProcessor
    participant DiscrepancyValidator
    participant EventCRFDAO
    participant ItemDataDAO
    participant Database

    User->>DataEntryServlet: POST /DataEntry
    DataEntryServlet->>FormProcessor: isSubmitted()
    DataEntryServlet->>FormProcessor: getBoolean("checkInputs")

    opt isSubmitted
        DataEntryServlet->>DiscrepancyValidator: validate()

        alt No Validation Errors
            DataEntryServlet->>EventCRFDAO: update(EventCRFBean)
            EventCRFDAO->>Database: UPDATE event_crf

            loop For Each Item
                DataEntryServlet->>ItemDataDAO: create/upsert(ItemDataBean)
                ItemDataDAO->>Database: INSERT/UPDATE item_data
            end

            DataEntryServlet-->>User: Show Success Page
        else Validation Errors
            DataEntryServlet-->>User: Show Form with Errors
        end
    end
```
