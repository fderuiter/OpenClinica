# OpenClinica CRF Builder - Migration Plan & Modernization Roadmap

## Executive Summary

This document provides a comprehensive step-by-step plan to extract the OpenClinica CRF Builder into a modern standalone application with optimal UI/UX design and contemporary technology stack.

## Recommended Technology Stack

### Backend Framework
**Primary Recommendation: Spring Boot 3.x + Java 17+**

**Why:**
- Natural evolution from existing Spring/Java codebase
- Minimal learning curve for existing developers
- Excellent REST API support
- Production-ready features (security, monitoring, etc.)
- Large ecosystem and community support
- Easy containerization (Docker)

**Alternatives:**
- **Node.js + NestJS**: If prioritizing JavaScript full-stack
- **Python + FastAPI**: If prioritizing data science integration
- **Go + Gin**: If prioritizing performance and small footprint

**Recommendation:** Spring Boot for enterprise-grade features and team familiarity

### Frontend Framework
**Primary Recommendation: React 18+ with TypeScript**

**Why:**
- Industry standard for complex web applications
- Excellent component reusability
- Large ecosystem (libraries, tools, developers)
- TypeScript adds type safety
- React Query for data fetching
- React Hook Form for form management
- Great mobile support via React Native

**Component Library:** Material-UI (MUI) or Ant Design
- Pre-built form components
- Accessibility built-in
- Professional appearance
- Extensive customization

**Alternatives:**
- **Vue 3 + TypeScript**: Gentler learning curve, great for smaller teams
- **Angular 16+**: If preferring opinionated structure
- **Svelte**: If prioritizing performance and simplicity

**Recommendation:** React + TypeScript + Material-UI for optimal developer experience and UI quality

### Database
**Primary Recommendation: PostgreSQL 15+**

**Why:**
- Already supported by OpenClinica
- JSON/JSONB support for flexible schemas
- Excellent performance
- Full ACID compliance
- Great for complex queries
- Free and open-source

**Migration Strategy:**
- Start with PostgreSQL (compatible with existing)
- Add JSON columns for flexible metadata
- Consider TimescaleDB extension for audit trails

**Alternatives:**
- **MongoDB**: If preferring document model for CRF definitions
- **MySQL 8+**: If team prefers MySQL
- **CockroachDB**: If requiring distributed database

**Recommendation:** PostgreSQL for compatibility and flexibility

### API Architecture
**Recommendation: RESTful API with GraphQL for Complex Queries**

**REST API:**
- Standard CRUD operations
- Easy to understand and implement
- Widely supported

**GraphQL (optional):**
- Complex nested queries (CRF with all sections and items)
- Reduces over-fetching
- Better for frontend flexibility

**Example:**
```
REST: GET /api/crfs/123
GraphQL: query { crf(id: 123) { name, versions { sections { items { ... } } } } }
```

### Additional Technologies

**Authentication & Authorization:**
- **OAuth 2.0 / OIDC**: Standard protocol
- **Keycloak** or **Auth0**: Identity provider
- **JWT**: Token-based auth
- **Spring Security**: Backend security

**Form Rendering Engine:**
- **React Hook Form**: Form state management
- **Formik**: Alternative form library
- **Yup / Zod**: Schema validation
- **JSON Schema**: Standard validation format

**Excel Processing:**
- **Apache POI**: Java Excel library (existing)
- **SheetJS**: JavaScript alternative
- Keep existing template parsing logic

**File Storage:**
- **MinIO**: S3-compatible object storage
- **AWS S3**: Cloud storage
- **Local filesystem**: For initial development

**API Documentation:**
- **OpenAPI 3.0 (Swagger)**: API specification
- **Swagger UI**: Interactive documentation
- **Redoc**: Alternative documentation UI

**Monitoring & Logging:**
- **Prometheus + Grafana**: Metrics and dashboards
- **ELK Stack**: Elasticsearch + Logstash + Kibana
- **Spring Boot Actuator**: Health checks and metrics

**Containerization & Deployment:**
- **Docker**: Containerization
- **Docker Compose**: Local development
- **Kubernetes**: Production orchestration
- **GitHub Actions**: CI/CD pipeline

**Testing:**
- **JUnit 5**: Backend unit tests
- **Mockito**: Mocking framework
- **Testcontainers**: Integration tests with real DB
- **Jest**: JavaScript unit tests
- **React Testing Library**: Component tests
- **Cypress**: End-to-end tests

## UI/UX Design Recommendations

### Design Principles

1. **User-Centered Design**
   - Focus on clinical researchers and data managers
   - Minimize clicks to accomplish tasks
   - Provide contextual help
   - Support keyboard shortcuts

2. **Visual Clarity**
   - Clean, uncluttered interfaces
   - Consistent color scheme
   - Clear visual hierarchy
   - Adequate white space

3. **Responsive Design**
   - Mobile-first approach
   - Tablet optimization
   - Desktop power-user features
   - Touch-friendly controls

4. **Accessibility (WCAG 2.1 Level AA)**
   - Screen reader support
   - Keyboard navigation
   - High contrast mode
   - Proper ARIA labels

5. **Performance**
   - Fast page loads (<2 seconds)
   - Instant feedback for actions
   - Progressive loading
   - Optimistic UI updates

### Key UI Components

#### 1. CRF Designer (Visual Form Builder)

**Current State:** Excel-based design
**Target State:** Web-based drag-and-drop designer

**Features:**
- **Component Palette**: Drag fields onto canvas
- **Properties Panel**: Configure field properties
- **Live Preview**: See form as users will see it
- **Section Management**: Add/reorder/nest sections
- **Validation Builder**: Visual rule creator
- **Version History**: View past versions
- **Collaboration**: Multi-user design (WebSocket)

**Mockup:**
```
┌─────────────────────────────────────────────────────────────────┐
│ CRF Designer: Demographics v2.0                    [Save] [Publish]│
├─────────────┬──────────────────────────┬─────────────────────────┤
│ Components  │  Canvas (Section 1)       │  Properties              │
│             │                           │                          │
│ ┌─────────┐│  ┌──────────────────────┐ │  Field: Age              │
│ │ Text    ││  │ [Label: Age]         │ │  ┌────────────────────┐ │
│ │ Number  ││  │ [____] years         │ │  │ Label: Age         │ │
│ │ Date    ││  │                      │ │  │ Type: Number       │ │
│ │ Select  ││  │ [Label: Gender]      │ │  │ Required: ☑        │ │
│ │ Radio   ││  │ ○ Male  ○ Female     │ │  │ Min: 0   Max: 120 │ │
│ │ Checkbox││  │                      │ │  │ Units: years       │ │
│ │ Section ││  │ [+ Add Field]        │ │  │ PHI: ☐             │ │
│ │ Group   ││  └──────────────────────┘ │  └────────────────────┘ │
│ └─────────┘│                           │  [Delete Field]          │
│             │  ┌──────────────────────┐ │                          │
│ [+ Section] │  │ Section 2            │ │                          │
│             │  │ [+ Add Field]        │ │                          │
│             │  └──────────────────────┘ │                          │
└─────────────┴──────────────────────────┴─────────────────────────┘
```

**Implementation:**
- React DnD or react-beautiful-dnd for drag-and-drop
- JSON Schema for form definition storage
- Real-time collaboration with Yjs or similar
- Canvas grid snapping for alignment

#### 2. CRF Library (Browse & Search)

**Features:**
- **Card View**: Visual CRF cards with previews
- **List View**: Compact table view
- **Filters**: By status, study, tags, date
- **Search**: Full-text search across CRF content
- **Sort**: By name, date, usage, version
- **Quick Actions**: Clone, export, archive
- **Templates**: Pre-built CRF templates library

**Mockup:**
```
┌──────────────────────────────────────────────────────────────────┐
│ CRF Library                           [Search: ___________] [🔍]  │
├──────────────────────────────────────────────────────────────────┤
│ Filters: [All Studies ▼] [Active ▼] [Sort: Name ▼]  ☰ ⊞         │
├──────────────────────────────────────────────────────────────────┤
│ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐        │
│ │ Demographics   │ │ Vital Signs    │ │ Adverse Events │        │
│ │ v2.0 • Active  │ │ v1.5 • Active  │ │ v3.1 • Draft   │        │
│ │ 245 subjects   │ │ 189 subjects   │ │ 12 subjects    │        │
│ │ Updated 2d ago │ │ Updated 5d ago │ │ Updated 1h ago │        │
│ │                │ │                │ │                │        │
│ │ [Edit] [Clone] │ │ [Edit] [Clone] │ │ [Edit] [Clone] │        │
│ └────────────────┘ └────────────────┘ └────────────────┘        │
│ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐        │
│ │ Medical Hist.  │ │ Lab Results    │ │ Concomitant Rx │        │
│ │ ...            │ │ ...            │ │ ...            │        │
└──────────────────────────────────────────────────────────────────┘
```

#### 3. Data Entry Form (Rendered CRF)

**Features:**
- **Auto-save**: Periodic saves (every 30s or on field change)
- **Progress Indicator**: Show completion percentage
- **Validation Feedback**: Inline error messages
- **Help System**: Field-level help tooltips
- **Navigation**: Section tabs or sidebar
- **Review Mode**: Read-only view with edit button
- **Responsive Layout**: Adapts to screen size
- **Offline Support**: PWA with offline data entry

**Mockup:**
```
┌──────────────────────────────────────────────────────────────────┐
│ Demographics • Subject S-001 • Screening Visit                    │
│ Progress: ████████░░ 80% Complete             [Save] [Complete]   │
├──────────────────────────────────────────────────────────────────┤
│ [Demographics] [Medical History] [Contact Info]                   │
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│ Patient Demographics                                               │
│                                                                    │
│ Date of Birth *          [MM/DD/YYYY] [📅]                        │
│                                                                    │
│ Age (calculated)         [45] years                               │
│                                                                    │
│ Gender *                 ○ Male  ● Female  ○ Other               │
│                                                                    │
│ Ethnicity                ☐ Hispanic or Latino                     │
│                          ☐ Not Hispanic or Latino                 │
│                          ☐ Unknown                                │
│                                                                    │
│ Race (select all)        ☑ White                                  │
│                          ☐ Black or African American              │
│                          ☐ Asian                                  │
│                          ☐ Native Hawaiian or Pacific Islander    │
│                          ☐ American Indian or Alaska Native       │
│                                                                    │
│                                      [< Previous] [Next Section >]│
└──────────────────────────────────────────────────────────────────┘
```

**Implementation:**
- Dynamic form rendering from JSON schema
- React Hook Form for state management
- Yup/Zod for validation
- Auto-save with debouncing
- IndexedDB for offline storage

#### 4. Study Visit Matrix (Enhanced Grid)

**Features:**
- **Real-time Updates**: WebSocket for live status changes
- **Color-Coded Status**: Visual indicators
- **Drill-down**: Click to expand inline
- **Filtering**: By subject, date, status, site
- **Export**: To Excel, PDF, CSV
- **Bulk Actions**: Schedule multiple events
- **Keyboard Navigation**: Arrow keys to move between cells
- **Responsive**: Stack columns on mobile

**Mockup:**
```
┌──────────────────────────────────────────────────────────────────┐
│ Study Visit Matrix                    [Filter] [Export] [Settings]│
├──────────────────────────────────────────────────────────────────┤
│ Subject   │ Screening │ Week 1  │ Week 4  │ Week 8  │ Week 12    │
├───────────┼───────────┼─────────┼─────────┼─────────┼────────────┤
│ S-001 ▼   │   100%    │  100%   │  75%    │  25%    │   0%       │
│           │   ✓✓✓✓    │  ✓✓✓✓   │  ✓✓✓○   │  ✓○○○   │   ○○○○     │
│           │ 01/15/23  │ 02/01   │ 03/01   │ 04/05   │ Scheduled  │
├───────────┼───────────┼─────────┼─────────┼─────────┼────────────┤
│ S-002     │   100%    │  100%   │  100%   │  50%    │   0%       │
│           │   ✓✓✓✓    │  ✓✓✓✓   │  ✓✓✓✓   │  ✓✓○○   │   ○○○○     │
│           │ 01/20/23  │ 02/10   │ 03/10   │ 04/12   │ 05/10      │
├───────────┼───────────┼─────────┼─────────┼─────────┼────────────┤
│ Expanded: S-001 • Week 4                                          │
│ CRF                  │ Status      │ Date       │ Actions         │
│ Demographics         │ Complete ✓  │ 03/01/23   │ [View] [Print]  │
│ Vital Signs          │ Complete ✓  │ 03/01/23   │ [View] [Print]  │
│ Lab Results          │ Complete ✓  │ 03/01/23   │ [View] [Print]  │
│ Adverse Events       │ Not Started │ -          │ [Start]         │
└──────────────────────────────────────────────────────────────────┘
```

**Implementation:**
- TanStack Table (React Table v8) for grid
- WebSocket for real-time updates
- Virtual scrolling for large datasets
- Canvas or SVG for custom visualizations

#### 5. Dashboard & Analytics

**Features:**
- **Study Overview**: Key metrics at a glance
- **CRF Completion Charts**: Progress visualization
- **Alerts**: Data quality issues
- **Recent Activity**: Audit trail
- **Quick Actions**: Common tasks
- **Customizable Widgets**: Drag-and-drop layout

**Mockup:**
```
┌──────────────────────────────────────────────────────────────────┐
│ Dashboard                                           [Customize]    │
├──────────────────────────────────────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│ │ Total CRFs  │ │ Active      │ │ Drafts      │ │ Archived    ││
│ │     247     │ │     189     │ │     12      │ │     46      ││
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
├──────────────────────────────────────────────────────────────────┤
│ ┌────────────────────────────┐ ┌──────────────────────────────┐ │
│ │ CRF Completion by Study    │ │ Recent Activity              │ │
│ │                            │ │                              │ │
│ │ ████████████░░ 80% Study A │ │ • Sarah edited Demographics │ │
│ │ █████████░░░░░ 60% Study B │ │   2 minutes ago              │ │
│ │ ███████████░░ 75% Study C  │ │ • John created Vitals v2.0  │ │
│ │                            │ │   1 hour ago                 │ │
│ │                            │ │ • Mary completed 5 CRFs     │ │
│ │                            │ │   3 hours ago                │ │
│ └────────────────────────────┘ └──────────────────────────────┘ │
├──────────────────────────────────────────────────────────────────┤
│ ┌───────────────────────────────────────────────────────────────┐│
│ │ Quick Actions                                                 ││
│ │ [Create New CRF] [Upload Template] [View Reports] [Settings] ││
│ └───────────────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

**Implementation:**
- Recharts or Chart.js for visualizations
- react-grid-layout for customizable dashboard
- React Query for data fetching
- Zustand or Redux for state management

### Design System

**Color Palette:**
- Primary: #1976D2 (Blue) - Actions, links
- Secondary: #388E3C (Green) - Success, completion
- Warning: #F57C00 (Orange) - In progress, warnings
- Error: #D32F2F (Red) - Errors, required fields
- Info: #0288D1 (Light Blue) - Information
- Gray Scale: #FAFAFA to #212121 - Backgrounds, text

**Typography:**
- Headings: Roboto Bold
- Body: Roboto Regular
- Monospace: Roboto Mono (for IDs, codes)
- Base size: 16px
- Line height: 1.5

**Spacing:**
- Base unit: 8px
- Small: 8px, Medium: 16px, Large: 24px, XL: 32px

**Components:**
- Material-UI components
- Custom theming
- Consistent styling across app

## Step-by-Step Migration Plan

### Phase 0: Preparation (Weeks 1-2)

**Week 1: Analysis & Documentation**
- [ ] Complete this documentation review
- [ ] Stakeholder interviews (identify must-have features)
- [ ] Create detailed feature prioritization matrix
- [ ] Define success criteria and KPIs
- [ ] Review existing CRF templates and identify patterns
- [ ] Document data migration requirements
- [ ] Identify integration points with other systems

**Week 2: Technical Setup**
- [ ] Set up development environment
  - [ ] Install Node.js 18+, Java 17+
  - [ ] Set up IDE (VS Code, IntelliJ)
  - [ ] Configure Docker Desktop
- [ ] Create Git repository structure
  - [ ] Backend repo (Spring Boot)
  - [ ] Frontend repo (React)
  - [ ] Monorepo alternative (Nx, Turborepo)
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Create project board (GitHub Projects, Jira)
- [ ] Define coding standards and conventions
- [ ] Set up testing framework
- [ ] Create initial architecture diagrams

### Phase 1: Foundation (Weeks 3-6)

**Week 3: Backend Foundation**
- [ ] Initialize Spring Boot 3.x project
  - [ ] Add dependencies (Spring Web, Spring Data JPA, Spring Security)
  - [ ] Configure PostgreSQL connection
  - [ ] Set up Liquibase/Flyway for schema migrations
- [ ] Create core domain models
  - [ ] CRF entity
  - [ ] CRFVersion entity
  - [ ] Section entity
  - [ ] Item entity
  - [ ] ItemFormMetadata entity
  - [ ] ItemGroup entity
- [ ] Set up JPA repositories
- [ ] Configure Spring Security (JWT-based)
- [ ] Set up exception handling
- [ ] Configure logging (SLF4J + Logback)
- [ ] Write unit tests for domain models

**Week 4: Frontend Foundation**
- [ ] Initialize React 18 + TypeScript project (Vite)
  - [ ] Configure ESLint, Prettier
  - [ ] Set up folder structure
  - [ ] Add Material-UI
  - [ ] Configure React Router
- [ ] Set up authentication flow
  - [ ] Login page
  - [ ] JWT storage and refresh
  - [ ] Protected routes
- [ ] Create layout components
  - [ ] Header with navigation
  - [ ] Sidebar menu
  - [ ] Footer
  - [ ] Page container
- [ ] Set up React Query for data fetching
- [ ] Configure Axios for API calls
- [ ] Create theme and design system
- [ ] Set up Storybook for component development

**Week 5: API Development**
- [ ] Design REST API structure
  - [ ] Define API contracts (OpenAPI spec)
  - [ ] Version API (v1)
- [ ] Implement CRF endpoints
  - [ ] POST /api/v1/crfs - Create CRF
  - [ ] GET /api/v1/crfs - List CRFs
  - [ ] GET /api/v1/crfs/{id} - Get CRF details
  - [ ] PUT /api/v1/crfs/{id} - Update CRF
  - [ ] DELETE /api/v1/crfs/{id} - Delete CRF
- [ ] Implement CRF Version endpoints
  - [ ] POST /api/v1/crfs/{id}/versions - Create version
  - [ ] GET /api/v1/crfs/{id}/versions - List versions
  - [ ] GET /api/v1/versions/{id} - Get version details
- [ ] Add pagination, sorting, filtering
- [ ] Write integration tests (Testcontainers)
- [ ] Generate API documentation (Swagger UI)

**Week 6: Data Migration Preparation**
- [ ] Analyze OpenClinica database schema
- [ ] Create data extraction scripts
  - [ ] Export CRF definitions to JSON
  - [ ] Export CRF versions to JSON
  - [ ] Export sections and items
- [ ] Create import service in new system
- [ ] Test data migration with sample data
- [ ] Document migration procedure
- [ ] Create rollback plan

### Phase 2: Core CRF Management (Weeks 7-10)

**Week 7: CRF CRUD Operations**
- [ ] Build CRF list page
  - [ ] Table with search and filter
  - [ ] Pagination
  - [ ] Sort by name, date, status
  - [ ] Quick actions (edit, clone, delete)
- [ ] Build CRF create/edit form
  - [ ] Name, description fields
  - [ ] Study association
  - [ ] Validation
  - [ ] Save/cancel actions
- [ ] Build CRF detail view
  - [ ] Display all CRF information
  - [ ] List versions
  - [ ] Action buttons
- [ ] Implement delete confirmation
- [ ] Add loading states and error handling
- [ ] Write E2E tests (Cypress)

**Week 8: Excel Template Upload**
- [ ] Port SpreadsheetPreview logic to new system
  - [ ] Parse Excel file (Apache POI)
  - [ ] Extract CRF metadata
  - [ ] Extract sections
  - [ ] Extract items and metadata
  - [ ] Extract item groups
- [ ] Build upload UI
  - [ ] Drag-and-drop file upload
  - [ ] Progress indicator
  - [ ] Preview parsed data
  - [ ] Validation error display
- [ ] Implement validation rules
  - [ ] Required fields check
  - [ ] Data type validation
  - [ ] Unique name constraints
- [ ] Add confirmation step
- [ ] Handle upload errors gracefully
- [ ] Write tests for parser

**Week 9: Section & Item Management**
- [ ] Build section list component
  - [ ] Display sections in order
  - [ ] Expand/collapse sections
  - [ ] Drag to reorder
- [ ] Build section create/edit form
  - [ ] Label, title, subtitle
  - [ ] Instructions
  - [ ] Page number
- [ ] Build item list component
  - [ ] Display items in section
  - [ ] Show item properties
  - [ ] Drag to reorder
- [ ] Build item create/edit form
  - [ ] All item properties
  - [ ] Response type selector
  - [ ] Validation rules
- [ ] Implement item metadata editor
  - [ ] Left/right text
  - [ ] Required checkbox
  - [ ] Default value
  - [ ] Validation pattern
- [ ] Write component tests

**Week 10: Version Management**
- [ ] Implement version creation
  - [ ] Clone from previous version
  - [ ] Upload new template
- [ ] Build version comparison tool
  - [ ] Side-by-side diff
  - [ ] Highlight changes
- [ ] Implement version locking
  - [ ] Lock version (prevent edits)
  - [ ] Unlock version
- [ ] Build version history view
  - [ ] Timeline of versions
  - [ ] Revision notes
  - [ ] Change author and date
- [ ] Implement version deletion
  - [ ] Soft delete
  - [ ] Prevent delete if data exists
- [ ] Add version status indicators

### Phase 3: Advanced Features (Weeks 11-14)

**Week 11: Item Groups (Repeating Data)**
- [ ] Implement item group models
- [ ] Build group create/edit form
  - [ ] Group name
  - [ ] Layout (horizontal/vertical/grid)
  - [ ] Repeat configuration
- [ ] Build group item management
  - [ ] Add items to group
  - [ ] Reorder items in group
  - [ ] Remove items from group
- [ ] Implement group metadata
  - [ ] Header, subheader
  - [ ] Repeat number, max
  - [ ] Row labels
- [ ] Test repeating group rendering
- [ ] Write tests

**Week 12: Validation Engine**
- [ ] Implement validation rule types
  - [ ] Required field
  - [ ] Data type validation
  - [ ] Range validation (min/max)
  - [ ] Regular expression
  - [ ] Custom functions
- [ ] Build validation rule editor UI
  - [ ] Visual rule builder
  - [ ] Test validation tool
- [ ] Implement server-side validation
- [ ] Implement client-side validation
- [ ] Add validation error messages
- [ ] Test all validation types

**Week 13: Conditional Display**
- [ ] Implement conditional logic engine
  - [ ] Show/hide based on other fields
  - [ ] Enable/disable fields
  - [ ] Change options dynamically
- [ ] Build conditional logic editor
  - [ ] Visual editor
  - [ ] Condition builder
  - [ ] Action selector
- [ ] Test conditional logic
- [ ] Handle edge cases
- [ ] Write tests

**Week 14: Calculations**
- [ ] Implement calculation engine
  - [ ] Basic arithmetic (+, -, *, /)
  - [ ] Functions (SUM, AVG, MIN, MAX)
  - [ ] Date calculations
  - [ ] Conditional calculations
- [ ] Build calculation editor
  - [ ] Formula builder
  - [ ] Function selector
  - [ ] Test calculator
- [ ] Implement instant calculations
- [ ] Test all calculation types
- [ ] Write tests

### Phase 4: Form Rendering & Data Entry (Weeks 15-18)

**Week 15: Form Rendering Engine**
- [ ] Design form rendering architecture
  - [ ] JSON schema for form definition
  - [ ] Component mapping
- [ ] Build form renderer
  - [ ] Dynamic component generation
  - [ ] Section rendering
  - [ ] Item rendering
  - [ ] Group rendering
- [ ] Implement all response types
  - [ ] Text input
  - [ ] Textarea
  - [ ] Select dropdown
  - [ ] Radio buttons
  - [ ] Checkboxes
  - [ ] Date picker
  - [ ] File upload
  - [ ] Calculation display
- [ ] Apply styling and layout
- [ ] Test rendering with various CRFs

**Week 16: Data Entry Features**
- [ ] Implement form state management
  - [ ] React Hook Form integration
  - [ ] Field registration
  - [ ] Value tracking
- [ ] Add auto-save functionality
  - [ ] Debounced save (30s)
  - [ ] Save on field blur
  - [ ] Save indicator
- [ ] Implement validation feedback
  - [ ] Inline error messages
  - [ ] Field highlighting
  - [ ] Error summary
- [ ] Add progress tracking
  - [ ] Completion percentage
  - [ ] Required fields counter
- [ ] Implement save/submit actions
  - [ ] Save draft
  - [ ] Mark complete
  - [ ] Submit for review
- [ ] Write E2E tests

**Week 17: Data Storage**
- [ ] Create data models
  - [ ] EventCRF entity
  - [ ] ItemData entity
- [ ] Implement data endpoints
  - [ ] POST /api/v1/event-crfs - Create instance
  - [ ] GET /api/v1/event-crfs/{id} - Get data
  - [ ] PUT /api/v1/event-crfs/{id}/data - Save data
  - [ ] PATCH /api/v1/event-crfs/{id}/complete - Mark complete
- [ ] Handle repeating group data
  - [ ] Array storage
  - [ ] Ordinal tracking
- [ ] Implement data validation
  - [ ] Required field checks
  - [ ] Data type validation
  - [ ] Business rule validation
- [ ] Write integration tests

**Week 18: Offline Support**
- [ ] Implement Progressive Web App (PWA)
  - [ ] Service worker
  - [ ] Offline manifest
  - [ ] Install prompts
- [ ] Add offline data storage
  - [ ] IndexedDB integration
  - [ ] Local data cache
- [ ] Implement sync mechanism
  - [ ] Detect online/offline
  - [ ] Queue offline changes
  - [ ] Sync when online
  - [ ] Conflict resolution
- [ ] Add offline indicator UI
- [ ] Test offline functionality
- [ ] Write tests

### Phase 5: Export & Import (Weeks 19-21)

**Week 19: Excel Export**
- [ ] Implement template download
  - [ ] Generate Excel file from CRF definition
  - [ ] Include all metadata
  - [ ] Format correctly
- [ ] Add template download endpoint
  - [ ] GET /api/v1/crfs/{id}/versions/{versionId}/template
- [ ] Build download UI
  - [ ] Download button
  - [ ] Progress indicator
- [ ] Test with various CRFs
- [ ] Handle large templates

**Week 20: ODM Export**
- [ ] Port MetaDataCollector logic
  - [ ] Generate CDISC ODM 1.3 XML
  - [ ] Include all CRF metadata
  - [ ] Include item definitions
  - [ ] Include code lists
- [ ] Implement ODM endpoints
  - [ ] GET /api/v1/studies/{id}/metadata/odm
  - [ ] Support XML and JSON formats
- [ ] Build export UI
  - [ ] Export options
  - [ ] Format selection
  - [ ] Download button
- [ ] Validate ODM compliance
- [ ] Test with ODM validators

**Week 21: Data Import**
- [ ] Implement data import from ODM
  - [ ] Parse ODM XML
  - [ ] Map to internal structure
  - [ ] Validate data
- [ ] Build import UI
  - [ ] File upload
  - [ ] Validation results
  - [ ] Confirmation
- [ ] Handle import errors
  - [ ] Show detailed errors
  - [ ] Partial import option
- [ ] Test with various ODM files
- [ ] Write tests

### Phase 6: Visit Grid Integration (Weeks 22-24)

**Week 22: Study Event Models**
- [ ] Create study event models
  - [ ] StudyEventDefinition
  - [ ] StudyEvent
  - [ ] EventDefinitionCRF
- [ ] Implement event endpoints
  - [ ] CRUD operations for events
  - [ ] Associate CRFs with events
- [ ] Build event management UI
  - [ ] List events
  - [ ] Create/edit events
  - [ ] Add/remove CRFs from events
- [ ] Test event management
- [ ] Write tests

**Week 23: Visit Grid Display**
- [ ] Build grid component
  - [ ] TanStack Table integration
  - [ ] Subjects as rows
  - [ ] Events as columns
  - [ ] Status indicators
- [ ] Implement data loading
  - [ ] Fetch all subjects
  - [ ] Fetch all events
  - [ ] Calculate completion status
- [ ] Add filtering
  - [ ] By subject
  - [ ] By date range
  - [ ] By status
- [ ] Add sorting
- [ ] Implement cell click
  - [ ] Show CRF list for event
  - [ ] Navigate to data entry
- [ ] Style grid
- [ ] Test with large datasets

**Week 24: Real-time Updates**
- [ ] Set up WebSocket server
  - [ ] Spring WebSocket
  - [ ] STOMP protocol
- [ ] Implement client WebSocket
  - [ ] Connect on grid load
  - [ ] Subscribe to updates
- [ ] Send status updates
  - [ ] On data save
  - [ ] On completion
  - [ ] On event changes
- [ ] Update grid in real-time
  - [ ] Update cell status
  - [ ] Show notifications
- [ ] Test real-time updates
- [ ] Handle connection errors

### Phase 7: User Management & Security (Weeks 25-27)

**Week 25: Authentication**
- [ ] Integrate with identity provider
  - [ ] Keycloak or Auth0
  - [ ] OAuth 2.0 / OIDC
- [ ] Implement login flow
  - [ ] Redirect to IdP
  - [ ] Handle callback
  - [ ] Store JWT token
- [ ] Implement logout
- [ ] Add token refresh
- [ ] Implement session timeout
- [ ] Test authentication flow

**Week 26: Authorization**
- [ ] Define roles
  - [ ] System Administrator
  - [ ] Study Designer
  - [ ] Data Manager
  - [ ] Monitor
  - [ ] View Only
- [ ] Implement role-based access control
  - [ ] Backend enforcement
  - [ ] Frontend route guards
  - [ ] UI element visibility
- [ ] Add permission checks
  - [ ] Create CRF
  - [ ] Edit CRF
  - [ ] Delete CRF
  - [ ] View data
  - [ ] Edit data
- [ ] Test authorization
- [ ] Write tests

**Week 27: Audit Trail**
- [ ] Create audit log model
  - [ ] Timestamp
  - [ ] User
  - [ ] Action
  - [ ] Entity type and ID
  - [ ] Old/new values
- [ ] Implement audit logging
  - [ ] AOP interceptor
  - [ ] Log all changes
  - [ ] Store in database
- [ ] Build audit log viewer
  - [ ] List all changes
  - [ ] Filter by entity, user, date
  - [ ] Show details
- [ ] Test audit logging
- [ ] Write tests

### Phase 8: Testing & Quality Assurance (Weeks 28-30)

**Week 28: Comprehensive Testing**
- [ ] Unit test coverage > 80%
  - [ ] Backend services
  - [ ] Frontend components
- [ ] Integration test coverage
  - [ ] API endpoints
  - [ ] Database operations
- [ ] E2E test coverage
  - [ ] Critical user flows
  - [ ] CRF creation
  - [ ] Data entry
  - [ ] Export
- [ ] Performance testing
  - [ ] Load testing (JMeter)
  - [ ] Stress testing
  - [ ] Identify bottlenecks
- [ ] Security testing
  - [ ] OWASP top 10
  - [ ] Penetration testing
  - [ ] Vulnerability scanning

**Week 29: Bug Fixing & Optimization**
- [ ] Fix all critical bugs
- [ ] Fix high priority bugs
- [ ] Optimize slow queries
  - [ ] Add indexes
  - [ ] Optimize joins
- [ ] Optimize frontend performance
  - [ ] Code splitting
  - [ ] Lazy loading
  - [ ] Image optimization
- [ ] Reduce bundle size
- [ ] Improve loading times

**Week 30: User Acceptance Testing**
- [ ] Deploy to staging environment
- [ ] Conduct UAT with stakeholders
  - [ ] Clinical researchers
  - [ ] Data managers
  - [ ] System administrators
- [ ] Gather feedback
- [ ] Prioritize changes
- [ ] Implement high-priority changes
- [ ] Retest

### Phase 9: Deployment & Migration (Weeks 31-33)

**Week 31: Production Environment Setup**
- [ ] Set up production infrastructure
  - [ ] Kubernetes cluster
  - [ ] Database (PostgreSQL)
  - [ ] Object storage (MinIO/S3)
  - [ ] Load balancer
- [ ] Configure monitoring
  - [ ] Prometheus metrics
  - [ ] Grafana dashboards
  - [ ] Alert rules
- [ ] Configure logging
  - [ ] ELK stack
  - [ ] Log aggregation
- [ ] Set up backups
  - [ ] Database backups
  - [ ] File backups
  - [ ] Backup schedule
- [ ] Configure CI/CD for production
- [ ] Set up DNS and SSL certificates

**Week 32: Data Migration**
- [ ] Perform dry run migration
  - [ ] Export from OpenClinica
  - [ ] Import to new system
  - [ ] Validate data integrity
- [ ] Identify and fix issues
- [ ] Create migration scripts
- [ ] Test migration multiple times
- [ ] Create migration runbook
- [ ] Schedule migration window

**Week 33: Production Deployment**
- [ ] Execute data migration
  - [ ] Export production data
  - [ ] Import to new system
  - [ ] Validate all data
- [ ] Deploy application to production
  - [ ] Backend services
  - [ ] Frontend application
  - [ ] Database migrations
- [ ] Verify deployment
  - [ ] Health checks
  - [ ] Smoke tests
- [ ] Monitor system
  - [ ] Check metrics
  - [ ] Watch for errors
- [ ] Communicate with users
- [ ] Provide support

### Phase 10: Post-Deployment (Weeks 34-36)

**Week 34: Monitoring & Support**
- [ ] Monitor production metrics
  - [ ] Response times
  - [ ] Error rates
  - [ ] Resource usage
- [ ] Address production issues
  - [ ] Fix bugs
  - [ ] Performance tuning
- [ ] Gather user feedback
- [ ] Create support documentation
- [ ] Provide training
  - [ ] User guides
  - [ ] Video tutorials
  - [ ] Live training sessions

**Week 35: Enhancement Planning**
- [ ] Review user feedback
- [ ] Prioritize enhancements
- [ ] Plan next release
  - [ ] New features
  - [ ] Improvements
  - [ ] Bug fixes
- [ ] Update roadmap
- [ ] Communicate plans to stakeholders

**Week 36: Documentation & Handoff**
- [ ] Complete technical documentation
  - [ ] Architecture docs
  - [ ] API documentation
  - [ ] Database schema
  - [ ] Deployment guide
- [ ] Complete user documentation
  - [ ] User manual
  - [ ] Administrator guide
  - [ ] FAQ
- [ ] Create training materials
- [ ] Conduct knowledge transfer
- [ ] Hand off to operations team

## Total Timeline: 36 Weeks (9 Months)

### Team Composition
- **2 Backend Developers** (Java/Spring Boot)
- **2 Frontend Developers** (React/TypeScript)
- **1 Full-Stack Developer** (Backend/Frontend)
- **1 DevOps Engineer** (Infrastructure, CI/CD)
- **1 QA Engineer** (Testing)
- **1 UX Designer** (UI/UX design)
- **1 Product Manager** (Requirements, prioritization)
- **1 Project Manager** (Timeline, coordination)

**Total: 10 people for 9 months**

### Risk Mitigation

**Risk 1: Data Migration Complexity**
- Mitigation: Start early, test thoroughly, have rollback plan

**Risk 2: Feature Creep**
- Mitigation: Strict prioritization, MVP first, enhancements later

**Risk 3: Performance Issues**
- Mitigation: Performance testing early, optimize continuously

**Risk 4: User Adoption**
- Mitigation: Involve users early, provide training, gradual rollout

**Risk 5: Integration Challenges**
- Mitigation: Define APIs early, test integrations continuously

### Success Criteria

**Technical:**
- [ ] All CRFs migrated successfully (100%)
- [ ] System uptime > 99.9%
- [ ] Page load time < 2 seconds
- [ ] API response time < 500ms (95th percentile)
- [ ] Zero data loss

**User:**
- [ ] User satisfaction score > 4/5
- [ ] Task completion rate > 90%
- [ ] Time to create CRF reduced by 50%
- [ ] Training time reduced by 30%
- [ ] Support tickets < 10 per month

**Business:**
- [ ] Deployment on schedule
- [ ] Within budget
- [ ] All critical features delivered
- [ ] Zero critical bugs in production
- [ ] Positive ROI within 12 months

## Maintenance & Operations

### Ongoing Tasks
- [ ] Monthly security updates
- [ ] Weekly database backups
- [ ] Daily monitoring and alerts
- [ ] Quarterly performance reviews
- [ ] Bi-annual disaster recovery drills
- [ ] Annual security audits

### Support Model
- **Tier 1**: Help desk (user questions)
- **Tier 2**: Application support (bug fixes)
- **Tier 3**: Development team (complex issues)
- **SLA**: 4 hours for critical, 24 hours for high, 72 hours for medium

## Conclusion

This comprehensive plan provides a structured approach to extracting and modernizing the OpenClinica CRF Builder into a standalone application. The recommended technology stack (Spring Boot + React + PostgreSQL) balances enterprise requirements with modern development practices. The phased approach ensures steady progress with frequent deliverables and feedback loops.

Key success factors:
1. **Strong team** with right skills
2. **Clear requirements** with prioritization
3. **Iterative development** with frequent testing
4. **User involvement** throughout process
5. **Comprehensive testing** at all levels
6. **Careful data migration** with validation
7. **Excellent documentation** for sustainability

With this plan, you can build a modern, user-friendly, high-performance CRF Builder that exceeds the capabilities of the original OpenClinica system while maintaining compatibility and data integrity.
