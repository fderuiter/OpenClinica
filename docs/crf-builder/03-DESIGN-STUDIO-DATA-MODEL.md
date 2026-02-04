# CRF Design Studio - Data Model

## Overview

The CRF Design Studio stores **metadata only** - no patient data, no collected responses, no clinical trial data. This is a design tool, not a data collection system.

## Key Principle

**We store DEFINITIONS, not DATA:**
- ✅ CRF structure (what fields exist)
- ✅ Field properties (validation rules, display options)
- ✅ Visit schedules (what visits are planned)
- ✅ CRF-to-visit associations (which forms for which visits)
- ❌ Patient information
- ❌ Form responses
- ❌ Clinical trial data

## Database Schema

### Core Tables

```sql
-- CRF Definition (the form itself)
CREATE TABLE crf (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    status              VARCHAR(50) DEFAULT 'DRAFT',  -- DRAFT, ACTIVE, ARCHIVED
    category            VARCHAR(100),  -- Demographics, Vitals, AE, etc.
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    metadata            JSONB,  -- Flexible additional metadata
    
    CONSTRAINT crf_name_unique UNIQUE (name)
);

-- CRF Version (versioned form definition)
CREATE TABLE crf_version (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crf_id              UUID NOT NULL REFERENCES crf(id) ON DELETE CASCADE,
    version             VARCHAR(50) NOT NULL,  -- Semantic version: 1.0.0, 1.1.0, 2.0.0
    description         TEXT,
    definition          JSONB NOT NULL,  -- Complete form definition in JSON
    git_commit          VARCHAR(100),  -- Git commit hash for version tracking
    status              VARCHAR(50) DEFAULT 'DRAFT',  -- DRAFT, PUBLISHED, DEPRECATED
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT crf_version_unique UNIQUE (crf_id, version)
);

-- Section (pages within a CRF)
CREATE TABLE section (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crf_version_id      UUID NOT NULL REFERENCES crf_version(id) ON DELETE CASCADE,
    label               VARCHAR(255) NOT NULL,
    title               VARCHAR(500),
    subtitle            TEXT,
    instructions        TEXT,
    ordinal             INTEGER NOT NULL,  -- Display order
    page_number         INTEGER,
    borders             BOOLEAN DEFAULT FALSE,
    metadata            JSONB,
    
    CONSTRAINT section_ordinal_unique UNIQUE (crf_version_id, ordinal)
);

-- Item (field definition)
CREATE TABLE item (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,  -- Unique identifier
    description         TEXT,
    question_text       TEXT,
    data_type           VARCHAR(50) NOT NULL,  -- ST, INT, REAL, DATE, PDATE, FILE, etc.
    units               VARCHAR(100),
    phi                 BOOLEAN DEFAULT FALSE,  -- Protected Health Information flag
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP DEFAULT NOW(),
    metadata            JSONB,
    
    CONSTRAINT item_name_unique UNIQUE (name)
);

-- Item Form Metadata (how item appears in specific section)
CREATE TABLE item_form_metadata (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id             UUID NOT NULL REFERENCES item(id) ON DELETE CASCADE,
    section_id          UUID NOT NULL REFERENCES section(id) ON DELETE CASCADE,
    ordinal             INTEGER NOT NULL,  -- Display order within section
    required            BOOLEAN DEFAULT FALSE,
    
    -- Display Properties
    header              VARCHAR(255),
    subheader           VARCHAR(255),
    left_item_text      VARCHAR(255),
    right_item_text     VARCHAR(255),
    column_number       INTEGER,
    
    -- Response Type & Options
    response_type       VARCHAR(50) NOT NULL,  -- text, textarea, single-select, multi-select, checkbox, radio, calculation, instant-calculation, file, group-calculation
    response_options    JSONB,  -- For select/radio/checkbox: {options: [{value, label, score}]}
    
    -- Validation Rules
    validation_type     VARCHAR(50),  -- regexp, range, date_range, etc.
    validation_config   JSONB,  -- {pattern, min, max, error_message, etc.}
    
    -- Default Value
    default_value       TEXT,
    
    -- Display Logic
    show_condition      JSONB,  -- Conditional display rules
    
    -- Calculation
    calculation_formula TEXT,
    
    -- Width Control
    width_decimal       VARCHAR(50),
    
    metadata            JSONB,
    
    CONSTRAINT item_form_ordinal_unique UNIQUE (section_id, ordinal)
);

-- Item Group (repeating groups)
CREATE TABLE item_group (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crf_version_id      UUID NOT NULL REFERENCES crf_version(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    repeating           BOOLEAN DEFAULT FALSE,
    repeating_max       INTEGER,  -- Max repetitions (NULL = unlimited)
    metadata            JSONB
);

-- Item Group Membership
CREATE TABLE item_group_metadata (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_group_id       UUID NOT NULL REFERENCES item_group(id) ON DELETE CASCADE,
    item_id             UUID NOT NULL REFERENCES item(id) ON DELETE CASCADE,
    ordinal             INTEGER NOT NULL,  -- Order within group
    column_number       INTEGER,
    
    CONSTRAINT item_group_item_ordinal_unique UNIQUE (item_group_id, ordinal)
);

-- Study Event Definition (visit type)
CREATE TABLE study_event_definition (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    study_id            UUID,  -- Optional: if multi-study support
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    type                VARCHAR(50) DEFAULT 'COMMON',  -- COMMON, SCHEDULED, UNSCHEDULED, CALIBRATION
    repeating           BOOLEAN DEFAULT FALSE,
    ordinal             INTEGER NOT NULL,  -- Display order
    category            VARCHAR(100),
    metadata            JSONB
);

-- Event Definition CRF (CRF-to-visit association)
CREATE TABLE event_definition_crf (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    study_event_definition_id UUID NOT NULL REFERENCES study_event_definition(id) ON DELETE CASCADE,
    crf_version_id      UUID NOT NULL REFERENCES crf_version(id) ON DELETE CASCADE,
    ordinal             INTEGER NOT NULL,  -- Display order within visit
    required            BOOLEAN DEFAULT TRUE,
    double_entry        BOOLEAN DEFAULT FALSE,
    hide_crf            BOOLEAN DEFAULT FALSE,
    source_data_verification VARCHAR(50),  -- AllREQUIRED, PARTIALREQUIRED, NOTREQUIRED, NOT_USED
    electronic_signature BOOLEAN DEFAULT FALSE,
    default_version     BOOLEAN DEFAULT FALSE,
    conditional_display JSONB,  -- Conditional logic for showing this CRF
    
    CONSTRAINT event_crf_ordinal_unique UNIQUE (study_event_definition_id, ordinal)
);

-- User Management
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255),  -- Hashed password
    name                VARCHAR(255),
    role                VARCHAR(50) DEFAULT 'USER',  -- USER, ADMIN, DESIGNER
    active              BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT NOW(),
    last_login          TIMESTAMP,
    metadata            JSONB
);

-- Template Library
CREATE TABLE template (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    category            VARCHAR(100),
    crf_version_id      UUID REFERENCES crf_version(id),  -- Template based on CRF
    is_public           BOOLEAN DEFAULT FALSE,
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP DEFAULT NOW(),
    usage_count         INTEGER DEFAULT 0,
    metadata            JSONB
);

-- Audit Log
CREATE TABLE audit_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES users(id),
    action              VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE, EXPORT, IMPORT
    entity_type         VARCHAR(50) NOT NULL,  -- CRF, CRF_VERSION, SECTION, ITEM, etc.
    entity_id           UUID NOT NULL,
    timestamp           TIMESTAMP DEFAULT NOW(),
    changes             JSONB,  -- Before/after values
    ip_address          VARCHAR(50),
    user_agent          TEXT
);

-- Create indexes for performance
CREATE INDEX idx_crf_status ON crf(status);
CREATE INDEX idx_crf_category ON crf(category);
CREATE INDEX idx_crf_version_crf ON crf_version(crf_id);
CREATE INDEX idx_section_crf_version ON section(crf_version_id);
CREATE INDEX idx_item_form_metadata_section ON item_form_metadata(section_id);
CREATE INDEX idx_item_form_metadata_item ON item_form_metadata(item_id);
CREATE INDEX idx_item_group_crf_version ON item_group(crf_version_id);
CREATE INDEX idx_event_def_crf_study_event ON event_definition_crf(study_event_definition_id);
CREATE INDEX idx_event_def_crf_crf_version ON event_definition_crf(crf_version_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
```

## Entity Relationships

```
┌─────────────┐
│    users    │
└──────┬──────┘
       │ creates
       ▼
┌─────────────┐
│     crf     │◄────────────┐
└──────┬──────┘             │
       │ has versions       │
       ▼                    │
┌─────────────┐             │
│ crf_version │             │ based on
└──────┬──────┘             │
       │ has sections       │
       ▼                    │
┌─────────────┐       ┌────┴─────┐
│   section   │       │ template │
└──────┬──────┘       └──────────┘
       │ contains
       ▼
┌──────────────────┐
│ item_form_       │
│  metadata        │
└─────────┬────────┘
          │ references
          ▼
    ┌─────────────┐
    │    item     │
    └──────┬──────┘
           │ grouped in
           ▼
    ┌──────────────────┐
    │  item_group_     │
    │   metadata       │
    └─────────┬────────┘
              │
              ▼
       ┌─────────────┐
       │ item_group  │
       └─────────────┘

┌──────────────────────┐
│ study_event_         │
│  definition          │
└──────────┬───────────┘
           │ associated with
           ▼
┌──────────────────────┐
│ event_definition_    │
│      crf             │
└──────────┬───────────┘
           │ uses
           ▼
    ┌─────────────┐
    │ crf_version │
    └─────────────┘
```

## Data Types

### Item Data Types

| Data Type | Description | Example |
|-----------|-------------|---------|
| `ST` | String/Text | "Patient's name" |
| `INT` | Integer | 42 |
| `REAL` | Decimal number | 98.6 |
| `DATE` | Full date | 2024-01-15 |
| `PDATE` | Partial date | 2024-01 (month/year only) |
| `FILE` | File upload | document.pdf |
| `BOOLEAN` | True/False | true |
| `CALCULATION` | Calculated value | =BMI formula |

### Response Types

| Response Type | Description | UI Element |
|---------------|-------------|------------|
| `text` | Single-line text | `<input type="text">` |
| `textarea` | Multi-line text | `<textarea>` |
| `single-select` | Choose one from list | `<select>` dropdown |
| `multi-select` | Choose multiple | Multiple checkboxes |
| `radio` | Choose one (visible options) | Radio buttons |
| `checkbox` | Single checkbox | `<input type="checkbox">` |
| `file` | File upload | `<input type="file">` |
| `calculation` | Calculated field | Read-only display |
| `instant-calculation` | Real-time calc | Updates as user types |

### Validation Types

| Validation Type | Description | Configuration |
|-----------------|-------------|---------------|
| `regexp` | Regular expression | `{pattern: "^[A-Z]{3}$"}` |
| `range` | Numeric range | `{min: 0, max: 100}` |
| `date_range` | Date range | `{min_date: "2024-01-01", max_date: "2024-12-31"}` |
| `required` | Must have value | `{required: true}` |
| `length` | String length | `{min_length: 5, max_length: 50}` |
| `email` | Email format | Built-in validation |
| `phone` | Phone format | `{pattern: "^\\d{3}-\\d{3}-\\d{4}$"}` |

## JSONB Structures

### crf_version.definition (Complete CRF Definition)

```json
{
  "version": "1.0.0",
  "sections": [
    {
      "id": "sec-1",
      "label": "Demographics",
      "ordinal": 1,
      "items": [
        {
          "id": "item-1",
          "name": "subject_initials",
          "question": "Subject Initials",
          "data_type": "ST",
          "response_type": "text",
          "required": true,
          "validation": {
            "type": "regexp",
            "pattern": "^[A-Z]{2,3}$",
            "message": "Must be 2-3 uppercase letters"
          }
        },
        {
          "id": "item-2",
          "name": "date_of_birth",
          "question": "Date of Birth",
          "data_type": "DATE",
          "response_type": "date",
          "required": true,
          "validation": {
            "type": "date_range",
            "max_date": "today",
            "message": "Cannot be in the future"
          }
        }
      ]
    }
  ],
  "item_groups": [
    {
      "id": "ig-1",
      "name": "medications",
      "label": "Concomitant Medications",
      "repeating": true,
      "items": ["med_name", "med_dose", "med_start_date"]
    }
  ]
}
```

### item_form_metadata.response_options (Select/Radio/Checkbox)

```json
{
  "options": [
    {"value": "1", "label": "Male", "score": 0},
    {"value": "2", "label": "Female", "score": 0},
    {"value": "3", "label": "Other", "score": 0}
  ]
}
```

### item_form_metadata.show_condition (Conditional Display)

```json
{
  "operator": "AND",
  "conditions": [
    {
      "field": "has_adverse_event",
      "operator": "equals",
      "value": "Yes"
    },
    {
      "field": "severity",
      "operator": "in",
      "value": ["Moderate", "Severe"]
    }
  ]
}
```

### event_definition_crf.conditional_display

```json
{
  "show_if": {
    "operator": "OR",
    "conditions": [
      {
        "field": "treatment_arm",
        "operator": "equals",
        "value": "Active"
      },
      {
        "field": "patient_age",
        "operator": ">=",
        "value": 65
      }
    ]
  }
}
```

## Git Storage Structure

CRF versions are also stored as JSON files in Git for version control:

```
crf-repository/
├── demographics/
│   ├── v1.0.0.json        # First version
│   ├── v1.1.0.json        # Minor update
│   └── v2.0.0.json        # Major revision
├── vital-signs/
│   ├── v1.0.0.json
│   └── v1.1.0.json
├── adverse-events/
│   └── v1.0.0.json
└── README.md
```

Each JSON file contains:
```json
{
  "crf_name": "Demographics",
  "version": "1.0.0",
  "created_at": "2024-01-15T10:30:00Z",
  "created_by": "john@example.com",
  "description": "Initial version",
  "definition": {
    // Complete CRF structure
  }
}
```

## Sample Data

### Example CRF: Demographics

```sql
-- CRF
INSERT INTO crf (id, name, description, status, category) VALUES
('crf-001', 'Demographics', 'Patient demographic information', 'ACTIVE', 'Demographics');

-- CRF Version
INSERT INTO crf_version (id, crf_id, version, description, status) VALUES
('cv-001', 'crf-001', '1.0.0', 'Initial version', 'PUBLISHED');

-- Section
INSERT INTO section (id, crf_version_id, label, ordinal) VALUES
('sec-001', 'cv-001', 'Basic Information', 1);

-- Items
INSERT INTO item (id, name, description, data_type) VALUES
('item-001', 'subject_initials', 'Subject initials', 'ST'),
('item-002', 'date_of_birth', 'Date of birth', 'DATE'),
('item-003', 'gender', 'Gender', 'ST');

-- Item Form Metadata
INSERT INTO item_form_metadata (id, item_id, section_id, ordinal, required, response_type, response_options) VALUES
('ifm-001', 'item-001', 'sec-001', 1, true, 'text', NULL),
('ifm-002', 'item-002', 'sec-001', 2, true, 'date', NULL),
('ifm-003', 'item-003', 'sec-001', 3, true, 'radio', 
 '{"options": [{"value":"M","label":"Male"},{"value":"F","label":"Female"},{"value":"O","label":"Other"}]}'::jsonb);
```

### Example Visit Grid

```sql
-- Study Events
INSERT INTO study_event_definition (id, name, ordinal, repeating) VALUES
('event-001', 'Screening', 1, false),
('event-002', 'Baseline', 2, false),
('event-003', 'Week 4', 3, false),
('event-004', 'Week 8', 4, false);

-- CRF-to-Event Associations
INSERT INTO event_definition_crf (id, study_event_definition_id, crf_version_id, ordinal, required) VALUES
('edc-001', 'event-001', 'cv-001', 1, true),  -- Demographics at Screening
('edc-002', 'event-002', 'cv-vitals', 1, true),  -- Vital Signs at Baseline
('edc-003', 'event-003', 'cv-vitals', 1, true),  -- Vital Signs at Week 4
('edc-004', 'event-004', 'cv-vitals', 1, true);  -- Vital Signs at Week 8
```

## Queries

### Get all CRFs with latest version
```sql
SELECT 
    c.id,
    c.name,
    c.description,
    c.status,
    cv.version,
    cv.created_at as last_updated
FROM crf c
LEFT JOIN LATERAL (
    SELECT version, created_at
    FROM crf_version
    WHERE crf_id = c.id
    ORDER BY created_at DESC
    LIMIT 1
) cv ON true
WHERE c.status = 'ACTIVE'
ORDER BY c.name;
```

### Get complete CRF structure
```sql
SELECT 
    c.name as crf_name,
    cv.version,
    s.label as section_label,
    s.ordinal as section_order,
    i.name as item_name,
    i.question_text,
    i.data_type,
    ifm.response_type,
    ifm.required,
    ifm.ordinal as item_order
FROM crf c
JOIN crf_version cv ON cv.crf_id = c.id
JOIN section s ON s.crf_version_id = cv.id
JOIN item_form_metadata ifm ON ifm.section_id = s.id
JOIN item i ON i.id = ifm.item_id
WHERE c.id = 'crf-001' AND cv.version = '1.0.0'
ORDER BY s.ordinal, ifm.ordinal;
```

### Get visit grid for a study
```sql
SELECT 
    sed.name as visit_name,
    sed.ordinal as visit_order,
    c.name as crf_name,
    cv.version,
    edc.ordinal as crf_order,
    edc.required
FROM study_event_definition sed
JOIN event_definition_crf edc ON edc.study_event_definition_id = sed.id
JOIN crf_version cv ON cv.id = edc.crf_version_id
JOIN crf c ON c.id = cv.crf_id
ORDER BY sed.ordinal, edc.ordinal;
```

## Data Validation Rules

### At CRF Level
- CRF name must be unique
- At least one section required
- At least one item required
- Version numbers must follow SemVer (1.0.0, 1.1.0, 2.0.0)

### At Section Level
- Section labels must be unique within CRF
- Ordinals must be sequential and unique
- At least one item per section

### At Item Level
- Item names must be unique globally
- Item names must be valid identifiers (alphanumeric + underscore)
- Data type must match response type

### At Item Form Metadata Level
- Required fields cannot have show_condition (always shown)
- Calculation fields must be read-only
- Response options required for select/radio/checkbox types
- Ordinals must be sequential and unique within section

### At Visit Grid Level
- Each visit must have at least one CRF
- CRF versions must exist and be published
- Ordinals must be sequential

## Migration from Excel

When importing Excel templates:

1. **Parse Excel file**
   - Read sheets and cells
   - Validate structure
   
2. **Create CRF record**
   - Extract CRF name and description
   - Set status to DRAFT
   
3. **Create CRF version**
   - Generate version 1.0.0
   - Store complete definition in JSONB
   
4. **Create sections**
   - One section per Excel sheet
   - Extract section properties
   
5. **Create items**
   - Parse item definitions
   - Validate data types
   
6. **Create item form metadata**
   - Link items to sections
   - Extract validation rules
   - Parse response options
   
7. **Commit to Git**
   - Create JSON file
   - Commit with version tag
   
8. **Update status**
   - Change to ACTIVE when validated

## Related Documents

- [02-DESIGN-STUDIO-ARCHITECTURE.md](./02-DESIGN-STUDIO-ARCHITECTURE.md) - System architecture
- [04-VISIT-GRID-INTEGRATION.md](./04-VISIT-GRID-INTEGRATION.md) - Visit grid details
- [development/GIT-VERSIONING-STRATEGY.md](./development/GIT-VERSIONING-STRATEGY.md) - Version control
- [guides/STEP-BY-STEP-GUIDE.md](./guides/STEP-BY-STEP-GUIDE.md) - Implementation guide
