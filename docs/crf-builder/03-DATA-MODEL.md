# OpenClinica CRF Builder - Data Model & Database Schema

## Database Schema Overview

The CRF Builder uses a relational database schema with clear separation between:
1. **Definition Tables** - Store CRF structure and metadata
2. **Instance Tables** - Store actual data collection instances
3. **Data Tables** - Store collected data values
4. **Association Tables** - Link CRFs to study events

## Entity Relationship Diagram

```
┌─────────────┐
│   study     │
└──────┬──────┘
       │
       ├──────────────────────────────┐
       │                              │
       ▼                              ▼
┌─────────────┐              ┌────────────────────┐
│    crf      │              │ study_event_       │
│             │              │   definition       │
└──────┬──────┘              └─────────┬──────────┘
       │                               │
       │                               │
       ▼                               │
┌─────────────┐                       │
│crf_version  │                       │
└──────┬──────┘                       │
       │                               │
       │                               ▼
       │                     ┌──────────────────────┐
       │                     │  event_definition_   │
       │                     │       crf            │◄─────┐
       │                     └──────────┬───────────┘      │
       │                                │                  │
       ▼                                │                  │
┌─────────────┐                        │                  │
│  section    │                        ▼                  │
└──────┬──────┘              ┌──────────────────┐         │
       │                     │   study_event    │         │
       │                     └─────────┬────────┘         │
       │                               │                  │
       ▼                               ▼                  │
┌─────────────┐              ┌──────────────────┐        │
│    item     │              │   event_crf      │────────┘
└──────┬──────┘              └─────────┬────────┘
       │                               │
       │                               │
       ├───────────────────────────────┤
       │                               │
       ▼                               ▼
┌─────────────────┐          ┌──────────────────┐
│item_form_       │          │   item_data      │
│  metadata       │          └──────────────────┘
└─────────────────┘

┌─────────────┐
│item_group   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│item_group_      │
│  metadata       │
└─────────────────┘
```

## Core Tables

### 1. CRF Definition Tables

#### **crf** - CRF Definitions
```sql
CREATE TABLE crf (
    crf_id SERIAL PRIMARY KEY,
    status_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    oc_oid VARCHAR(40) UNIQUE,
    study_id INT NOT NULL,
    
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    FOREIGN KEY (study_id) REFERENCES study(study_id),
    UNIQUE (name, study_id)
);
```

**Key Fields:**
- `crf_id` - Primary key
- `name` - CRF identifier (e.g., "Demographics", "Adverse Events")
- `description` - Human-readable description
- `oc_oid` - CDISC OID (unique identifier for ODM export)
- `study_id` - Associated study
- `status_id` - Status (available, unavailable, deleted)

#### **crf_version** - CRF Versions
```sql
CREATE TABLE crf_version (
    crf_version_id SERIAL PRIMARY KEY,
    crf_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    revision_notes VARCHAR(255),
    status_id INT NOT NULL,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    oc_oid VARCHAR(40) UNIQUE,
    xform TEXT,
    xform_name VARCHAR(255),
    
    FOREIGN KEY (crf_id) REFERENCES crf(crf_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    UNIQUE (crf_id, name)
);
```

**Key Fields:**
- `crf_version_id` - Primary key
- `crf_id` - Parent CRF
- `name` - Version identifier (e.g., "v1.0", "v2.0")
- `revision_notes` - Change description
- `xform` - XForm XML (for mobile data collection)
- `xform_name` - XForm filename

#### **section** - CRF Sections/Pages
```sql
CREATE TABLE section (
    section_id SERIAL PRIMARY KEY,
    crf_version_id INT NOT NULL,
    status_id INT NOT NULL,
    label VARCHAR(2000) NOT NULL,
    title VARCHAR(2000),
    subtitle VARCHAR(2000),
    instructions TEXT,
    page_number_label VARCHAR(5),
    ordinal INT NOT NULL,
    parent_id INT,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    borders INT DEFAULT 0,
    
    FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id),
    FOREIGN KEY (parent_id) REFERENCES section(section_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    UNIQUE (crf_version_id, label)
);
```

**Key Fields:**
- `section_id` - Primary key
- `crf_version_id` - Parent CRF version
- `label` - Identifier (e.g., "DEMOGRAPHICS", "VITALS")
- `title` - Display name (e.g., "Patient Demographics")
- `subtitle` - Additional heading
- `instructions` - Section instructions
- `page_number_label` - Page number display
- `ordinal` - Sort order
- `parent_id` - Parent section (for nested sections)
- `borders` - Border display (0=none, 1=show)

#### **item** - Data Items/Questions
```sql
CREATE TABLE item (
    item_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(4000),
    units VARCHAR(64),
    phi_status BOOLEAN DEFAULT FALSE,
    item_data_type_id INT NOT NULL,
    item_reference_type_id INT NOT NULL,
    status_id INT NOT NULL,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    oc_oid VARCHAR(40) UNIQUE,
    
    FOREIGN KEY (item_data_type_id) REFERENCES item_data_type(item_data_type_id),
    FOREIGN KEY (item_reference_type_id) REFERENCES item_reference_type(item_reference_type_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id)
);
```

**Key Fields:**
- `item_id` - Primary key
- `name` - Unique identifier (e.g., "AGE", "BLOOD_PRESSURE_SYS")
- `description` - Item description
- `units` - Measurement units (e.g., "years", "mmHg")
- `phi_status` - Protected Health Information flag
- `item_data_type_id` - Data type (ST, INT, REAL, DATE, etc.)
- `item_reference_type_id` - Reference type
- `oc_oid` - CDISC OID

#### **item_form_metadata** - Form-Specific Item Configuration
```sql
CREATE TABLE item_form_metadata (
    item_form_metadata_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    crf_version_id INT NOT NULL,
    header TEXT,
    subheader TEXT,
    parent_id INT,
    parent_label VARCHAR(120),
    column_number INT,
    page_number_label VARCHAR(5),
    question_number_label VARCHAR(20),
    left_item_text VARCHAR(2000),
    right_item_text VARCHAR(2000),
    section_id INT NOT NULL,
    response_set_id INT NOT NULL,
    regexp VARCHAR(1000),
    regexp_error_msg VARCHAR(255),
    ordinal INT NOT NULL,
    required BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(4000),
    response_layout VARCHAR(20),
    width_decimal VARCHAR(10),
    show_item BOOLEAN DEFAULT TRUE,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    
    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id),
    FOREIGN KEY (parent_id) REFERENCES item(item_id),
    FOREIGN KEY (section_id) REFERENCES section(section_id),
    FOREIGN KEY (response_set_id) REFERENCES response_set(response_set_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    UNIQUE (item_id, crf_version_id)
);
```

**Key Fields:**
- `item_form_metadata_id` - Primary key
- `item_id` - Associated item
- `crf_version_id` - Associated CRF version
- `header` - Section header
- `subheader` - Section subheader
- `parent_id` - Parent item (for conditional display)
- `parent_label` - Parent item label
- `left_item_text` - Question text/label
- `right_item_text` - Units or suffix text
- `section_id` - Section containing this item
- `response_set_id` - Response options
- `regexp` - Validation regular expression
- `regexp_error_msg` - Validation error message
- `ordinal` - Display order
- `required` - Required field flag
- `default_value` - Default value
- `response_layout` - horizontal/vertical
- `show_item` - Visibility flag

#### **item_group** - Repeating Groups
```sql
CREATE TABLE item_group (
    item_group_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    crf_id INT NOT NULL,
    status_id INT NOT NULL,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    oc_oid VARCHAR(40),
    
    FOREIGN KEY (crf_id) REFERENCES crf(crf_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    UNIQUE (name, crf_id)
);
```

#### **item_group_metadata** - Group Configuration
```sql
CREATE TABLE item_group_metadata (
    item_group_metadata_id SERIAL PRIMARY KEY,
    item_group_id INT NOT NULL,
    header VARCHAR(255),
    subheader VARCHAR(255),
    layout VARCHAR(100),
    repeat_number INT,
    repeat_max INT,
    repeat_array VARCHAR(255),
    row_start_number INT,
    crf_version_id INT NOT NULL,
    item_id INT NOT NULL,
    ordinal INT NOT NULL,
    show_group BOOLEAN DEFAULT TRUE,
    borders INT DEFAULT 0,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    
    FOREIGN KEY (item_group_id) REFERENCES item_group(item_group_id),
    FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id),
    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id)
);
```

**Key Fields:**
- `layout` - Display layout (horizontal/vertical/grid)
- `repeat_number` - Initial number of rows
- `repeat_max` - Maximum number of rows
- `repeat_array` - Custom row labels
- `row_start_number` - Starting row number

#### **response_set** - Response Options
```sql
CREATE TABLE response_set (
    response_set_id SERIAL PRIMARY KEY,
    response_type_id INT NOT NULL,
    label VARCHAR(255),
    options_text TEXT,
    options_values TEXT,
    version_id INT,
    
    FOREIGN KEY (response_type_id) REFERENCES response_type(response_type_id)
);
```

**Key Fields:**
- `response_type_id` - Type (text, textarea, select, radio, checkbox, file, calculation)
- `options_text` - Display values (comma-separated)
- `options_values` - Stored values (comma-separated)

### 2. Study Event Integration Tables

#### **study_event_definition** - Study Visit Definitions
```sql
CREATE TABLE study_event_definition (
    study_event_definition_id SERIAL PRIMARY KEY,
    study_id INT NOT NULL,
    name VARCHAR(2000) NOT NULL,
    description VARCHAR(4000),
    repeating BOOLEAN DEFAULT FALSE,
    type VARCHAR(20),
    category VARCHAR(2000),
    ordinal INT NOT NULL,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    oc_oid VARCHAR(40),
    
    FOREIGN KEY (study_id) REFERENCES study(study_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    UNIQUE (study_id, name)
);
```

**Key Fields:**
- `study_event_definition_id` - Primary key
- `name` - Event name (e.g., "Screening", "Visit 1", "Week 4")
- `repeating` - Can occur multiple times
- `type` - Event type (scheduled/unscheduled/common)
- `ordinal` - Display order

#### **event_definition_crf** - CRF-Event Association
```sql
CREATE TABLE event_definition_crf (
    event_definition_crf_id SERIAL PRIMARY KEY,
    study_event_definition_id INT NOT NULL,
    study_id INT NOT NULL,
    crf_id INT NOT NULL,
    required_crf BOOLEAN DEFAULT FALSE,
    double_entry BOOLEAN DEFAULT FALSE,
    decision_condition_id INT,
    null_values TEXT,
    electronic_signature BOOLEAN DEFAULT FALSE,
    hide_crf BOOLEAN DEFAULT FALSE,
    source_data_verification_code INT,
    default_version_id INT,
    ordinal INT NOT NULL,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    parent_id INT,
    
    FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id),
    FOREIGN KEY (study_id) REFERENCES study(study_id),
    FOREIGN KEY (crf_id) REFERENCES crf(crf_id),
    FOREIGN KEY (default_version_id) REFERENCES crf_version(crf_version_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    FOREIGN KEY (parent_id) REFERENCES event_definition_crf(event_definition_crf_id),
    UNIQUE (study_event_definition_id, crf_id, study_id)
);
```

**Key Fields:**
- `event_definition_crf_id` - Primary key
- `study_event_definition_id` - Associated study event
- `crf_id` - Associated CRF
- `required_crf` - Required for event completion
- `double_entry` - Requires dual data entry
- `electronic_signature` - Requires e-signature
- `hide_crf` - Hidden from site users
- `source_data_verification_code` - SDV requirement level
- `default_version_id` - Default CRF version to use
- `ordinal` - Display order in event
- `parent_id` - Parent event definition CRF (for site overrides)

### 3. Data Collection Instance Tables

#### **study_event** - Subject Event Instances
```sql
CREATE TABLE study_event (
    study_event_id SERIAL PRIMARY KEY,
    study_event_definition_id INT NOT NULL,
    study_subject_id INT NOT NULL,
    location VARCHAR(2000),
    sample_ordinal INT,
    date_start DATE,
    date_end DATE,
    owner_id INT NOT NULL,
    status_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    subject_event_status_id INT,
    start_time_flag BOOLEAN,
    end_time_flag BOOLEAN,
    
    FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id),
    FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    FOREIGN KEY (subject_event_status_id) REFERENCES subject_event_status(subject_event_status_id)
);
```

**Key Fields:**
- `study_event_id` - Primary key
- `study_event_definition_id` - Event type
- `study_subject_id` - Subject
- `sample_ordinal` - Repetition number (for repeating events)
- `date_start` - Event start date
- `date_end` - Event end date
- `subject_event_status_id` - Status (scheduled, data entry started, completed, etc.)

#### **event_crf** - CRF Instance
```sql
CREATE TABLE event_crf (
    event_crf_id SERIAL PRIMARY KEY,
    study_event_id INT NOT NULL,
    crf_version_id INT NOT NULL,
    date_interviewed DATE,
    interviewer_name VARCHAR(255),
    completion_status_id INT,
    status_id INT NOT NULL,
    annotations TEXT,
    date_completed DATE,
    validator_id INT,
    date_validate DATE,
    date_validate_completed DATE,
    validator_annotations TEXT,
    validate_string VARCHAR(256),
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    sdv_status BOOLEAN,
    sdv_update_id INT,
    
    FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id),
    FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id),
    FOREIGN KEY (validator_id) REFERENCES user_account(user_id),
    FOREIGN KEY (completion_status_id) REFERENCES completion_status(completion_status_id)
);
```

**Key Fields:**
- `event_crf_id` - Primary key
- `study_event_id` - Parent event instance
- `crf_version_id` - CRF version used
- `date_interviewed` - Data collection date
- `interviewer_name` - Data collector
- `completion_status_id` - Status (initial data entry, complete, etc.)
- `validator_id` - Data validator user
- `sdv_status` - Source data verification status

### 4. Data Storage Tables

#### **item_data** - Collected Data Values
```sql
CREATE TABLE item_data (
    item_data_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    event_crf_id INT NOT NULL,
    status_id INT NOT NULL,
    value TEXT,
    owner_id INT NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    update_id INT,
    ordinal INT,
    
    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id),
    FOREIGN KEY (owner_id) REFERENCES user_account(user_id),
    FOREIGN KEY (update_id) REFERENCES user_account(user_id)
);
```

**Key Fields:**
- `item_data_id` - Primary key
- `item_id` - Item definition
- `event_crf_id` - CRF instance
- `value` - Stored value
- `ordinal` - Repetition number (for repeating groups)

### 5. Reference/Lookup Tables

#### **item_data_type** - Data Type Definitions
```sql
CREATE TABLE item_data_type (
    item_data_type_id INT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

INSERT INTO item_data_type VALUES
    (1, 'BL', 'Boolean'),
    (2, 'BN', 'Binary'),
    (3, 'ED', 'Encapsulated Data'),
    (4, 'TEL', 'Telephone'),
    (5, 'ST', 'Character String'),
    (6, 'INT', 'Integer'),
    (7, 'REAL', 'Floating'),
    (8, 'SET', 'Set Data Type'),
    (9, 'DATE', 'Date'),
    (10, 'PDATE', 'Partial Date'),
    (11, 'FILE', 'File');
```

#### **response_type** - Response Type Definitions
```sql
CREATE TABLE response_type (
    response_type_id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO response_type VALUES
    (1, 'text', 'Text Field'),
    (2, 'textarea', 'Text Area'),
    (3, 'select', 'Single-Select Dropdown'),
    (4, 'checkbox', 'Checkboxes (Multi-Select)'),
    (5, 'radio', 'Radio Buttons'),
    (6, 'file', 'File Upload'),
    (7, 'calculation', 'Instant Calculation'),
    (8, 'group-calculation', 'Group Calculation'),
    (9, 'multi-select', 'Multi-Select List');
```

## Data Model Relationships

### CRF Structure Hierarchy
```
study (1) ─── (∞) crf
crf (1) ─── (∞) crf_version
crf_version (1) ─── (∞) section
section (1) ─── (∞) item_form_metadata
item_form_metadata (∞) ─── (1) item
item (∞) ─── (∞) item_group_metadata ─── (1) item_group
```

### Event-CRF Association
```
study (1) ─── (∞) study_event_definition
study_event_definition (1) ─── (∞) event_definition_crf
event_definition_crf (∞) ─── (1) crf
event_definition_crf (∞) ─── (1) crf_version (default)
```

### Data Collection Flow
```
study_subject (1) ─── (∞) study_event
study_event (1) ─── (∞) event_crf
event_crf (∞) ─── (1) crf_version
event_crf (1) ─── (∞) item_data
item_data (∞) ─── (1) item
```

## Key Design Patterns

### 1. Versioning Pattern
- **CRF** is the master definition
- **CRF Version** allows evolution over time
- Old data remains linked to original version
- New data uses latest version

### 2. Metadata Pattern
- **Item** is the base definition (reusable)
- **ItemFormMetadata** is form-specific configuration
- Same item can appear in multiple CRFs with different metadata

### 3. Event-Driven Pattern
- CRFs are always collected within a **Study Event** context
- **EventDefinitionCRF** defines which CRFs belong to which events
- **StudyEvent** is a subject-specific instance of an event definition

### 4. Repeating Data Pattern
- **ItemGroup** defines repeatable groups
- **ItemGroupMetadata** specifies repeat configuration
- **item_data.ordinal** tracks which repetition

## Visit Grid Integration

The **Visit Grid** (Subject Matrix) displays:
```
           | Screening | Visit 1 | Visit 2 | Visit 3 |
-----------|-----------|---------|---------|---------|
Subject 01 |    ✓      |    ●    |    ○    |    ○    |
Subject 02 |    ✓      |    ✓    |    ●    |    ○    |
Subject 03 |    ✓      |    ✓    |    ✓    |    ●    |

Legend:
✓ = Event completed
● = Event in progress
○ = Event scheduled/not started
```

### How It Works:

1. **Study Event Definitions** define the columns (visits)
2. **Event Definition CRFs** define which CRFs appear in each visit
3. **Study Events** are created when subjects reach that visit
4. **Event CRFs** are created when user enters data for that CRF
5. **Item Data** records store the actual values

### Query Example:
```sql
-- Get all events and CRFs for a subject
SELECT 
    sed.name AS event_name,
    se.sample_ordinal,
    c.name AS crf_name,
    ec.completion_status_id,
    ec.date_completed
FROM study_subject ss
JOIN study_event se ON se.study_subject_id = ss.study_subject_id
JOIN study_event_definition sed ON sed.study_event_definition_id = se.study_event_definition_id
JOIN event_definition_crf edc ON edc.study_event_definition_id = sed.study_event_definition_id
JOIN crf c ON c.crf_id = edc.crf_id
LEFT JOIN event_crf ec ON ec.study_event_id = se.study_event_id 
    AND ec.crf_version_id IN (SELECT crf_version_id FROM crf_version WHERE crf_id = c.crf_id)
WHERE ss.label = 'Subject 01'
ORDER BY sed.ordinal, edc.ordinal;
```

## Data Integrity Constraints

### Cascading Deletes
- CRF deletion → Soft delete (status change)
- CRF Version deletion → Prevent if data exists
- Section deletion → Prevent if items exist
- Item deletion → Prevent if data exists

### Referential Integrity
- All foreign keys enforced
- Orphan records prevented
- Audit trail preserved

### Business Rules
1. CRF name must be unique within study
2. CRF Version name must be unique within CRF
3. Section label must be unique within CRF Version
4. Item name must be globally unique
5. Event Definition CRF must have default version
6. Item in form must have metadata record

## Migration Considerations

### Schema Modernization
1. **Add JSON Columns**: Store complex configurations as JSON
2. **Add Indexes**: Optimize common queries
3. **Normalize Further**: Separate audit fields into history table
4. **Add Constraints**: More CHECK constraints for validation
5. **Add Triggers**: Automatic timestamp updates
6. **Add Views**: Materialized views for performance

### NoSQL Alternatives
Consider document database for:
- CRF Version (as complete document)
- Section + Items (nested documents)
- Metadata configurations (flexible schema)

### Example MongoDB Schema:
```json
{
  "_id": "crf_12345",
  "name": "Demographics",
  "study_id": "study_001",
  "versions": [
    {
      "version_id": "v1",
      "name": "1.0",
      "sections": [
        {
          "label": "DEMO",
          "title": "Demographics",
          "items": [
            {
              "name": "AGE",
              "label": "Age",
              "type": "INT",
              "required": true,
              "validation": {"min": 0, "max": 120}
            }
          ]
        }
      ]
    }
  ]
}
```

## Performance Optimization

### Recommended Indexes
```sql
-- CRF lookups
CREATE INDEX idx_crf_study ON crf(study_id);
CREATE INDEX idx_crf_oid ON crf(oc_oid);

-- Version lookups
CREATE INDEX idx_version_crf ON crf_version(crf_id);

-- Section lookups
CREATE INDEX idx_section_version ON section(crf_version_id);
CREATE INDEX idx_section_ordinal ON section(crf_version_id, ordinal);

-- Item metadata lookups
CREATE INDEX idx_metadata_item ON item_form_metadata(item_id);
CREATE INDEX idx_metadata_version ON item_form_metadata(crf_version_id);
CREATE INDEX idx_metadata_section ON item_form_metadata(section_id);

-- Event CRF lookups
CREATE INDEX idx_event_crf_event ON event_crf(study_event_id);
CREATE INDEX idx_event_crf_version ON event_crf(crf_version_id);

-- Item data lookups
CREATE INDEX idx_item_data_event_crf ON item_data(event_crf_id);
CREATE INDEX idx_item_data_item ON item_data(item_id);
CREATE INDEX idx_item_data_ordinal ON item_data(event_crf_id, item_id, ordinal);
```

### Query Optimization
- Use prepared statements
- Limit result sets
- Paginate large lists
- Cache metadata
- Use connection pooling
