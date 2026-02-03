# CRF Design Studio - Implementation Roadmap

## Executive Summary

Focused plan to build a standalone **CRF Design Studio** for designing forms and configuring visit schedules. This is **NOT** a data collection system.

**Timeline:** 24 weeks (6 months)
**Team:** 5-6 people
**Budget:** ~$320,000

## Scope Reminder

### ✅ Building
- Visual CRF designer
- Visit grid configurator
- Form preview
- Excel import/export
- ODM XML export

### ❌ NOT Building
- Data collection forms
- Subject enrollment
- Clinical workflows
- Data storage (actual patient data)
- Monitoring/reporting

## Recommended Technology Stack

```yaml
Frontend:
  - React 18 + TypeScript + Vite
  - React Flow (visual designer)
  - dnd-kit (drag-and-drop)
  - Material-UI (components)
  - Zustand (state) + TanStack Query

Backend:
  - Node.js 20 + NestJS + TypeScript
  - PostgreSQL 15 with JSONB
  - Prisma ORM

Tools:
  - ExcelJS (Excel parsing)
  - fast-xml-parser (ODM export)
  - Playwright (E2E testing)
```

## 6-Month Implementation Plan

### Month 1: Foundation & Setup

#### Week 1-2: Project Setup
- [ ] **Day 1-2: Infrastructure**
  - [ ] Create GitHub repository
  - [ ] Set up project structure (monorepo with Nx or separate repos)
  - [ ] Configure development environment
  - [ ] Set up Docker Compose for local dev
  - [ ] Initialize PostgreSQL database

- [ ] **Day 3-5: Backend Foundation**
  - [ ] Initialize NestJS project
  - [ ] Configure TypeScript strict mode
  - [ ] Set up Prisma ORM
  - [ ] Create database schema (simplified for design tool):
    ```prisma
    model CRF {
      id          String   @id @default(uuid())
      name        String
      description String?
      status      String   @default("draft")
      definition  Json     // Complete CRF definition as JSON
      versions    CRFVersion[]
      createdAt   DateTime @default(now())
      updatedAt   DateTime @updatedAt
    }
    
    model CRFVersion {
      id          String   @id @default(uuid())
      crfId       String
      crf         CRF      @relation(fields: [crfId], references: [id])
      version     String
      definition  Json     // Versioned CRF definition
      createdAt   DateTime @default(now())
    }
    
    model StudyTemplate {
      id          String   @id @default(uuid())
      name        String
      visitConfig Json     // Complete visit grid configuration
      createdAt   DateTime @default(now())
    }
    ```
  - [ ] Create basic REST API structure
  - [ ] Set up authentication (JWT-based, simple)

- [ ] **Day 6-10: Frontend Foundation**
  - [ ] Initialize React + Vite + TypeScript
  - [ ] Install dependencies: React Flow, MUI, Zustand, TanStack Query
  - [ ] Set up project structure:
    ```
    src/
    ├── app/             # Main app setup
    ├── components/      # Reusable components
    ├── features/        # Feature modules
    │   ├── designer/    # CRF designer
    │   ├── library/     # CRF library
    │   └── visitgrid/   # Visit grid config
    ├── lib/             # Utilities
    ├── types/           # TypeScript types
    └── api/             # API client
    ```
  - [ ] Configure routing (React Router)
  - [ ] Set up authentication flow
  - [ ] Create layout components (header, sidebar, container)
  - [ ] Set up design system (MUI theme)

#### Week 3-4: Core Data Models & APIs
- [ ] **Backend APIs**
  - [ ] POST /api/crfs - Create CRF
  - [ ] GET /api/crfs - List CRFs
  - [ ] GET /api/crfs/:id - Get CRF
  - [ ] PUT /api/crfs/:id - Update CRF
  - [ ] DELETE /api/crfs/:id - Delete CRF
  - [ ] POST /api/crfs/:id/versions - Create version
  - [ ] GET /api/crfs/:id/versions - List versions

- [ ] **Type Definitions (Shared)**
  ```typescript
  // types/crf.types.ts
  export interface CRFDefinition {
    metadata: {
      name: string;
      description: string;
      version: string;
    };
    sections: Section[];
  }
  
  export interface Section {
    id: string;
    label: string;
    title: string;
    items: Item[];
  }
  
  export interface Item {
    id: string;
    name: string;
    label: string;
    type: ItemType;
    validation?: ValidationRule[];
    required: boolean;
    properties: Record<string, any>;
  }
  
  export type ItemType = 
    | 'text' 
    | 'number' 
    | 'date' 
    | 'select' 
    | 'radio' 
    | 'checkbox'
    | 'textarea';
  ```

- [ ] **Frontend Data Layer**
  - [ ] Set up TanStack Query
  - [ ] Create API client hooks
  - [ ] Implement optimistic updates
  - [ ] Add error handling

- [ ] **CRF Library UI**
  - [ ] Create CRF list page
  - [ ] Add search and filtering
  - [ ] Implement card/list view toggle
  - [ ] Create CRF detail view

### Month 2: Visual Designer Foundation

#### Week 5-6: React Flow Integration
- [ ] **Basic Designer Setup**
  - [ ] Integrate React Flow
  - [ ] Create custom node types for fields:
    ```typescript
    // TextFieldNode.tsx
    const TextFieldNode = ({ data, id }) => (
      <div className="field-node">
        <Handle type="target" position="top" />
        <div className="node-content">
          <Typography variant="caption">{data.label}</Typography>
          <TextField size="small" placeholder={data.placeholder} />
        </div>
        <Handle type="source" position="bottom" />
      </div>
    );
    ```
  - [ ] Create field types: text, number, date, select, radio, checkbox, textarea
  - [ ] Implement node selection and editing
  - [ ] Add node deletion

- [ ] **Component Palette**
  - [ ] Create draggable field palette
  - [ ] Implement drag from palette to canvas
  - [ ] Add field type icons
  - [ ] Group fields by category

- [ ] **Canvas Operations**
  - [ ] Zoom controls
  - [ ] Pan canvas
  - [ ] Grid snapping
  - [ ] Undo/redo (basic)
  - [ ] Auto-layout options

#### Week 7-8: Property Panel & Field Configuration
- [ ] **Property Panel**
  - [ ] Create property editor sidebar
  - [ ] Display selected field properties
  - [ ] Implement property editing:
    - [ ] Field name/label
    - [ ] Required checkbox
    - [ ] Placeholder text
    - [ ] Help text
    - [ ] Default value

- [ ] **Field-Specific Properties**
  - [ ] Text: max length, pattern
  - [ ] Number: min, max, decimal places
  - [ ] Date: min/max dates, format
  - [ ] Select: options editor
  - [ ] Radio: options editor
  - [ ] Checkbox: options editor

- [ ] **Validation Rules**
  - [ ] Add validation rule builder
  - [ ] Support regex patterns
  - [ ] Range validation (min/max)
  - [ ] Custom validation messages

### Month 3: Advanced Designer Features

#### Week 9-10: Sections & Organization
- [ ] **Section Management**
  - [ ] Add section concept to designer
  - [ ] Create section node type
  - [ ] Drag fields into sections
  - [ ] Reorder sections
  - [ ] Section properties (title, instructions)

- [ ] **Designer Enhancements**
  - [ ] Field duplication
  - [ ] Bulk field operations
  - [ ] Field search/filter in canvas
  - [ ] Keyboard shortcuts
  - [ ] Mini-map navigation

- [ ] **Templates**
  - [ ] Create field templates
  - [ ] Save custom components
  - [ ] Template library
  - [ ] Drag template fields to canvas

#### Week 11-12: Conditional Logic & Calculations
- [ ] **Conditional Display**
  - [ ] Visual rule builder
  - [ ] Show/hide based on field values
  - [ ] Enable/disable fields
  - [ ] Condition types:
    - [ ] Equals
    - [ ] Not equals
    - [ ] Contains
    - [ ] Greater than / Less than
    - [ ] Is empty / Is not empty

- [ ] **Calculations**
  - [ ] Formula editor
  - [ ] Basic operations (+, -, *, /)
  - [ ] Functions (SUM, AVG, MIN, MAX)
  - [ ] Field references
  - [ ] Calculation preview

- [ ] **Repeating Groups**
  - [ ] Create group node type
  - [ ] Add fields to group
  - [ ] Configure repeat settings:
    - [ ] Initial rows
    - [ ] Max rows
    - [ ] Add/remove row buttons
  - [ ] Group layout options (horizontal/vertical)

### Month 4: Preview & Import/Export

#### Week 13-14: Form Preview
- [ ] **Live Preview**
  - [ ] Create preview mode
  - [ ] Render form from definition
  - [ ] Match final appearance
  - [ ] Test interactions
  - [ ] Show validation in action

- [ ] **Preview Features**
  - [ ] Switch between edit/preview modes
  - [ ] Mobile preview
  - [ ] Tablet preview
  - [ ] Print preview
  - [ ] Sample data population

#### Week 15-16: Excel Import/Export
- [ ] **Excel Parser**
  - [ ] Port existing SpreadsheetPreview logic to TypeScript
  - [ ] Parse Excel template:
    ```typescript
    // excelParser.service.ts
    export class ExcelParserService {
      async parseCRFTemplate(file: Buffer): Promise<CRFDefinition> {
        const workbook = XLSX.read(file);
        
        // Parse CRF sheet
        const crfMetadata = this.parseCRFSheet(workbook);
        
        // Parse Sections sheet
        const sections = this.parseSectionsSheet(workbook);
        
        // Parse Items sheet
        const items = this.parseItemsSheet(workbook);
        
        // Parse Groups sheet (if exists)
        const groups = this.parseGroupsSheet(workbook);
        
        return this.buildCRFDefinition(crfMetadata, sections, items, groups);
      }
    }
    ```
  - [ ] Validate template structure
  - [ ] Show validation errors
  - [ ] Preview parsed CRF

- [ ] **Excel Export**
  - [ ] Generate Excel from CRF definition
  - [ ] Create sheets: CRF, Sections, Items, Groups
  - [ ] Format correctly
  - [ ] Download functionality

- [ ] **Import UI**
  - [ ] Drag-and-drop file upload
  - [ ] File validation
  - [ ] Preview before import
  - [ ] Import confirmation
  - [ ] Handle errors gracefully

### Month 5: Visit Grid & ODM Export

#### Week 17-18: Visit Grid Configuration
- [ ] **Visit Grid UI**
  - [ ] Create visit grid configurator
  - [ ] List view of visits (study events)
  - [ ] Add/edit/delete visits
  - [ ] Drag to reorder visits

- [ ] **CRF-Visit Association**
  - [ ] Associate CRFs with visits
  - [ ] Set CRF as required/optional
  - [ ] Select default CRF version
  - [ ] Reorder CRFs within visit

- [ ] **Visit Grid Preview**
  - [ ] Visual matrix view (like spreadsheet)
  - [ ] Visits as columns
  - [ ] CRFs as rows (or nested)
  - [ ] Show configuration visually

- [ ] **Export Configuration**
  - [ ] Export visit grid as JSON
  - [ ] Export as Excel
  - [ ] Include all associations

#### Week 19-20: ODM Export
- [ ] **ODM XML Generator**
  - [ ] Implement CDISC ODM 1.3 generator
  - [ ] Generate metadata section:
    ```typescript
    // odmExporter.service.ts
    export class ODMExporterService {
      generateODM(crf: CRFDefinition, visitGrid?: VisitConfig): string {
        const odmDoc = {
          ODM: {
            $: {
              xmlns: 'http://www.cdisc.org/ns/odm/v1.3',
              'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
              ODMVersion: '1.3.2',
              FileType: 'Snapshot',
            },
            Study: this.generateStudyElement(crf, visitGrid),
          },
        };
        
        return builder.buildObject(odmDoc);
      }
      
      private generateStudyElement(crf: CRFDefinition, visitGrid?: VisitConfig) {
        return {
          GlobalVariables: {...},
          MetaDataVersion: {
            FormDef: this.generateFormDef(crf),
            ItemGroupDef: this.generateItemGroups(crf),
            ItemDef: this.generateItems(crf),
            CodeList: this.generateCodeLists(crf),
          },
        };
      }
    }
    ```
  - [ ] Generate FormDef, ItemGroupDef, ItemDef
  - [ ] Generate CodeLists for select/radio fields
  - [ ] Include validation rules
  - [ ] Validate against ODM schema

- [ ] **Export UI**
  - [ ] Select CRFs to export
  - [ ] Choose format (ODM XML, Excel, JSON)
  - [ ] Include visit grid configuration
  - [ ] Download or copy to clipboard
  - [ ] Validation report

- [ ] **JSON Schema Export**
  - [ ] Generate JSON Schema from CRF
  - [ ] For modern API integrations
  - [ ] Include validation rules
  - [ ] Documentation generation

### Month 6: Polish, Testing & Deployment

#### Week 21-22: Testing & Quality
- [ ] **Unit Tests**
  - [ ] Backend services (> 80% coverage)
  - [ ] Frontend components (> 70% coverage)
  - [ ] Utilities and helpers (> 90% coverage)

- [ ] **Integration Tests**
  - [ ] API endpoints
  - [ ] Database operations
  - [ ] Excel parser
  - [ ] ODM generator

- [ ] **E2E Tests (Playwright)**
  - [ ] Create new CRF
  - [ ] Design form with multiple field types
  - [ ] Import Excel template
  - [ ] Export to ODM
  - [ ] Configure visit grid
  - [ ] Preview form

- [ ] **Performance Testing**
  - [ ] Large forms (100+ fields)
  - [ ] Multiple sections
  - [ ] Complex conditional logic
  - [ ] Canvas rendering performance

#### Week 23: UI Polish & Accessibility
- [ ] **UI Refinements**
  - [ ] Consistent spacing and alignment
  - [ ] Loading states
  - [ ] Error states
  - [ ] Empty states
  - [ ] Success messages
  - [ ] Animations and transitions

- [ ] **Accessibility (WCAG 2.1 AA)**
  - [ ] Keyboard navigation
  - [ ] Screen reader support
  - [ ] ARIA labels
  - [ ] Focus management
  - [ ] Color contrast
  - [ ] Alt text for images

- [ ] **Responsive Design**
  - [ ] Mobile optimization
  - [ ] Tablet optimization
  - [ ] Different screen sizes

#### Week 24: Documentation & Deployment
- [ ] **User Documentation**
  - [ ] Getting started guide
  - [ ] CRF designer tutorial
  - [ ] Visit grid configuration guide
  - [ ] Import/export guide
  - [ ] FAQ

- [ ] **Technical Documentation**
  - [ ] API documentation (Swagger/OpenAPI)
  - [ ] Architecture overview
  - [ ] Database schema
  - [ ] Deployment guide
  - [ ] Development setup guide

- [ ] **Deployment**
  - [ ] Set up production environment
  - [ ] Configure CI/CD pipeline
  - [ ] Deploy to staging
  - [ ] User acceptance testing
  - [ ] Deploy to production
  - [ ] Monitoring and alerting

## Team Structure

### Development Team (5-6 people)

**Full-Stack Developer 1 (Lead)**
- Backend architecture
- API design
- Database schema
- Code reviews

**Full-Stack Developer 2**
- Frontend features
- Visual designer
- Excel parser
- Testing

**UI/UX Designer**
- User interface design
- User experience flows
- Design system
- Usability testing

**DevOps Engineer (Part-time, 50%)**
- Infrastructure setup
- CI/CD pipeline
- Deployment
- Monitoring

**Product Manager**
- Requirements
- Prioritization
- Stakeholder communication
- User feedback

**QA Engineer (Part-time, 50%)**
- Test planning
- E2E tests
- Manual testing
- Bug tracking

## Budget Breakdown

**Personnel (6 months):**
- 2 Full-Stack Developers @ $130k/yr = $130,000
- 1 UI/UX Designer @ $110k/yr = $55,000
- 0.5 DevOps Engineer @ $140k/yr = $35,000
- 1 Product Manager @ $150k/yr = $75,000
- 0.5 QA Engineer @ $100k/yr = $25,000

**Total Personnel: $320,000**

**Infrastructure & Tools:**
- Development tools & licenses: $5,000
- Cloud hosting (dev/staging): $3,000
- Testing tools: $2,000

**Total Infrastructure: $10,000**

**Contingency (10%):** $33,000

**Grand Total: ~$363,000**

## Success Criteria

### Functional Requirements
- [ ] Can create CRFs visually with drag-and-drop
- [ ] Can import Excel templates
- [ ] Can export to ODM XML (CDISC compliant)
- [ ] Can configure visit grid with 50+ visits
- [ ] Can preview forms accurately
- [ ] Can create complex validation rules
- [ ] Can version CRFs

### Performance Requirements
- [ ] Page load time < 2 seconds
- [ ] Designer responsive with 100+ fields
- [ ] Excel import < 5 seconds
- [ ] ODM export < 3 seconds

### Usability Requirements
- [ ] Non-technical users can design forms
- [ ] < 30 minutes to create typical CRF
- [ ] < 10 minutes training needed
- [ ] WCAG 2.1 AA compliant

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| React Flow learning curve | Proof-of-concept in week 1, training |
| Excel parsing complexity | Port proven logic, extensive test suite |
| ODM compliance | Follow standard strictly, validate output |
| Scope creep | Strict adherence to "design only" scope |
| Performance issues | Virtual scrolling, lazy loading, profiling |

## Milestones & Demos

**Month 1 Demo:** Basic designer with simple fields
**Month 2 Demo:** Full visual designer with property panel
**Month 3 Demo:** Conditional logic and calculations working
**Month 4 Demo:** Preview mode and Excel import
**Month 5 Demo:** Visit grid and ODM export
**Month 6 Demo:** Polished, production-ready application

## Post-Launch

**Month 7+:**
- Gather user feedback
- Fix bugs
- Performance optimization
- Feature enhancements
- Template library expansion

## Deployment Options

### Option 1: Cloud Hosted (Recommended)
- Deploy on AWS/Azure/GCP
- Kubernetes for orchestration
- PostgreSQL managed database
- Users access via web browser

### Option 2: On-Premise
- Docker containers
- Customer's infrastructure
- VPN access
- More setup required

### Option 3: Desktop App (Electron)
- Standalone application
- No internet required
- SQLite database
- Auto-updates

## Next Steps

1. **Week 1:**
   - Stakeholder approval of plan
   - Team assembly
   - Development environment setup
   - Kickoff meeting

2. **Week 2:**
   - Begin backend foundation
   - Begin frontend foundation
   - First sprint planning

3. **Ongoing:**
   - 2-week sprints
   - Weekly demos
   - Continuous deployment to staging
   - Regular stakeholder updates

---

**This plan delivers a production-ready CRF Design Studio in 6 months for ~$363K with a focused team of 5-6 people.**
