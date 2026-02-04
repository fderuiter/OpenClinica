# OpenClinica CRF Builder - Comprehensive Overview

## Executive Summary

The Case Report Form (CRF) Builder is a core component of OpenClinica that enables clinical researchers to design, create, and manage electronic data collection forms (eCRFs) for clinical trials. This document provides a comprehensive analysis of the CRF builder functionality to support migration to a different framework.

## What is a CRF?

A Case Report Form (CRF) in OpenClinica is:
- An electronic data collection instrument used to capture clinical trial data
- Organized into **Sections** (pages) containing **Items** (questions/fields)
- Supports **Item Groups** (repeating groups of items)
- Version-controlled (multiple versions of the same CRF can exist)
- Associated with Study Events in a clinical trial workflow
- Supports both traditional forms and OpenRosa/XForm mobile data collection

## Core Concepts

### 1. CRF Hierarchy
```
Study
  └── Study Event Definition
       └── Event Definition CRF (association)
            └── CRF (instrument definition)
                 └── CRF Version (specific version)
                      ├── Sections (pages/groups)
                      │    └── Items (fields/questions)
                      └── Item Groups (repeating data)
                           └── Items (fields in group)
```

### 2. Key Entities

- **CRF**: The form definition (e.g., "Demographics CRF", "Adverse Events CRF")
- **CRF Version**: A specific version of the CRF (supports versioning/evolution)
- **Section**: A page or logical grouping within a CRF
- **Item**: A single data field/question (e.g., "Patient Age", "Blood Pressure")
- **Item Group**: A repeating group of items (e.g., multiple medication entries)
- **Item Metadata**: Form-specific configuration (validation, display settings)
- **Event CRF**: An instance of a CRF filled out for a specific study event

### 3. Data Flow

```
Design Phase:
1. Create CRF metadata (name, description)
2. Upload Excel template OR create XForm
3. System parses template and creates:
   - CRF Version
   - Sections
   - Items
   - Item Groups
   - Item Metadata

Data Collection Phase:
4. CRF associated with Study Events
5. Event CRF instances created for subjects
6. Data entered through form rendering
7. Data stored in item_data table

Export Phase:
8. CRF definitions exported as:
   - ODM XML (metadata)
   - Excel templates
   - PDF prints
9. Data exported in multiple formats
```

## Technology Stack

### Backend
- **Java** (Primary language)
- **Spring Framework** (MVC, Dependency Injection)
- **Hibernate/JPA** (ORM for newer components)
- **JDBC** (DAO layer for legacy components)
- **PostgreSQL/Oracle** (Database)
- **Apache POI** (Excel file processing)
- **JAXB** (XML marshalling/unmarshalling)

### Frontend
- **JSP** (JavaServer Pages for UI)
- **JavaScript/jQuery** (Client-side scripting)
- **JSTL** (JSP Standard Tag Library)
- **CSS** (Styling)
- **GWT** (Google Web Toolkit for some components)

### Standards & Formats
- **CDISC ODM 1.3** (Operational Data Model for clinical data exchange)
- **OpenRosa/XForm** (Mobile data collection standard)
- **Excel XLS** (CRF template format)
- **REST APIs** (Data export endpoints)

## Architecture Patterns

### 1. MVC Pattern
- **Model**: Bean classes (CRFBean, ItemBean, SectionBean)
- **View**: JSP pages with JSTL
- **Controller**: Servlet classes extending SecureController

### 2. DAO Pattern
- Data Access Objects for database operations
- One DAO per entity type (CRFDAO, ItemDAO, etc.)

### 3. Builder Pattern
- FormBuilder classes for rendering forms
- Different builders for different layouts (Horizontal, Vertical, Print)

## Key Features

### CRF Design Features
1. **Excel-based CRF Design**: Users design CRFs in Excel spreadsheets
2. **Template Validation**: Validates template structure and content
3. **Version Control**: Multiple versions of same CRF
4. **Section Organization**: Organize items into logical pages
5. **Item Groups**: Support for repeating data (e.g., multiple medications)
6. **Data Types**: ST (string), INT (integer), REAL, DATE, PDATE (partial date), FILE
7. **Response Types**: text, textarea, select, multi-select, radio, checkbox, file, instant calculation
8. **Validation**: Regular expressions, range checks, required fields
9. **PHI Marking**: Mark items as Protected Health Information
10. **Conditional Display**: Show/hide items based on other item values
11. **Default Values**: Pre-populate fields with default values
12. **Units**: Specify measurement units for items
13. **Instructions**: Section and item-level instructions
14. **Layouts**: Horizontal and vertical item layouts

### Data Collection Features
1. **Form Rendering**: Dynamic HTML form generation from metadata
2. **Real-time Validation**: Client and server-side validation
3. **Save & Exit**: Partial form completion support
4. **Data Entry Rules**: Complex business rules
5. **Discrepancy Notes**: Query management system
6. **Audit Trail**: Complete change history
7. **Electronic Signatures**: Signed/locked forms
8. **Batch Upload**: Import data from files

### Export Features
1. **ODM XML Export**: CDISC-compliant metadata export
2. **Excel Template Download**: Export CRF as editable template
3. **PDF Print**: Printable form versions
4. **Data Export**: Multiple formats (CSV, SAS, SPSS, Tab-delimited)
5. **REST API**: Programmatic access to CRF metadata
6. **Dataset Export**: Scheduled data extracts with CRF definitions

## Migration Considerations

### What Must Be Preserved
1. **Data Model Integrity**: All CRF, Section, Item relationships
2. **Validation Logic**: All validation rules and error messages
3. **ODM Export Compatibility**: Maintain ODM 1.3 compliance
4. **Version History**: CRF version tracking
5. **Audit Trail**: Complete change history
6. **Excel Template Format**: Backward compatibility with existing templates

### Modernization Opportunities
1. **Web-based Designer**: Replace Excel with visual form builder
2. **Modern UI Framework**: React, Angular, or Vue.js
3. **API-First Design**: RESTful APIs for all operations
4. **Microservices**: Separate CRF service from main application
5. **Real-time Collaboration**: Multiple users designing forms
6. **Template Library**: Pre-built form components
7. **Drag-and-Drop Interface**: Visual form design
8. **Responsive Design**: Mobile-friendly form rendering

### Complexity Areas
1. **Repeating Groups**: Complex rendering and data model
2. **Conditional Display**: Client-side logic dependencies
3. **Calculations**: Instant calculations between items
4. **Import/Export**: Multiple format support
5. **Backward Compatibility**: Support existing CRFs
6. **Validation Engine**: Complex rule evaluation
7. **Discrepancy Management**: Integration with query system

## File Organization

See companion documents:
- `02-ARCHITECTURE.md` - Detailed architecture and component breakdown
- `03-DATA-MODEL.md` - Database schema and entity relationships
- `04-FEATURES-SPECIFICATIONS.md` - Detailed feature specifications
- `05-EXPORT-IMPORT.md` - Export and import capabilities
- `06-CODE-INVENTORY.md` - Complete file and class inventory
- `07-MIGRATION-PLAN.md` - Framework migration strategy and roadmap

## Quick Reference

**Primary Entry Points:**
- Create CRF: `CreateCRFServlet.java`
- Create CRF Version: `CreateCRFVersionServlet.java`
- Upload Template: `SpreadsheetPreview.java`
- Download Template: `DownloadVersionSpreadSheetServlet.java`
- Export ODM: `DownloadStudyMetadataServlet.java`

**Core Data Beans:**
- `CRFBean.java` - CRF definition
- `CRFVersionBean.java` - CRF version
- `SectionBean.java` - Section/page
- `ItemBean.java` - Item/question
- `ItemGroupBean.java` - Repeating group

**Database Tables (key):**
- `crf` - CRF definitions
- `crf_version` - CRF versions
- `section` - Sections
- `item` - Items
- `item_group_metadata` - Groups
- `item_form_metadata` - Item display config
- `event_crf` - Form instances
- `item_data` - Collected data
