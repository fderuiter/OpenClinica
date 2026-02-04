# CRF Design Studio - Visit Grid Configuration

## Overview

The Visit Grid Configurator is a key feature of the CRF Design Studio that allows users to:
1. **Define visit schedules** - Create study events/time points (Screening, Baseline, Week 4, etc.)
2. **Associate CRFs with visits** - Specify which forms are used at which visits
3. **Configure requirements** - Mark CRFs as required or optional
4. **Set display order** - Control the sequence in which CRFs appear
5. **Add conditional logic** - Show/hide CRFs based on conditions

**Important:** This is a CONFIGURATION tool, not a data collection system. We're designing the visit matrix, not collecting patient data.

## What is a Visit Grid?

A Visit Grid (also called Study Matrix) is a configuration that defines:

```
┌────────────────┬─────────────┬─────────────┬─────────────┬─────────────┐
│ CRF / Visit    │  Screening  │  Baseline   │   Week 4    │   Week 8    │
├────────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Demographics   │      ✓      │             │             │             │
│                │  Required   │             │             │             │
├────────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Medical Hx     │      ✓      │             │             │             │
│                │  Required   │             │             │             │
├────────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Vital Signs    │      ✓      │      ✓      │      ✓      │      ✓      │
│                │  Required   │  Required   │  Required   │  Required   │
├────────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Lab Results    │             │      ✓      │      ✓      │      ✓      │
│                │             │  Required   │  Required   │  Required   │
├────────────────┼─────────────┼─────────────┼─────────────┼─────────────┤
│ Adverse Events │      ○      │      ○      │      ○      │      ○      │
│                │  Optional   │  Optional   │  Optional   │  Optional   │
└────────────────┴─────────────┴─────────────┴─────────────┴─────────────┘

Legend:
✓ = CRF is required at this visit
○ = CRF is optional at this visit
(blank) = CRF not used at this visit
```

## User Interface Design

### Main View: Visit Grid Editor

```
┌─────────────────────────────────────────────────────────────────────┐
│ Study: Clinical Trial ABC-123                           [+ Add Visit]│
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  [+ Add CRF]                                                         │
│                                                                       │
│  ┌─────────────┬────────────┬────────────┬────────────┬────────────┐│
│  │ CRF Name    │ Screening  │ Baseline   │  Week 4    │  Week 8    ││
│  │             │ Day 0      │ Day 1      │ Day 28     │ Day 56     ││
│  ├─────────────┼────────────┼────────────┼────────────┼────────────┤│
│  │ Demographics│ [✓] Req.   │            │            │            ││
│  │  v1.0       │ [↑][↓][⚙] │            │            │            ││
│  ├─────────────┼────────────┼────────────┼────────────┼────────────┤│
│  │ Medical Hx  │ [✓] Req.   │            │            │            ││
│  │  v1.0       │ [↑][↓][⚙] │            │            │            ││
│  ├─────────────┼────────────┼────────────┼────────────┼────────────┤│
│  │ Vital Signs │ [✓] Req.   │ [✓] Req.   │ [✓] Req.   │ [✓] Req.   ││
│  │  v2.1       │ [↑][↓][⚙] │ [↑][↓][⚙] │ [↑][↓][⚙] │ [↑][↓][⚙] ││
│  ├─────────────┼────────────┼────────────┼────────────┼────────────┤│
│  │ Lab Results │            │ [✓] Req.   │ [✓] Req.   │ [✓] Req.   ││
│  │  v1.2       │            │ [↑][↓][⚙] │ [↑][↓][⚙] │ [↑][↓][⚙] ││
│  ├─────────────┼────────────┼────────────┼────────────┼────────────┤│
│  │ Adverse     │ [ ] Opt.   │ [ ] Opt.   │ [ ] Opt.   │ [ ] Opt.   ││
│  │  Events v1.0│ [↑][↓][⚙] │ [↑][↓][⚙] │ [↑][↓][⚙] │ [↑][↓][⚙] ││
│  └─────────────┴────────────┴────────────┴────────────┴────────────┘│
│                                                                       │
│  [Export to Excel] [Export to ODM] [Save] [Preview]                 │
└─────────────────────────────────────────────────────────────────────┘

Controls:
[✓] = Include CRF at this visit (checked)
[ ] = Don't include CRF at this visit (unchecked)
[↑] = Move up in display order
[↓] = Move down in display order
[⚙] = Configure (required/optional, conditional logic, etc.)
```

### CRF Configuration Dialog

When user clicks [⚙]:

```
┌─────────────────────────────────────────────────────────────────┐
│ Configure CRF: Vital Signs at Week 4                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  CRF Version:                                                    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ Vital Signs v2.1 ▼                                      │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
│  Requirement:                                                    │
│  ( ) Required   (•) Optional   ( ) Hidden                       │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ ☐ Enable conditional display                            │   │
│  │                                                          │   │
│  │ Show this CRF only if:                                   │   │
│  │ ┌─────────────────────────────────────────────────┐     │   │
│  │ │ treatment_arm  [equals ▼]  [Active      ▼]     │     │   │
│  │ └─────────────────────────────────────────────────┘     │   │
│  │ [+ Add Condition]                                        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Additional Settings:                                            │
│  ☐ Require electronic signature                                 │
│  ☐ Enable double data entry                                     │
│  ☐ Source data verification required                            │
│                                                                  │
│  Display Order: [2] (within this visit)                         │
│                                                                  │
│  [Cancel] [Save]                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Add Visit Dialog

When user clicks [+ Add Visit]:

```
┌─────────────────────────────────────────────────────────────────┐
│ Add Study Event                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Event Name: [Week 12                                      ]    │
│                                                                  │
│  Description: [Follow-up visit at week 12                  ]    │
│               [                                             ]    │
│                                                                  │
│  Event Type:                                                     │
│  (•) Scheduled   ( ) Unscheduled   ( ) Calibration              │
│                                                                  │
│  ☑ Repeating event                                              │
│    Max repetitions: [3] (leave blank for unlimited)             │
│                                                                  │
│  Display Order: [5] (position in timeline)                      │
│                                                                  │
│  Category: [Follow-up ▼]                                        │
│                                                                  │
│  [Cancel] [Add Event]                                           │
└─────────────────────────────────────────────────────────────────┘
```

## Data Model

### Study Event Definition

```typescript
interface StudyEventDefinition {
  id: string;
  studyId?: string;  // Optional: for multi-study support
  name: string;  // "Screening", "Week 4", etc.
  description?: string;
  type: 'COMMON' | 'SCHEDULED' | 'UNSCHEDULED' | 'CALIBRATION';
  repeating: boolean;
  ordinal: number;  // Display order (1, 2, 3...)
  category?: string;  // "Screening", "Treatment", "Follow-up"
  metadata?: Record<string, any>;
}
```

### Event Definition CRF (Association)

```typescript
interface EventDefinitionCrf {
  id: string;
  studyEventDefinitionId: string;
  crfVersionId: string;
  ordinal: number;  // Display order within visit
  required: boolean;  // Required or optional
  doubleEntry: boolean;  // Require double data entry
  hideCrf: boolean;  // Hidden by default
  sourceDataVerification: 'ALL_REQUIRED' | 'PARTIAL_REQUIRED' | 'NOT_REQUIRED' | 'NOT_USED';
  electronicSignature: boolean;
  defaultVersion: boolean;
  conditionalDisplay?: ConditionalLogic;
}
```

### Conditional Logic

```typescript
interface ConditionalLogic {
  operator: 'AND' | 'OR';
  conditions: Condition[];
}

interface Condition {
  field: string;  // Field name or variable
  operator: 'equals' | 'not_equals' | 'greater_than' | 'less_than' | 'in' | 'not_in' | 'contains';
  value: any;
}
```

## API Endpoints

### Visit Grid Management

```typescript
// Get visit grid configuration for a study
GET /api/visit-grids/:studyId
Response: {
  studyId: string;
  events: StudyEventDefinition[];
  associations: EventDefinitionCrf[];
}

// Update entire visit grid
PUT /api/visit-grids/:studyId
Body: {
  events: StudyEventDefinition[];
  associations: EventDefinitionCrf[];
}

// Add a new visit event
POST /api/visit-grids/:studyId/events
Body: {
  name: string;
  type: string;
  ordinal: number;
  ...
}

// Update visit event
PUT /api/visit-grids/:studyId/events/:eventId
Body: {
  name?: string;
  ordinal?: number;
  ...
}

// Delete visit event
DELETE /api/visit-grids/:studyId/events/:eventId

// Add CRF to visit
POST /api/visit-grids/:studyId/events/:eventId/crfs
Body: {
  crfVersionId: string;
  required: boolean;
  ordinal: number;
  ...
}

// Update CRF association
PUT /api/visit-grids/:studyId/events/:eventId/crfs/:crfId
Body: {
  required?: boolean;
  ordinal?: number;
  conditionalDisplay?: ConditionalLogic;
  ...
}

// Remove CRF from visit
DELETE /api/visit-grids/:studyId/events/:eventId/crfs/:crfId

// Reorder visits
POST /api/visit-grids/:studyId/events/reorder
Body: {
  eventIds: string[];  // Array of event IDs in new order
}

// Reorder CRFs within visit
POST /api/visit-grids/:studyId/events/:eventId/crfs/reorder
Body: {
  crfIds: string[];  // Array of CRF IDs in new order
}
```

## Features

### 1. Drag-and-Drop Configuration

Users can drag CRFs onto the grid to assign them to visits:

```
┌─────────────────────────────────────────────────────────────────┐
│ Available CRFs                  Visit Grid                      │
├────────────────────┬────────────────────────────────────────────┤
│                    │                                             │
│ ┌────────────────┐ │  ┌──────────┬──────────┬──────────┐        │
│ │ Demographics   │ │  │Screening │ Baseline │  Week 4  │        │
│ │ v1.0           │ │  ├──────────┼──────────┼──────────┤        │
│ └────────────────┘ │  │          │          │          │        │
│                    │  │          │          │          │        │
│ ┌────────────────┐ │  │ Drop     │          │          │        │
│ │ Vital Signs    │═══════►here    │          │          │        │
│ │ v2.1           │ │  │          │          │          │        │
│ └────────────────┘ │  │          │          │          │        │
│                    │  └──────────┴──────────┴──────────┘        │
│ ┌────────────────┐ │                                             │
│ │ Lab Results    │ │                                             │
│ │ v1.2           │ │                                             │
│ └────────────────┘ │                                             │
└────────────────────┴────────────────────────────────────────────┘
```

### 2. Timeline Visualization

Visual timeline showing visit sequence:

```
┌─────────────────────────────────────────────────────────────────┐
│ Study Timeline                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Day 0          Day 1         Day 28        Day 56        Day 84 │
│    │             │              │             │             │    │
│    ▼             ▼              ▼             ▼             ▼    │
│ ┌─────┐      ┌─────┐       ┌─────┐       ┌─────┐      ┌─────┐  │
│ │ SCR │──────│ BL  │───────│ W4  │───────│ W8  │──────│ W12 │  │
│ └─────┘      └─────┘       └─────┘       └─────┘      └─────┘  │
│ 3 CRFs       2 CRFs        3 CRFs        3 CRFs       2 CRFs    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

SCR = Screening
BL = Baseline
W4 = Week 4
W8 = Week 8
W12 = Week 12
```

### 3. CRF Version Management

Users can select which version of a CRF to use at each visit:

```
┌─────────────────────────────────────────────────────────────────┐
│ Vital Signs at Baseline                                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Select Version:                                                 │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ v2.1 (Latest) - Added blood pressure measurement    ▼  │    │
│  │ v2.0          - Added temperature field                │    │
│  │ v1.0          - Initial version                         │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ☑ Use latest version automatically                             │
│                                                                  │
│  [Preview Form] [View Changelog]                                │
└─────────────────────────────────────────────────────────────────┘
```

### 4. Bulk Operations

Apply CRF to multiple visits at once:

```
┌─────────────────────────────────────────────────────────────────┐
│ Bulk Assign CRF                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  CRF: Vital Signs v2.1                                          │
│                                                                  │
│  Assign to:                                                      │
│  ☑ Screening                                                     │
│  ☑ Baseline                                                      │
│  ☑ Week 4                                                        │
│  ☑ Week 8                                                        │
│  ☑ Week 12                                                       │
│  ☐ End of Study                                                  │
│                                                                  │
│  Settings for all selected visits:                              │
│  ( ) Required   (•) Optional                                    │
│  ☐ Electronic signature required                                │
│                                                                  │
│  [Cancel] [Apply]                                                │
└─────────────────────────────────────────────────────────────────┘
```

### 5. Conditional Display Configuration

Create rules for when CRFs should be shown:

```
┌─────────────────────────────────────────────────────────────────┐
│ Conditional Display Rules                                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Show "Pregnancy Test" CRF only when:                           │
│                                                                  │
│  [AND ▼]                                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ gender        [equals ▼]     [Female    ▼]     [×]      │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ age           [between ▼]    [18] and [50]     [×]      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [+ Add Condition]                                               │
│                                                                  │
│  Preview: This CRF will be shown for female participants        │
│           between ages 18 and 50.                               │
│                                                                  │
│  [Test Rule] [Clear] [Save]                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 6. Export Options

Export visit grid configuration:

- **Excel Spreadsheet** - Traditional matrix format
- **ODM XML** - CDISC standard format
- **PDF Report** - Human-readable documentation
- **JSON** - For programmatic use

## Validation Rules

### Visit Grid Validation

```typescript
// Validation checks performed before saving:

1. Each visit must have unique name
2. Each visit must have unique ordinal
3. At least one CRF must be assigned to at least one visit
4. CRF versions must exist and be published
5. Ordinals within visit must be sequential
6. Conditional display rules must reference valid fields
7. No circular dependencies in conditional logic
```

## Example Use Cases

### Use Case 1: Simple Linear Study

```
Study Design: 4 visits with standard assessments

Screening → Baseline → Month 3 → Month 6

All subjects follow same path:
- Demographics (Screening only)
- Medical History (Screening only)
- Vital Signs (all visits)
- Lab Results (Baseline, Month 3, Month 6)
- Adverse Events (all visits, optional)
```

### Use Case 2: Treatment Arm-Specific Forms

```
Study Design: Randomized to Active or Placebo

Screening → Randomization → Follow-ups

Conditional forms:
- Pharmacokinetics CRF (only for Active arm)
- Placebo Tolerance (only for Placebo arm)

Configuration:
- PK CRF with conditional: treatment_arm == "Active"
- Tolerance CRF with conditional: treatment_arm == "Placebo"
```

### Use Case 3: Repeating Visits

```
Study Design: Treatment phase with weekly visits

Baseline → Treatment (repeating weekly) → Follow-up

Visit configuration:
- Treatment visit set as "repeating"
- Max repetitions: 12 (for 12 weeks)
- Same CRFs for all treatment visits
```

## Implementation Steps

### Phase 1: Basic Grid Configuration (Week 1-2)
1. Create database schema (tables shown in Data Model section)
2. Build API endpoints for CRUD operations
3. Create basic UI grid component
4. Implement visit addition/deletion
5. Implement CRF assignment to visits

### Phase 2: Advanced Features (Week 3-4)
1. Implement drag-and-drop
2. Add timeline visualization
3. Build CRF configuration dialog
4. Implement reordering (visits and CRFs)
5. Add bulk operations

### Phase 3: Conditional Logic (Week 5-6)
1. Build conditional logic editor
2. Implement rule evaluation engine
3. Add rule preview/testing
4. Validate rule dependencies

### Phase 4: Export & Integration (Week 7-8)
1. Implement ODM XML export
2. Implement Excel export
3. Implement PDF documentation export
4. Add Git integration for version control

## Testing Strategy

### Unit Tests
- Visit grid service logic
- Conditional logic evaluation
- Validation rules
- Reordering algorithms

### Integration Tests
- API endpoints
- Database operations
- Export generators

### E2E Tests
- Create visit grid
- Assign CRFs to visits
- Configure conditional logic
- Reorder visits and CRFs
- Export to various formats

### UI Tests
- Drag-and-drop functionality
- Dialog interactions
- Form validation
- Responsive layout

## Related Documents

- [00-SCOPE-DEFINITION.md](./00-SCOPE-DEFINITION.md) - Project scope
- [02-DESIGN-STUDIO-ARCHITECTURE.md](./02-DESIGN-STUDIO-ARCHITECTURE.md) - System architecture
- [03-DESIGN-STUDIO-DATA-MODEL.md](./03-DESIGN-STUDIO-DATA-MODEL.md) - Database schema
- [development/API-DESIGN.md](./development/API-DESIGN.md) - API conventions
- [guides/STEP-BY-STEP-GUIDE.md](./guides/STEP-BY-STEP-GUIDE.md) - Implementation guide
