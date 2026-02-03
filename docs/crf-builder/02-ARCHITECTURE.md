# OpenClinica CRF Builder - Detailed Architecture

## System Architecture Overview

The CRF Builder follows a traditional Java enterprise application architecture with clear separation of concerns across presentation, business logic, and data access layers.

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   JSP    │  │JavaScript│  │   CSS    │  │   GWT    │   │
│  │  Pages   │  │  jQuery  │  │  Styles  │  │Components│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  ┌────────────────────────────────────────────────────┐     │
│  │          Servlets (extend SecureController)        │     │
│  │  CreateCRF, CreateCRFVersion, UpdateCRF, etc.     │     │
│  └────────────────────────────────────────────────────┘     │
│  ┌────────────────────────────────────────────────────┐     │
│  │          Form Processors & Validators              │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Spreadsheet│  │   Form   │  │   ODM    │  │  XForm   │   │
│  │ Preview  │  │ Builder  │  │Generation│  │ Parser   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Business Logic Layer                      │
│  ┌────────────────────────────────────────────────────┐     │
│  │      Bean Classes (Domain Model)                   │     │
│  │  CRFBean, ItemBean, SectionBean, etc.             │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                         │
│  ┌────────────────────────────────────────────────────┐     │
│  │      DAO Classes (JDBC-based)                      │     │
│  │  CRFDAO, ItemDAO, SectionDAO, etc.                │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                            │
│            PostgreSQL / Oracle Database                      │
└─────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Controller Layer

#### Core CRF Management Servlets
Located in: `/web/src/main/java/org/akaza/openclinica/control/admin/`

**CRF Creation & Management:**
- `CreateCRFServlet.java` - Creates new CRF metadata (name, description, OID)
- `UpdateCRFServlet.java` - Updates existing CRF metadata
- `InitCreateCRFServlet.java` - Initializes CRF creation workflow
- `InitUpdateCRFServlet.java` - Initializes CRF update workflow
- `ViewCRFServlet.java` - Displays CRF details and versions
- `ListCRFServlet.java` - Lists all CRFs in the system
- `RemoveCRFServlet.java` - Soft deletes a CRF
- `RestoreCRFServlet.java` - Restores a deleted CRF

**CRF Version Management:**
- `CreateCRFVersionServlet.java` - **Primary servlet for uploading Excel templates**
  - Handles file upload
  - Calls SpreadsheetPreview for parsing
  - Validates template structure
  - Creates database records
  - Manages transaction boundaries
- `InitCreateCRFVersionServlet.java` - Initializes version creation
- `DeleteCRFVersionServlet.java` - Deletes a CRF version
- `RemoveCRFVersionServlet.java` - Soft deletes version
- `RestoreCRFVersionServlet.java` - Restores deleted version
- `CreateXformCRFVersionServlet.java` - Handles XForm/OpenRosa format uploads

**CRF Export/Download:**
- `DownloadVersionSpreadSheetServlet.java` - Downloads CRF as Excel template
- `DownloadStudyMetadataServlet.java` - Exports ODM XML with CRF definitions
- `PrintCRFServlet.java` - Generates printable CRF version
- `PrintEventCRFServlet.java` - Prints specific event CRF instance

**Study Event Integration:**
- `AddCRFToDefinitionServlet.java` - Associates CRF with study event
- `RemoveCRFFromDefinitionServlet.java` - Removes CRF from event
- `LockCRFVersionServlet.java` - Locks CRF version (prevents edits)
- `UnlockCRFVersionServlet.java` - Unlocks CRF version

**Batch Operations:**
- `BatchCRFMigrationServlet.java` - Bulk CRF migration operations
- `BatchCRFMigrationController.java` - Spring MVC controller for batch operations

#### Request Flow Example: Creating a CRF Version

```
1. User clicks "Create New Version" → InitCreateCRFVersionServlet
   ↓
2. User uploads Excel file → CreateCRFVersionServlet (action=confirm)
   ↓
3. CreateCRFVersionServlet processes:
   - Validates file upload
   - Calls SpreadsheetPreview.createCrfMetaObject()
   - Displays preview page (createCRFVersionConfirm.jsp)
   ↓
4. User confirms → CreateCRFVersionServlet (action=submit)
   ↓
5. CreateCRFVersionServlet:
   - Begins transaction
   - Creates CRFVersionBean
   - Creates SectionBeans (via SectionDAO)
   - Creates ItemBeans (via ItemDAO)
   - Creates ItemGroupBeans (via ItemGroupDAO)
   - Creates ItemFormMetadataBeans (via ItemFormMetadataDAO)
   - Commits transaction
   - Stores Excel file on disk
   ↓
6. Success page displayed (createCRFVersionDone.jsp)
```

### 2. Service Layer

#### Spreadsheet Processing
Located in: `/web/src/main/java/org/akaza/openclinica/control/admin/`

**SpreadsheetPreview.java** - Core Excel parsing service
```java
Key Methods:
- createCrfMetaObject(HSSFWorkbook) → Map<String, Map>
  Returns: {
    "crf_info": {...},
    "sections": {...},
    "items": {...},
    "groups": {...}
  }
  
- createItemsOrSectionMap(HSSFWorkbook, String) → Map<Integer, Map<String, String>>
  Parses "Items" or "Sections" sheet
  
- createGroupsMap(HSSFWorkbook) → Map<Integer, Map<String, String>>
  Parses "Groups" sheet
  
- createCrfMap(HSSFWorkbook) → Map<String, String>
  Parses "CRF" sheet metadata
```

**SpreadsheetPreviewNw.java** - Newer version with enhancements

**Template Validation:**
- `SpreadSheetValidator.java` - Validates template structure
- `SpreadSheetTable.java` - HTML table rendering for preview
- `SpreadSheetTableClassic.java` - Classic layout rendering
- `SpreadSheetTableRepeating.java` - Repeating group rendering

#### Form Rendering
Located in: `/web/src/main/java/org/akaza/openclinica/view/form/`

**FormBuilder Hierarchy:**
```
FormBuilder (interface)
  └── DefaultFormBuilder (abstract base)
       ├── HorizontalFormBuilder
       ├── VerticalFormBuilder
       └── PrintHorizontalFormBuilder
```

**FormBuilder.java** - Interface defining form rendering contract
```java
Methods:
- buildForm() → String HTML
- createTable(int, int) → String
- addTableRow(String...) → void
- addFormElement(ItemBean) → void
```

**DefaultFormBuilder.java** - Base implementation
- Handles common rendering logic
- Manages form element generation
- Implements table-based layouts

**HorizontalFormBuilder.java** - Renders items horizontally
- Used for compact layouts
- Multiple items per row

**VerticalFormBuilder.java** - Renders items vertically
- Default layout
- One item per row
- Better for long forms

**PrintHorizontalFormBuilder.java** - Print-optimized rendering
- Removes interactive elements
- Optimized for PDF/print output

#### Data Entry Input Generation
Located in: `/web/src/main/java/org/akaza/openclinica/control/submit/`

**DataEntryInputGenerator.java**
- Generates HTML input elements based on response type
- Handles validation attributes
- Supports all response types: text, textarea, select, radio, checkbox, file, calculation

#### ODM Export Services
Located in: `/core/src/main/java/org/akaza/openclinica/service/`

**OdmFileCreation.java** - Main ODM export service
- Orchestrates ODM XML generation
- Handles metadata, clinical data, and admin data sections
- CDISC ODM 1.3 compliant

**MetaDataCollector.java** - Collects CRF metadata for ODM
- Gathers CRF definitions
- Collects item definitions
- Builds code lists
- Generates measurement units

**ClinicalDataCollector.java** - Collects clinical data
- Retrieves event CRF data
- Formats item data for export

**AdminDataCollector.java** - Collects administrative data
- User information
- Location data
- Audit information

**REST API Controllers:**
Located in: `/web/src/main/java/org/akaza/openclinica/web/restful/`
- `ODMMetadataRestResource.java` - REST endpoint for ODM metadata
- `ODMClinicaDataResource.java` - REST endpoint for clinical data
- `OdmController.java` - Spring MVC controller for ODM

### 3. Domain Model (Bean Classes)

Located in: `/core/src/main/java/org/akaza/openclinica/bean/`

#### Core CRF Beans (`admin` package)
**CRFBean.java**
```java
Fields:
- int id
- String name
- String description
- int statusId
- String oid (unique identifier)
- int studyId
- Date dateCreated
- Date dateUpdated
- int ownerId
- int updaterId
- ArrayList<CRFVersionBean> versions
```

**NewCRFBean.java** - Extends CRFBean with validation

#### CRF Version & Structure (`submit` package)
**CRFVersionBean.java**
```java
Fields:
- int id
- int crfId
- String name
- String description
- String revisionNotes
- int statusId
- String oid
- ArrayList<SectionBean> sections
```

**SectionBean.java**
```java
Fields:
- int id
- int crfVersionId
- String label (identifier)
- String title (display name)
- String subtitle
- String instructions
- String pageNumberLabel
- int ordinal (sort order)
- int parentId (for nested sections)
- int borders
- ArrayList<ItemBean> items
- ArrayList<ItemGroupBean> groups
```

**ItemBean.java**
```java
Fields:
- int id
- String name (identifier)
- String description
- String units
- boolean phiStatus (Protected Health Information)
- int itemDataTypeId (ST, INT, REAL, DATE, etc.)
- ItemDataType dataType
- int itemReferenceTypeId
- String oid
- ItemFormMetadataBean itemMeta
- ArrayList<ItemFormMetadataBean> itemMetas
```

**ItemGroupBean.java**
```java
Fields:
- int id
- String name
- int crfId
- String oid
- ArrayList<ItemBean> items
- ItemGroupMetadataBean meta
```

#### Item Configuration (`submit` package)
**ItemFormMetadataBean.java** - Form-specific item configuration
```java
Fields:
- int id
- int itemId
- int crfVersionId
- String header
- String subHeader
- int parentId (for conditional display)
- String parentLabel
- int columnNumber
- String pageNumberLabel
- String questionNumberLabel
- String leftItemText (label)
- String rightItemText (suffix)
- int sectionId
- String responseSetId
- String responseLayout (horizontal/vertical)
- String defaultValue
- String regexpErrorMsg
- String regexp (validation pattern)
- int ordinal
- boolean required
- boolean showItem
- int widthDecimal
```

**ItemGroupMetadataBean.java** - Group configuration
```java
Fields:
- int id
- int itemGroupId
- String header
- String subheader
- String layout (horizontal/vertical/grid)
- int repeatNum (initial rows)
- int repeatMax (maximum rows)
- String repeatArray (custom repeat labels)
- int rowStartNumber
- int crfVersionId
- int ordinal
- boolean borders
```

**ResponseSetBean.java** - Response options
```java
Fields:
- int id
- int responseTypeId (text, select, radio, etc.)
- String label
- String optionsText (display values)
- String optionsValues (stored values)
- ArrayList<ResponseOptionBean> options
```

#### Display Beans (View Models)
**DisplayEventCRFBean.java** - Event CRF instance display
**DisplaySectionBean.java** - Section display with status info
**DisplayItemBean.java** - Item display with data and metadata
**DisplayItemGroupBean.java** - Item group display

### 4. Data Access Layer (DAO)

Located in: `/core/src/main/java/org/akaza/openclinica/dao/`

#### DAO Pattern Implementation
Each DAO extends `AuditableEntityDAO<T>` and implements standard CRUD operations:
- `create(T bean)` → EntityBean
- `update(T bean)` → EntityBean
- `findByPK(int id)` → EntityBean
- `findAll()` → ArrayList<EntityBean>
- `delete(int id)` → void

#### Core CRF DAOs (`admin` package)
**CRFDAO.java**
```java
Key Methods:
- create(CRFBean) → CRFBean
- update(CRFBean) → CRFBean
- findByPK(int) → CRFBean
- findAllByStudy(StudyBean) → ArrayList<CRFBean>
- findByNameAndStudy(String, StudyBean) → CRFBean
- findAllByOid(String) → ArrayList<CRFBean>
```

**CRFVersionDAO.java**
```java
Key Methods:
- create(CRFVersionBean) → CRFVersionBean
- findAllByCRF(int crfId) → ArrayList<CRFVersionBean>
- findByOid(String) → CRFVersionBean
- findByCRFId(int) → ArrayList<CRFVersionBean>
- findByFullName(String, String) → CRFVersionBean
```

#### Structure DAOs (`submit` package)
**SectionDAO.java**
```java
Key Methods:
- create(SectionBean) → SectionBean
- findAllByCRFVersionId(int) → ArrayList<SectionBean>
- findByOrdinal(int crfVersionId, int ordinal) → SectionBean
```

**ItemDAO.java**
```java
Key Methods:
- create(ItemBean) → ItemBean
- findByName(String) → ItemBean
- findByOid(String) → ItemBean
- findAllWithItemFormMetadata(int crfVersionId) → ArrayList<ItemBean>
- findAllBySectionId(int) → ArrayList<ItemBean>
```

**ItemFormMetadataDAO.java**
```java
Key Methods:
- create(ItemFormMetadataBean) → ItemFormMetadataBean
- findAllByCRFVersionId(int) → ArrayList<ItemFormMetadataBean>
- findAllByItemId(int) → ArrayList<ItemFormMetadataBean>
- findByItemIdAndCRFVersionId(int, int) → ItemFormMetadataBean
```

**ItemGroupDAO.java**
**ItemGroupMetadataDAO.java**

#### Event & Data DAOs
**EventCRFDAO.java** - Event CRF instances
**EventDefinitionCRFDAO.java** - CRF-Event associations
**ItemDataDAO.java** - Actual data values

### 5. XForm/OpenRosa Support

Located in: `/core/src/main/java/org/akaza/openclinica/domain/xform/`

**XformParser.java** - Parses XForm XML
**XformContainer.java** - XForm document model
**XformGroup.java** - XForm group representation
**XformItem.java** - XForm item representation
**XformMetaDataService.java** - XForm metadata service
**OpenRosaServices.java** - OpenRosa API services
**OpenRosaXmlGenerator.java** - Generates OpenRosa XML

### 6. Presentation Layer

#### JSP Pages
Located in: `/web/src/main/webapp/WEB-INF/jsp/admin/`

**CRF Management Pages:**
- `createCRF.jsp` - Form to create new CRF
- `createCRFConfirm.jsp` - Confirm CRF creation
- `updateCRF.jsp` - Form to update CRF
- `updateCRFConfirm.jsp` - Confirm CRF update
- `viewCRF.jsp` - Display CRF details and versions
- `listCRF.jsp` - List all CRFs with search/filter
- `removeCRF.jsp` - Confirm CRF removal
- `restoreCRF.jsp` - Confirm CRF restoration

**CRF Version Pages:**
- `createCRFVersion.jsp` - Upload Excel template form
- `uploadCRFVersionFile.jsp` - File upload handler
- `createCRFVersionConfirm.jsp` - Preview uploaded CRF
- `createCRFVersionDone.jsp` - Success page
- `createCRFVersionError.jsp` - Error page with validation messages
- `removeCRFVersion.jsp` - Confirm version removal
- `restoreCRFVersion.jsp` - Confirm version restoration

#### JavaScript Components
Located in: `/web/src/main/webapp/includes/` and `/web/src/main/webapp/js/`

- Form validation scripts
- Dynamic form behaviors
- Repeating group management
- Conditional display logic
- Data entry helpers

#### CSS Styling
Located in: `/web/src/main/webapp/includes/styles/`
- Form layout styles
- Responsive design (limited)
- Print styles

## Design Patterns Used

### 1. MVC (Model-View-Controller)
- **Model**: Bean classes
- **View**: JSP pages
- **Controller**: Servlet classes

### 2. DAO (Data Access Object)
- Abstracts database access
- One DAO per entity type

### 3. Builder Pattern
- FormBuilder for dynamic form generation
- Different builders for different layouts

### 4. Factory Pattern
- DAOFactory for creating DAO instances
- Used in Spring configuration

### 5. Template Method
- SecureController defines template for request handling
- Subclasses implement mayProceed() and processRequest()

### 6. Singleton
- DAOs are Spring-managed singletons
- Service classes are singletons

## Key Workflows

### CRF Creation Workflow
```
1. User: Navigate to Create CRF
2. System: Display createCRF.jsp
3. User: Enter name, description
4. System: CreateCRFServlet.processRequest()
   - Validate input
   - Check for duplicates
   - CRFDAO.create()
   - Generate OID
5. System: Display success message
```

### CRF Version Upload Workflow
```
1. User: Select CRF, click "Create Version"
2. System: InitCreateCRFVersionServlet
   - Display createCRFVersion.jsp
3. User: Upload Excel file
4. System: CreateCRFVersionServlet (action=confirm)
   - FileUploadHelper.parse()
   - SpreadsheetPreview.createCrfMetaObject()
   - Validate template structure
   - Display createCRFVersionConfirm.jsp with preview
5. User: Review and confirm
6. System: CreateCRFVersionServlet (action=submit)
   - Begin transaction
   - CRFVersionDAO.create()
   - For each section: SectionDAO.create()
   - For each item: ItemDAO.create()
   - For each group: ItemGroupDAO.create()
   - For each item metadata: ItemFormMetadataDAO.create()
   - Commit transaction
   - Save Excel file to disk
7. System: Display createCRFVersionDone.jsp
```

### Form Rendering Workflow
```
1. User: Access data entry form
2. System: DataEntryServlet.processRequest()
   - EventCRFDAO.findByPK()
   - Load sections and items
   - Create DisplaySectionBeans
3. System: FormBuilder.buildForm()
   - For each section:
     - For each item:
       - DataEntryInputGenerator.generate()
       - Apply validation rules
       - Set default values
       - Handle conditional display
4. System: Render JSP with form HTML
```

### ODM Export Workflow
```
1. User: Request metadata export
2. System: DownloadStudyMetadataServlet
   - OdmFileCreation.createOdmXml()
3. System: MetaDataCollector.collectMetadata()
   - Collect study info
   - For each CRF:
     - Collect CRF definition
     - For each version:
       - Collect sections
       - Collect items
       - Collect code lists
       - Collect measurement units
4. System: Generate ODM XML structure
5. System: Return XML file to user
```

## Technology Considerations for Migration

### Current Limitations
1. **Monolithic Architecture**: Tightly coupled components
2. **JSP/Servlet Technology**: Dated presentation layer
3. **No REST APIs**: Limited programmatic access (some added recently)
4. **JDBC DAO Pattern**: Not using modern ORM fully
5. **Server-Side Rendering**: No SPA capabilities
6. **Limited Mobile Support**: Not responsive
7. **Excel Dependency**: Requires desktop tool for design

### Strengths to Preserve
1. **Robust Data Model**: Well-designed entity relationships
2. **ODM Compliance**: Industry standard support
3. **Validation Engine**: Comprehensive validation
4. **Versioning**: Good version control
5. **Audit Trail**: Complete history tracking
6. **Transaction Management**: Proper ACID support

### Migration Targets
1. **Spring Boot**: Modern Spring framework
2. **REST API**: Full RESTful API
3. **React/Angular/Vue**: Modern SPA frontend
4. **Microservices**: Separate CRF service
5. **GraphQL**: Flexible data querying
6. **Docker/Kubernetes**: Containerization
7. **MongoDB/PostgreSQL**: Modern data storage
8. **Web-Based Designer**: Visual form builder
