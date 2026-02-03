# CRF Builder - Visual Architecture Summary

## System Overview Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    OpenClinica CRF Builder System                    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         DESIGN PHASE                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │    Excel     │ OR │   Web-based  │ OR │   XForm      │          │
│  │   Template   │    │   Designer   │    │   Mobile     │          │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘          │
│         │                   │                    │                   │
│         └───────────────────┴────────────────────┘                   │
│                              │                                        │
│                              ▼                                        │
│                    ┌──────────────────┐                              │
│                    │  Upload/Parse    │                              │
│                    │   Validate       │                              │
│                    └─────────┬────────┘                              │
│                              │                                        │
│                              ▼                                        │
│                    ┌──────────────────┐                              │
│                    │ Store Metadata   │                              │
│                    │  in Database     │                              │
│                    └─────────┬────────┘                              │
│                              │                                        │
│  ┌───────────────────────────┴────────────────────────┐             │
│  │                                                     │             │
│  ▼                    ▼                    ▼           ▼             │
│ CRF              CRF Version          Sections      Items            │
│ Definition       (v1.0, v2.0)        (Pages)       (Fields)          │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     STUDY SETUP PHASE                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────┐          ┌──────────────────┐                   │
│  │ Study Events   │          │      CRFs        │                   │
│  │ (Visits)       │          │   (Forms)        │                   │
│  │                │          │                  │                   │
│  │ • Screening    │   ◄──►   │ • Demographics   │                   │
│  │ • Visit 1      │          │ • Vitals         │                   │
│  │ • Visit 2      │          │ • Lab Results    │                   │
│  │ • Week 4       │          │ • Adverse Events │                   │
│  └────────────────┘          └──────────────────┘                   │
│         │                              │                             │
│         └──────────────┬───────────────┘                             │
│                        │                                             │
│                        ▼                                             │
│              ┌──────────────────┐                                    │
│              │ Event Definition │                                    │
│              │      CRF         │                                    │
│              │  (Association)   │                                    │
│              └──────────────────┘                                    │
│              Which CRFs appear in which visits?                      │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   DATA COLLECTION PHASE                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Subject Matrix (Visit Grid)                                         │
│  ┌────────┬──────────┬──────────┬──────────┬──────────┐            │
│  │Subject │Screening │ Visit 1  │ Visit 2  │ Visit 3  │            │
│  ├────────┼──────────┼──────────┼──────────┼──────────┤            │
│  │ S-001  │   100%   │   75%    │   25%    │    0%    │◄─Status    │
│  │        │   ✓✓✓✓   │   ✓✓✓○   │   ✓○○○   │   ○○○○   │            │
│  ├────────┼──────────┼──────────┼──────────┼──────────┤            │
│  │ S-002  │   100%   │   100%   │   50%    │    0%    │            │
│  │        │   ✓✓✓✓   │   ✓✓✓✓   │   ✓✓○○   │   ○○○○   │            │
│  └────────┴──────────┴──────────┴──────────┴──────────┘            │
│                                                                       │
│  Click on cell → List of CRFs for that visit                        │
│  ┌─────────────────────────────────────────────────────┐            │
│  │ Visit 1 - Subject S-001                             │            │
│  │ ┌───────────────┬──────────┬────────────────┐       │            │
│  │ │ Demographics  │ Complete │ [View] [Print] │       │            │
│  │ │ Vitals        │ Complete │ [View] [Print] │       │            │
│  │ │ Lab Results   │ Complete │ [View] [Print] │       │            │
│  │ │ Adverse Evts  │ Not Strt │ [Start]        │◄─Click│            │
│  │ └───────────────┴──────────┴────────────────┘       │            │
│  └─────────────────────────────────────────────────────┘            │
│                         │                                            │
│                         ▼                                            │
│              ┌──────────────────┐                                    │
│              │  Data Entry Form │                                    │
│              │   (Rendered CRF) │                                    │
│              └─────────┬────────┘                                    │
│                        │                                             │
│                        ▼                                             │
│              ┌──────────────────┐                                    │
│              │   Save to        │                                    │
│              │   item_data      │                                    │
│              └──────────────────┘                                    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                      EXPORT PHASE                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │    Excel     │  │   ODM XML    │  │     PDF      │              │
│  │  Template    │  │  (CDISC)     │  │    Print     │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │     CSV      │  │     SAS      │  │    SPSS      │              │
│  │     Data     │  │    Format    │  │   Format     │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Model Simplified

```
┌─────────┐
│  study  │
└────┬────┘
     │
     ├──────────────────────────────┐
     │                              │
     ▼                              ▼
┌─────────┐              ┌──────────────────┐
│   crf   │              │study_event_      │
│         │              │  definition      │
│ • name  │              │                  │
│ • desc  │              │ • name           │
└────┬────┘              │ • repeating      │
     │                   └────────┬─────────┘
     │                            │
     ▼                            │
┌──────────────┐                  │
│ crf_version  │                  │
│              │                  ▼
│ • name       │        ┌──────────────────┐
│ • desc       │        │event_definition_ │
└──────┬───────┘        │       crf        │◄─┐
       │                │                  │  │
       │                │ Links CRFs to    │  │
       │                │ Study Events     │  │
       ▼                └────────┬─────────┘  │
┌─────────────┐                 │            │
│   section   │                 ▼            │
│             │       ┌──────────────────┐   │
│ • label     │       │  study_event     │   │
│ • title     │       │                  │   │
│ • ordinal   │       │ • subject        │   │
└──────┬──────┘       │ • date           │   │
       │              └────────┬─────────┘   │
       │                       │             │
       ▼                       ▼             │
┌─────────────┐      ┌──────────────────┐   │
│    item     │      │   event_crf      │───┘
│             │      │                  │
│ • name      │      │ Instance of CRF  │
│ • type      │      │ for subject      │
│ • units     │      └────────┬─────────┘
└──────┬──────┘               │
       │                      │
       ├──────────────────────┤
       │                      │
       ▼                      ▼
┌─────────────────┐  ┌──────────────────┐
│ item_form_      │  │   item_data      │
│   metadata      │  │                  │
│                 │  │ • value          │
│ • label         │  │ • ordinal        │
│ • validation    │  └──────────────────┘
│ • required      │
└─────────────────┘
```

## Technology Stack Evolution

```
CURRENT STATE (Legacy)
┌─────────────────────────────────────┐
│ Frontend:  JSP, jQuery              │
│ Backend:   Java Servlets, JDBC      │
│ Database:  PostgreSQL               │
│ Design:    Excel templates          │
└─────────────────────────────────────┘
             │
             │ MIGRATION
             ▼
FUTURE STATE (Modern)
┌─────────────────────────────────────┐
│ Frontend:  React, TypeScript, MUI   │
│ Backend:   Spring Boot 3, REST API  │
│ Database:  PostgreSQL 15+           │
│ Design:    Web-based visual builder │
└─────────────────────────────────────┘
```

## User Roles & Workflows

```
┌───────────────────────────────────────────────────────────┐
│ SYSTEM ADMINISTRATOR                                       │
│ • Manage users                                             │
│ • Configure system                                         │
│ • Monitor performance                                      │
└───────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────┐
│   STUDY     │  │   DATA          │  │   MONITOR   │
│  DESIGNER   │  │   MANAGER       │  │             │
└─────────────┘  └─────────────────┘  └─────────────┘
       │                 │                    │
       ▼                 ▼                    ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────┐
│ • Design    │  │ • Enter data    │  │ • Review    │
│   CRFs      │  │ • Edit forms    │  │   data      │
│ • Create    │  │ • Complete      │  │ • Query     │
│   versions  │  │   forms         │  │   issues    │
│ • Define    │  │ • Print forms   │  │ • Verify    │
│   events    │  │                 │  │   source    │
│ • Associate │  └─────────────────┘  └─────────────┘
│   CRFs      │
└─────────────┘
```

## Key File Locations

```
OpenClinica/
├── core/src/main/java/org/akaza/openclinica/
│   ├── bean/
│   │   ├── admin/
│   │   │   ├── CRFBean.java              ◄─ CRF definition
│   │   │   └── NewCRFBean.java
│   │   ├── submit/
│   │   │   ├── CRFVersionBean.java       ◄─ CRF version
│   │   │   ├── SectionBean.java          ◄─ Section
│   │   │   ├── ItemBean.java             ◄─ Item
│   │   │   ├── ItemFormMetadataBean.java ◄─ Item config
│   │   │   └── ItemGroupBean.java        ◄─ Repeating group
│   │   └── managestudy/
│   │       └── EventDefinitionCRFBean.java ◄─ CRF-Event link
│   │
│   ├── dao/
│   │   ├── admin/
│   │   │   ├── CRFDAO.java               ◄─ CRF database access
│   │   │   └── CRFVersionDAO.java
│   │   └── submit/
│   │       ├── SectionDAO.java
│   │       ├── ItemDAO.java
│   │       └── ItemFormMetadataDAO.java
│   │
│   └── service/
│       └── OdmFileCreation.java          ◄─ ODM export service
│
├── web/src/main/java/org/akaza/openclinica/control/
│   └── admin/
│       ├── CreateCRFServlet.java         ◄─ Create CRF
│       ├── CreateCRFVersionServlet.java  ◄─ Upload template
│       ├── SpreadsheetPreview.java       ◄─ Parse Excel
│       ├── DownloadVersionSpreadSheet... ◄─ Download template
│       └── DownloadStudyMetadata...      ◄─ Export ODM
│
├── web/src/main/webapp/WEB-INF/jsp/admin/
│   ├── createCRF.jsp                     ◄─ Create CRF UI
│   ├── createCRFVersion.jsp              ◄─ Upload template UI
│   └── listCRF.jsp                       ◄─ List CRFs UI
│
└── docs/crf-builder/                     ◄─ DOCUMENTATION
    ├── README.md                         ◄─ Executive summary
    ├── 01-OVERVIEW.md                    ◄─ Overview
    ├── 02-ARCHITECTURE.md                ◄─ Architecture
    ├── 03-DATA-MODEL.md                  ◄─ Data model
    ├── 04-VISIT-GRID-INTEGRATION.md      ◄─ Visit grid
    ├── 05-MIGRATION-PLAN.md              ◄─ Migration plan
    └── CRF_Design_Template_v3.10.xls     ◄─ Template file
```

## Response Types Supported

```
┌──────────────┬────────────────────────────────────────┐
│ Type         │ Description                            │
├──────────────┼────────────────────────────────────────┤
│ text         │ Single-line text input                 │
│ textarea     │ Multi-line text input                  │
│ select       │ Dropdown single selection              │
│ multi-select │ Dropdown multiple selection            │
│ radio        │ Radio buttons                          │
│ checkbox     │ Checkboxes                             │
│ date         │ Date picker                            │
│ pdate        │ Partial date (year/month optional)     │
│ file         │ File upload                            │
│ calculation  │ Instant calculation from other fields  │
└──────────────┴────────────────────────────────────────┘
```

## Data Types Supported

```
┌──────────┬──────────────────────────────────────────┐
│ Type     │ Description                              │
├──────────┼──────────────────────────────────────────┤
│ ST       │ String (Character String)                │
│ INT      │ Integer                                  │
│ REAL     │ Floating point number                    │
│ DATE     │ Date (MM/DD/YYYY)                        │
│ PDATE    │ Partial Date (allows incomplete dates)   │
│ FILE     │ File reference                           │
│ BL       │ Boolean (true/false)                     │
└──────────┴──────────────────────────────────────────┘
```

## Validation Types

```
┌────────────────┬──────────────────────────────────────┐
│ Type           │ Example                              │
├────────────────┼──────────────────────────────────────┤
│ Required       │ Field must have a value              │
│ Range          │ Age: 0-120                           │
│ Regex          │ Email: ^[a-z0-9._%+-]+@[a-z0-9.-]+  │
│ Date range     │ Date must be in past                 │
│ Custom func    │ checkLuhn(creditCard)                │
│ Conditional    │ If A = "Yes", B is required          │
└────────────────┴──────────────────────────────────────┘
```

## Export Formats

```
┌─────────────────┬────────────────────────────────────┐
│ Format          │ Use Case                           │
├─────────────────┼────────────────────────────────────┤
│ Excel (XLS)     │ CRF template download/edit         │
│ ODM XML         │ CDISC-compliant metadata exchange  │
│ PDF             │ Printable forms                    │
│ CSV             │ Data export for analysis           │
│ SAS             │ Statistical analysis (SAS)         │
│ SPSS            │ Statistical analysis (SPSS)        │
│ Tab-delimited   │ Data export (simple text)          │
│ JSON (REST)     │ API access to metadata             │
└─────────────────┴────────────────────────────────────┘
```

## Migration Timeline Visualization

```
Month 1-2: Foundation
████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 

Month 3-4: Core Features
████████████████░░░░░░░░░░░░░░░░░░░░

Month 5-6: Advanced Features & Forms
████████████████████████░░░░░░░░░░░░

Month 7-8: Integration & Security
████████████████████████████████░░░░

Month 9: Testing & Deployment
████████████████████████████████████
```

## Priority Matrix

```
HIGH PRIORITY     │ MEDIUM PRIORITY   │ LOW PRIORITY
──────────────────┼───────────────────┼──────────────────
✓ CRF CRUD        │ • Visual designer │ • Template library
✓ Template upload │ • Real-time collab│ • AI suggestions
✓ Form rendering  │ • Offline mode    │ • Mobile apps
✓ Data storage    │ • Advanced search │ • REDCap import
✓ Validation      │ • Analytics       │ • FHIR integration
✓ ODM export      │ • Audit trail     │
✓ Authentication  │ • Visit grid      │
```

## Success Metrics Dashboard

```
┌──────────────────────────────────────────────────────────┐
│                     SUCCESS METRICS                       │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  System Uptime           ████████████████████ 99.9%      │
│  Page Load Time          ████░░░░░░░░░░░░░░░  1.5s       │
│  API Response Time       ██████░░░░░░░░░░░░░  300ms      │
│  Test Coverage           ████████████████░░░░ 85%        │
│  User Satisfaction       ████████████████░░░░ 4.2/5.0    │
│                                                           │
├──────────────────────────────────────────────────────────┤
│  On Schedule             ✓ Yes                            │
│  On Budget               ✓ Within 5%                      │
│  Feature Complete        ✓ 100% MVP                       │
│  Security Vulnerabilities ✓ 0 Critical                    │
└──────────────────────────────────────────────────────────┘
```

## Contact Information

```
┌────────────────────────────────────────────────────────┐
│ For Questions:                                          │
│                                                         │
│ Technical:      architecture-team@example.com          │
│ Product:        product-manager@example.com            │
│ Project:        project-manager@example.com            │
│ Documentation:  docs-team@example.com                  │
│                                                         │
│ Project Board:  https://github.com/.../projects/...    │
│ Repository:     https://github.com/fderuiter/OpenClinica│
└────────────────────────────────────────────────────────┘
```

---

**Last Updated:** February 3, 2026
**Version:** 1.0
**Status:** Analysis Complete, Ready for Implementation
