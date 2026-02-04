# CRF Design Studio - System Architecture

## Overview

The CRF Design Studio is a standalone web application for designing Case Report Forms (CRFs) and configuring Visit Grids for clinical studies. It is NOT a data collection system - it's a **design and configuration tool**.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    CRF Design Studio                             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    Presentation Layer (React)                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Visual     │  │  Visit Grid  │  │   Preview    │          │
│  │   Designer   │  │ Configurator │  │   & Export   │          │
│  │ (React Flow) │  │   (Editor)   │  │   (ODM/XLS)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    API Layer (NestJS/REST)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  CRF APIs    │  │ Visit Grid   │  │  Export      │          │
│  │  CRUD + Ver. │  │   APIs       │  │   APIs       │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Service Layer                                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │     CRF      │  │  Visit Grid  │  │   Export     │          │
│  │   Service    │  │   Service    │  │   Service    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Validation  │  │     Git      │  │   Import     │          │
│  │   Service    │  │   Service    │  │   Service    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Repository Layer                              │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │     CRF      │  │  Visit Grid  │  │   Template   │          │
│  │  Repository  │  │  Repository  │  │  Repository  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Data Layer                                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐           ┌──────────────────┐           │
│  │   PostgreSQL     │           │   Git Storage    │           │
│  │   (Metadata)     │           │ (CRF Versions)   │           │
│  └──────────────────┘           └──────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer (React + TypeScript)

**Purpose:** User interface for designing CRFs and configuring visit grids

**Components:**

#### Visual Designer
- Drag-and-drop form builder using React Flow
- Field palette (text, number, date, select, radio, checkbox, etc.)
- Canvas for arranging fields into sections
- Properties panel for configuring field attributes
- Real-time validation feedback

#### Visit Grid Configurator
- Matrix editor for associating CRFs with visit schedules
- Drag-and-drop CRF assignment
- Conditional logic for CRF inclusion
- Schedule timeline visualization

#### Preview & Export
- Live form preview (what data collectors would see)
- Export to ODM XML (CDISC standard)
- Export to Excel template
- Export to PDF documentation
- Version comparison view

**Technology:**
- React 18+ with TypeScript
- React Flow for visual designer
- Material-UI (MUI) components
- React Query for data fetching
- Zustand for state management
- React Hook Form for form handling

### 2. API Layer (NestJS + TypeScript)

**Purpose:** RESTful API for all operations

**Endpoints:**

#### CRF Management
```
GET    /api/crfs                    - List all CRFs
GET    /api/crfs/:id                - Get CRF details
POST   /api/crfs                    - Create new CRF
PUT    /api/crfs/:id                - Update CRF
DELETE /api/crfs/:id                - Delete CRF
GET    /api/crfs/:id/versions       - List CRF versions
POST   /api/crfs/:id/versions       - Create new version
GET    /api/crfs/:id/versions/:ver  - Get specific version
```

#### Visit Grid Management
```
GET    /api/visit-grids/:studyId    - Get visit grid for study
PUT    /api/visit-grids/:studyId    - Update visit grid
POST   /api/visit-grids/:studyId/events - Add visit event
PUT    /api/visit-grids/:studyId/events/:id - Update event
DELETE /api/visit-grids/:studyId/events/:id - Delete event
```

#### Import/Export
```
POST   /api/import/excel            - Import from Excel
POST   /api/import/odm              - Import from ODM XML
GET    /api/export/crfs/:id/odm     - Export to ODM XML
GET    /api/export/crfs/:id/excel   - Export to Excel
GET    /api/export/crfs/:id/pdf     - Export to PDF
```

**Technology:**
- NestJS framework
- TypeScript
- OpenAPI/Swagger documentation
- JWT authentication
- Rate limiting
- CORS configuration

### 3. Service Layer

**Purpose:** Business logic and orchestration

**Services:**

#### CRF Service
- CRUD operations for CRFs
- Version management
- Validation rules
- Field dependency management
- Clone/duplicate CRFs

#### Visit Grid Service
- Visit schedule management
- CRF-to-event associations
- Conditional logic evaluation
- Schedule validation

#### Export Service
- ODM XML generation (CDISC compliant)
- Excel template generation
- PDF documentation generation
- Template library management

#### Import Service
- Excel parsing and validation
- ODM XML parsing
- Data transformation
- Error reporting

#### Validation Service
- Field validation rules
- Cross-field validation
- CRF structure validation
- Business rule validation

#### Git Service
- Version control for CRF definitions
- Commit CRF changes
- Branch management
- Diff and merge support
- Version history

**Technology:**
- TypeScript classes
- Dependency injection
- Unit testable

### 4. Repository Layer

**Purpose:** Data access abstraction

**Repositories:**

#### CRF Repository
- Database operations for CRF metadata
- Query optimization
- Transaction management

#### Visit Grid Repository
- Study event definitions
- CRF associations
- Schedule data

#### Template Repository
- Template library management
- Template versioning

**Technology:**
- Prisma ORM
- Type-safe database access
- Migration support

### 5. Data Layer

**Purpose:** Persistent storage

#### PostgreSQL (Metadata Storage)
**Stores:**
- CRF definitions (name, description, status)
- CRF versions (version number, date, author)
- Sections (page structure)
- Items (field definitions)
- Item metadata (validation rules, display properties)
- Visit event definitions
- CRF-to-event associations
- User accounts and permissions
- Audit logs

**Key Tables:**
```sql
-- CRF Definition
crf (id, name, description, status, created_at, updated_at)

-- CRF Versions
crf_version (id, crf_id, version, definition_json, git_commit, created_at)

-- Visit Schedule
study_event_definition (id, study_id, name, type, ordinal, repeating)

-- CRF Association
event_definition_crf (id, study_event_id, crf_version_id, required, ordinal)

-- User Management
users (id, email, name, role, created_at)

-- Audit Trail
audit_log (id, user_id, action, entity_type, entity_id, timestamp)
```

#### Git Storage (Version Control)
**Stores:**
- CRF definitions as JSON files
- Complete version history
- Branches for feature development
- Tags for releases

**Structure:**
```
crfs/
  ├── demographics/
  │   ├── v1.0.0.json
  │   ├── v1.1.0.json
  │   └── v2.0.0.json
  ├── vital-signs/
  │   ├── v1.0.0.json
  │   └── v1.1.0.json
  └── adverse-events/
      └── v1.0.0.json
```

## Data Flow

### CRF Creation Flow
```
1. User creates CRF in Visual Designer
2. React components build CRF structure
3. API call to POST /api/crfs
4. CRF Service validates structure
5. Repository saves to PostgreSQL
6. Git Service commits JSON to Git
7. Success response with CRF ID
8. UI updates with new CRF
```

### CRF Version Flow
```
1. User modifies existing CRF
2. User clicks "Save as New Version"
3. API call to POST /api/crfs/:id/versions
4. Service validates changes
5. Repository creates new version record
6. Git Service commits with version tag
7. Success response
8. UI shows new version in history
```

### Export Flow
```
1. User selects CRF to export
2. User chooses format (ODM/Excel/PDF)
3. API call to GET /api/export/crfs/:id/odm
4. Export Service generates output
5. File streamed to browser
6. User downloads file
```

## Security Architecture

### Authentication
- JWT-based authentication
- OAuth 2.0 / OpenID Connect
- Session management
- Token refresh mechanism

### Authorization
- Role-based access control (RBAC)
- Permissions: READ, CREATE, EDIT, DELETE, EXPORT
- Study-level permissions
- CRF-level permissions

### Data Security
- HTTPS only (TLS 1.3)
- Input validation and sanitization
- SQL injection prevention (Prisma ORM)
- XSS prevention
- CSRF tokens
- Rate limiting

### Audit Trail
- All changes logged
- User, timestamp, action, entity
- Before/after values for updates
- Cannot be deleted
- Read-only access for audit review

## Performance Considerations

### Frontend Optimization
- Code splitting
- Lazy loading routes
- Memoization with useMemo/useCallback
- Virtual scrolling for large lists
- Debounced search/filter
- Optimistic UI updates

### Backend Optimization
- Database indexing
- Query optimization
- Connection pooling
- Caching with Redis
- Pagination for large datasets
- Async processing for exports

### Scalability
- Stateless API (horizontal scaling)
- Database read replicas
- CDN for static assets
- Load balancing
- Container orchestration (Kubernetes)

## Monitoring & Observability

### Application Monitoring
- Request/response times
- Error rates
- Endpoint usage
- User activity

### Infrastructure Monitoring
- CPU/Memory usage
- Database performance
- Disk space
- Network latency

### Logging
- Structured logging (JSON)
- Log aggregation (ELK stack or similar)
- Log levels (ERROR, WARN, INFO, DEBUG)
- Request tracing

### Alerting
- Error rate thresholds
- Performance degradation
- System downtime
- Security events

## Deployment Architecture

### Development Environment
```
┌─────────────┐
│  Developer  │
│  Workstation│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Docker    │
│  Compose    │
│  (Local)    │
└─────────────┘
```

### Production Environment
```
┌──────────────┐      ┌──────────────┐
│   Browser    │─────▶│     CDN      │
└──────────────┘      └──────┬───────┘
                             │
                             ▼
                      ┌──────────────┐
                      │Load Balancer │
                      └──────┬───────┘
                             │
                ┌────────────┴────────────┐
                ▼                         ▼
         ┌──────────────┐        ┌──────────────┐
         │  API Server  │        │  API Server  │
         │  (Container) │        │  (Container) │
         └──────┬───────┘        └──────┬───────┘
                │                       │
                └───────────┬───────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
         ┌──────────────┐        ┌──────────────┐
         │  PostgreSQL  │        │     Git      │
         │  (Primary)   │        │  Repository  │
         └──────┬───────┘        └──────────────┘
                │
                ▼
         ┌──────────────┐
         │  PostgreSQL  │
         │   (Replica)  │
         └──────────────┘
```

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| Frontend | React 18 + TypeScript | UI framework |
| UI Components | Material-UI (MUI) | Component library |
| Visual Designer | React Flow | Node-based designer |
| State Management | Zustand | Global state |
| Data Fetching | React Query | API calls & caching |
| Backend | NestJS + TypeScript | API framework |
| ORM | Prisma | Database access |
| Database | PostgreSQL 15+ | Metadata storage |
| Version Control | Git | CRF versioning |
| Authentication | JWT + OAuth 2.0 | User auth |
| API Docs | OpenAPI/Swagger | API documentation |
| Testing (FE) | Vitest + RTL | Unit/integration tests |
| Testing (BE) | Jest | Unit/integration tests |
| E2E Testing | Playwright | End-to-end tests |
| Containerization | Docker | Deployment |
| Orchestration | Kubernetes | Production scaling |
| CI/CD | GitHub Actions | Automation |
| Monitoring | Prometheus + Grafana | Observability |

## Design Principles

1. **Separation of Concerns**: Clear layer boundaries
2. **Single Responsibility**: Each component has one job
3. **DRY (Don't Repeat Yourself)**: Reusable components
4. **SOLID Principles**: Object-oriented best practices
5. **API-First Design**: Frontend and backend decoupled
6. **Type Safety**: TypeScript throughout
7. **Testability**: Unit, integration, and E2E tests
8. **Security by Default**: Authentication, authorization, validation
9. **Performance**: Optimized queries, caching, lazy loading
10. **Maintainability**: Clear code, documentation, patterns

## Next Steps

1. Review and approve architecture
2. Set up development environment
3. Implement database schema
4. Build API endpoints
5. Create React components
6. Integrate visual designer
7. Implement export/import
8. Add testing
9. Deploy to staging
10. Production rollout

## Related Documents

- [00-SCOPE-DEFINITION.md](./00-SCOPE-DEFINITION.md) - What we're building
- [03-DATA-MODEL.md](./03-DATA-MODEL.md) - Database schema
- [04-VISIT-GRID-INTEGRATION.md](./04-VISIT-GRID-INTEGRATION.md) - Visit grid details
- [development/ARCHITECTURE-PATTERNS.md](./development/ARCHITECTURE-PATTERNS.md) - Design patterns
- [development/API-DESIGN.md](./development/API-DESIGN.md) - API conventions
- [guides/STEP-BY-STEP-GUIDE.md](./guides/STEP-BY-STEP-GUIDE.md) - Implementation guide
