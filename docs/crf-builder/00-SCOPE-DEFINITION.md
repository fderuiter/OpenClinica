# CRF Design Studio - Scope Definition

## What We're Building

A **standalone CRF Design Studio** - a configuration and metadata management tool for designing Case Report Forms (CRFs) and configuring study visit schedules. This is **NOT** a data collection system.

## Clear Scope

### ✅ IN SCOPE: Design & Configuration

1. **CRF Designer**
   - Visual form designer (drag-and-drop)
   - Define form structure (sections, fields)
   - Configure field properties (validation, data types)
   - Create repeating groups
   - Set up conditional display logic
   - Define calculations
   - Version management
   - Form preview

2. **Visit Grid Configuration**
   - Define study events (visits)
   - Associate CRFs with events
   - Set CRF requirements (required/optional)
   - Configure event properties
   - Define visit order and scheduling rules
   - Preview visit matrix

3. **Metadata Management**
   - Import CRF templates (Excel)
   - Export CRF definitions (Excel, ODM XML)
   - CRF library management
   - Template catalog
   - Version control

4. **Preview & Validation**
   - Preview forms as they will appear
   - Validate form definitions
   - Check for errors and warnings
   - Export validation rules

### ❌ OUT OF SCOPE: Data Collection

1. **NOT Building:**
   - Subject enrollment
   - Actual data entry forms
   - Data storage (item_data, event_crf tables)
   - Study participant management
   - Clinical workflows
   - Data monitoring
   - Query management
   - Audit trails for data
   - Electronic signatures for data
   - Source data verification
   - Discrepancy notes
   - Real-time data validation during entry
   - Progress tracking for subjects
   - Completion status tracking

2. **Data Model Simplification:**
   - Keep: crf, crf_version, section, item, item_form_metadata, item_group
   - Keep: study_event_definition, event_definition_crf
   - Remove: event_crf, item_data (these are for actual data)
   - Remove: study_event, study_subject (actual study execution)

## Use Cases

### Primary Users

**1. Study Designers**
- Create new CRF definitions
- Upload Excel templates
- Design forms visually
- Configure validation rules
- Version CRFs

**2. Protocol Designers**
- Define study visit schedule
- Associate CRFs with visits
- Configure visit requirements
- Export study design

**3. System Administrators**
- Manage CRF library
- Import/export definitions
- Manage templates
- Configure system

### Key Workflows

**Workflow 1: Create New CRF**
```
1. Open CRF Designer
2. Choose: Upload Template OR Start from Scratch
3. If upload: Parse Excel → Preview → Confirm
4. If scratch: Drag fields onto canvas
5. Configure field properties
6. Add validation rules
7. Preview form
8. Save/Publish CRF
```

**Workflow 2: Configure Visit Schedule**
```
1. Open Visit Grid Configuration
2. Create study events (visits)
3. For each visit:
   - Add CRFs to visit
   - Set required/optional
   - Set default version
4. Preview visit matrix
5. Export configuration
```

**Workflow 3: Export for Implementation**
```
1. Select CRFs to export
2. Choose format (ODM XML, Excel)
3. Export includes:
   - CRF definitions
   - Validation rules
   - Visit configuration
4. Hand off to data collection system
```

## System Boundaries

```
┌─────────────────────────────────────────────────────────────┐
│           CRF DESIGN STUDIO (This System)                    │
│                                                               │
│  ┌────────────────┐         ┌──────────────────┐           │
│  │ CRF Designer   │         │ Visit Grid       │           │
│  │                │         │ Configuration    │           │
│  │ • Create CRFs  │         │                  │           │
│  │ • Edit forms   │         │ • Define visits  │           │
│  │ • Validation   │◄───────►│ • Link CRFs      │           │
│  │ • Preview      │         │ • Export config  │           │
│  └────────────────┘         └──────────────────┘           │
│          │                           │                       │
│          └───────────┬───────────────┘                       │
│                      ▼                                        │
│            ┌──────────────────┐                              │
│            │ Metadata Export  │                              │
│            │ (ODM, Excel)     │                              │
│            └──────────────────┘                              │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ Export Package
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         DATA COLLECTION SYSTEM (External - Not Built)        │
│                                                               │
│  • Imports CRF definitions                                   │
│  • Collects actual data                                      │
│  • Manages subjects                                          │
│  • Clinical workflows                                        │
└─────────────────────────────────────────────────────────────┘
```

## Architecture Implications

### Simplified Components

**Keep:**
- CRF metadata models (CRF, CRFVersion, Section, Item)
- Form designer UI
- Validation engine (for design-time validation)
- Excel parser
- ODM export generator
- Visit definition models

**Remove/Simplify:**
- Data entry forms (just preview)
- Data storage models (event_crf, item_data)
- Subject/patient models
- Clinical workflow logic
- Real-time form validation (just design validation)
- Audit trail for data (keep for design changes only)

### New Focus Areas

**Enhanced Design Experience:**
- Rich visual designer (like Google Forms, Typeform)
- Live preview
- Drag-and-drop interface
- Component palette
- Template library
- Collaborative design (multiple users)

**Better Export:**
- Multiple format support
- Implementation guides
- JSON Schema output
- API specifications for implementation

## Technology Implications

### Different Framework Considerations

Since this is a **design tool** rather than a **data collection system**, framework choices change:

**Frontend Priorities:**
- Excellent drag-and-drop (react-dnd, dnd-kit)
- Rich form builder UI (FormBuilder.io, SurveyJS)
- Canvas/drawing capabilities
- Real-time collaboration (Yjs, Socket.io)
- Component libraries with design tools

**Backend Priorities:**
- Metadata storage and versioning
- Template management
- Export generation (ODM, Excel)
- Lighter weight (no heavy clinical workflows)
- API-first for export integration

**Framework Options to Consider:**
1. **React + TypeScript** (still good choice)
   - React Flow for visual design
   - react-dnd for drag-and-drop
   - Excellent component ecosystem

2. **Vue 3 + TypeScript** (alternative)
   - Vue Flow for visual design
   - Lighter weight than React
   - Great for design tools

3. **Svelte + SvelteKit** (modern alternative)
   - Better performance
   - Simpler state management
   - Good for design tools

4. **No-Code Platform Extension**
   - Build on SurveyJS, FormBuilder.io
   - Faster development
   - Limited customization

## Success Criteria

### For Design Studio

1. **CRF Designer:**
   - Can create complex forms visually
   - Support all field types
   - Validation rules configurable
   - Preview looks like final form
   - Export to ODM XML matches standard

2. **Visit Grid Configuration:**
   - Can define 50+ visits
   - Associate 20+ CRFs per visit
   - Clear visual representation
   - Export complete configuration

3. **Usability:**
   - Non-technical users can design forms
   - < 30 min to create typical CRF
   - < 5 min to configure visit grid
   - Intuitive UI requiring minimal training

4. **Integration:**
   - ODM export validated against standard
   - Excel templates compatible
   - JSON Schema for modern systems
   - API documentation for implementers

### NOT Measuring

- Data collection metrics
- Subject enrollment
- Form completion rates
- Clinical workflow efficiency
- Data quality metrics

## Deliverables

### What We Deliver

1. **Design Studio Application**
   - Web-based application
   - CRF visual designer
   - Visit grid configurator
   - Preview system

2. **Export Packages**
   - ODM XML files
   - Excel templates
   - JSON Schema
   - Implementation guides

3. **Documentation**
   - User guide for designers
   - API documentation
   - Integration guide
   - Template library

### What We Don't Deliver

- Data collection application
- Subject management system
- Clinical workflow engine
- Data storage system
- Monitoring/reporting tools

## Timeline Impact

**Shorter Timeline Expected:**
- No clinical workflows to build
- No data storage optimization
- No subject management
- No complex security for PHI
- Simpler testing (no data scenarios)

**Estimated Timeline:**
- Original: 36 weeks (9 months)
- Revised: 20-24 weeks (5-6 months)
- Team: 6-7 people (reduced from 10)

## Cost Impact

**Lower Cost Expected:**
- Smaller team
- Shorter timeline
- Simpler infrastructure
- Less complex security

**Estimated Cost:**
- Original: ~$1.08M
- Revised: ~$600-700K

## Next Steps

1. Review this scope with stakeholders
2. Confirm this matches intended use case
3. Update all documentation to reflect design-only focus
4. Re-analyze framework options for design tools
5. Create focused migration plan

---

**Critical Question for Stakeholder:**
Will this design studio need to connect to ANY data collection system, or is it purely for exporting definitions that will be manually imported elsewhere?

If purely export-focused, we can optimize even further for offline-first, desktop application possibilities.
