# CRF Design Studio vs Data Collection System - Comparison

## What We're Building vs What We're NOT Building

This document clarifies the scope by showing side-by-side comparisons.

## Visual Comparison

```
┌─────────────────────────────────────────────────────────────────┐
│                    CRF DESIGN STUDIO                             │
│                   (What We're Building)                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────┐         ┌──────────────────┐               │
│  │ Visual         │         │ Visit Grid       │               │
│  │ Form Designer  │◄───────►│ Configurator     │               │
│  │                │         │                  │               │
│  │ • Drag & drop  │         │ • Define visits  │               │
│  │ • Configure    │         │ • Link CRFs      │               │
│  │ • Validate     │         │ • Export config  │               │
│  │ • Preview      │         │                  │               │
│  └────────┬───────┘         └────────┬─────────┘               │
│           │                          │                          │
│           └──────────┬───────────────┘                          │
│                      ▼                                           │
│            ┌──────────────────┐                                 │
│            │ Export Package   │                                 │
│            │ • ODM XML        │                                 │
│            │ • Excel          │                                 │
│            │ • JSON Schema    │                                 │
│            └──────────────────┘                                 │
│                                                                   │
│  Users: Study designers, protocol designers                     │
│  Output: Metadata definitions ready for implementation          │
└─────────────────────────────────────────────────────────────────┘
                           │
                           │ Hand off metadata
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│              DATA COLLECTION SYSTEM                              │
│              (What We're NOT Building)                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Imports metadata from Design Studio, then:                     │
│                                                                   │
│  • Enrolls study subjects                                        │
│  • Schedules visits for patients                                │
│  • Collects actual patient data                                 │
│  • Stores data in production database                           │
│  • Validates data in real-time                                  │
│  • Manages clinical workflows                                   │
│  • Tracks data quality                                          │
│  • Generates reports                                            │
│  • Handles queries and discrepancies                            │
│  • Provides audit trails for data                               │
│  • Manages electronic signatures                                │
│                                                                   │
│  Users: Data managers, monitors, coordinators, patients         │
│  Output: Clinical data ready for analysis                       │
└─────────────────────────────────────────────────────────────────┘
```

## Feature-by-Feature Comparison

| Feature | Design Studio ✅ | Data Collection ❌ |
|---------|------------------|-------------------|
| **Form Design** | | |
| Visual form builder | ✅ YES | ❌ NO |
| Drag-and-drop fields | ✅ YES | ❌ NO |
| Configure field properties | ✅ YES | ❌ NO |
| Set validation rules | ✅ YES | ❌ NO |
| Create conditional logic | ✅ YES | ❌ NO |
| Define calculations | ✅ YES | ❌ NO |
| Preview forms | ✅ YES | ❌ NO |
| **Import/Export** | | |
| Import Excel templates | ✅ YES | ❌ NO |
| Export to Excel | ✅ YES | ❌ NO |
| Export to ODM XML | ✅ YES | ❌ NO |
| Export to JSON Schema | ✅ YES | ❌ NO |
| **Visit Configuration** | | |
| Define study visits | ✅ YES | ❌ NO |
| Associate CRFs with visits | ✅ YES | ❌ NO |
| Configure visit requirements | ✅ YES | ❌ NO |
| Preview visit matrix | ✅ YES | ❌ NO |
| **Version Control** | | |
| CRF versioning | ✅ YES | ❌ NO |
| Compare versions | ✅ YES (future) | ❌ NO |
| Version history | ✅ YES | ❌ NO |
| **Subject Management** | | |
| Enroll subjects | ❌ NO | ✅ YES (not building) |
| Schedule subject visits | ❌ NO | ✅ YES (not building) |
| Track subject status | ❌ NO | ✅ YES (not building) |
| **Data Collection** | | |
| Data entry forms | ❌ NO | ✅ YES (not building) |
| Save patient data | ❌ NO | ✅ YES (not building) |
| Real-time validation | ❌ NO | ✅ YES (not building) |
| Progress tracking | ❌ NO | ✅ YES (not building) |
| **Clinical Workflows** | | |
| Data quality checks | ❌ NO | ✅ YES (not building) |
| Query management | ❌ NO | ✅ YES (not building) |
| Discrepancy notes | ❌ NO | ✅ YES (not building) |
| Source data verification | ❌ NO | ✅ YES (not building) |
| Electronic signatures | ❌ NO | ✅ YES (not building) |
| Audit trail (for data) | ❌ NO | ✅ YES (not building) |
| **Reporting** | | |
| Study progress reports | ❌ NO | ✅ YES (not building) |
| Data exports | ❌ NO | ✅ YES (not building) |
| Analytics dashboards | ❌ NO | ✅ YES (not building) |

## Database Comparison

### Design Studio Database (Simple)

```sql
-- ONLY metadata, NO patient data

CREATE TABLE crf (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    definition JSONB,  -- Complete CRF as JSON
    created_at TIMESTAMP
);

CREATE TABLE crf_version (
    id UUID PRIMARY KEY,
    crf_id UUID REFERENCES crf(id),
    version VARCHAR(50),
    definition JSONB,  -- Versioned definition
    created_at TIMESTAMP
);

CREATE TABLE study_template (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    visit_config JSONB,  -- Visit grid configuration
    created_at TIMESTAMP
);

-- That's it! Only 3 main tables
```

### Data Collection Database (Complex)

```sql
-- Full clinical data model (NOT building this)

CREATE TABLE study (...);
CREATE TABLE study_subject (...);     -- Actual patients
CREATE TABLE study_event (...);       -- Scheduled visits
CREATE TABLE event_crf (...);         -- Form instances
CREATE TABLE item_data (...);         -- ACTUAL PATIENT DATA ❌
CREATE TABLE discrepancy_note (...);  -- Queries
CREATE TABLE audit_log (...);         -- Data changes
CREATE TABLE signature (...);         -- E-signatures
-- + 20+ more tables for clinical workflows
```

## User Roles Comparison

### Design Studio Users
- **Study Designers** - Design CRFs
- **Protocol Designers** - Configure visit schedules
- **System Administrators** - Manage system
- **Template Authors** - Create templates

### Data Collection Users (NOT our users)
- ~~Data Managers~~ - Enter patient data
- ~~Clinical Research Coordinators~~ - Manage subjects
- ~~Monitors~~ - Review data quality
- ~~Principal Investigators~~ - Oversee study
- ~~Patients~~ - Sometimes enter own data

## Technology Stack Comparison

### Design Studio Stack ✅

```yaml
Purpose: Design tool, configuration, metadata management
Scale: Single org, < 100 concurrent users

Frontend:
  - React 18 + TypeScript
  - React Flow (visual designer)
  - Material-UI
  - Lighter weight

Backend:
  - Node.js + NestJS
  - Simple REST API
  - PostgreSQL with JSONB
  - No complex workflows

Focus:
  - Great visual design UX
  - Fast iteration
  - Easy export
```

### Data Collection Stack ❌ (Not Building)

```yaml
Purpose: Clinical data collection, patient management
Scale: Multi-site, 1000+ concurrent users, PHI compliance

Frontend:
  - Complex form rendering
  - Real-time validation
  - Progress tracking
  - Dashboard analytics

Backend:
  - Heavy clinical workflows
  - Complex security (PHI)
  - Integration with EDC systems
  - Reporting engines
  - Query management

Focus:
  - Data integrity
  - Regulatory compliance
  - Audit trails
  - High availability
```

## Cost & Timeline Comparison

| Aspect | Design Studio ✅ | Data Collection ❌ |
|--------|------------------|-------------------|
| **Timeline** | 6 months | 12-18 months |
| **Team Size** | 5-6 people | 15-20 people |
| **Development Cost** | ~$363k | ~$2-3M |
| **Complexity** | Medium | Very High |
| **Regulatory** | None | 21 CFR Part 11, HIPAA |
| **Security Level** | Standard | High (PHI) |
| **Infrastructure** | Simple | Complex, distributed |
| **Maintenance** | Low | High |

## Use Case Examples

### Design Studio Use Case ✅

**Scenario:** Research organization designs a new clinical trial

1. Study designer opens Design Studio
2. Creates new CRF "Patient Demographics"
3. Drags fields onto canvas: Age, Gender, Race
4. Configures validation: Age 0-120, Gender required
5. Adds section "Medical History"
6. Adds more fields with conditional logic
7. Previews form
8. Creates visit schedule: Screening, Week 1, Week 4
9. Associates Demographics CRF with Screening visit
10. Exports to ODM XML
11. Hands off to data collection system (separate)

**Result:** Complete, validated CRF design ready for implementation

### Data Collection Use Case ❌ (Out of Scope)

**Scenario:** Clinical site collects patient data (NOT what we're building)

1. ~~Coordinator enrolls new patient~~ ❌
2. ~~System schedules visits for patient~~ ❌
3. ~~Data manager opens Demographics form~~ ❌
4. ~~Enters patient's age: 45~~ ❌
5. ~~Selects gender: Female~~ ❌
6. ~~System validates in real-time~~ ❌
7. ~~Saves data to database~~ ❌
8. ~~Marks form complete~~ ❌
9. ~~Monitor reviews data quality~~ ❌
10. ~~System generates progress report~~ ❌

**Result:** Clinical data collected and stored (completely separate system)

## Integration Point

The two systems connect via **export/import**:

```
┌─────────────────┐                    ┌─────────────────┐
│ Design Studio   │    ODM XML File    │  Data Collection│
│                 │  ───────────────>  │  System         │
│                 │                    │                 │
│ Creates         │    JSON/Excel      │ Imports         │
│ Metadata        │  ───────────────>  │ & Uses for      │
│ Definitions     │                    │ Data Capture    │
└─────────────────┘                    └─────────────────┘

 What we BUILD ✅                       What we DON'T build ❌
                                        (customer has this)
```

## Success Criteria

### For Design Studio ✅

**We succeed if:**
- [ ] Non-technical users can design complex forms
- [ ] Can create typical CRF in < 30 minutes
- [ ] ODM export validates against CDISC standard
- [ ] Excel templates import correctly
- [ ] Visual designer is intuitive
- [ ] Can configure visit grid with 50+ visits
- [ ] Preview accurately shows final form

### For Data Collection ❌ (Not Measuring)

**Not our success criteria:**
- ~~How many subjects enrolled~~
- ~~Data quality metrics~~
- ~~Query resolution time~~
- ~~Form completion rates~~
- ~~Clinical workflow efficiency~~

## Questions for Stakeholder

To confirm scope:

1. **Is this correct?** You want ONLY the design studio, not data collection?
2. **Integration:** Will you use the exported metadata in a separate data collection system?
3. **Users:** Your users are study designers, not data managers?
4. **PHI:** You won't be handling patient data, correct?
5. **Compliance:** No 21 CFR Part 11 or HIPAA requirements?

If yes to all, we're aligned on the design studio scope!

## Summary

| What | Design Studio ✅ | Data Collection ❌ |
|------|------------------|-------------------|
| **Purpose** | Design forms & visits | Collect patient data |
| **Users** | Designers | Data managers, patients |
| **Output** | Metadata (ODM, Excel) | Clinical data |
| **Complexity** | Medium | Very High |
| **Timeline** | 6 months | 12-18 months |
| **Cost** | ~$363k | ~$2-3M |
| **We Build** | ✅ YES | ❌ NO |

---

**Bottom Line:** We're building the **design tool**, not the **clinical system**. Think "Google Forms Builder" not "Google Forms (with responses)".
