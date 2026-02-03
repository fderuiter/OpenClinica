# OpenClinica CRF Builder - Visit Grid Integration

## Overview

The Visit Grid (also called Subject Matrix or Study Matrix) is the central dashboard in OpenClinica that shows the intersection of:
- **Subjects** (rows) - Study participants
- **Study Events** (columns) - Scheduled visits/time points
- **CRFs** (cells) - Forms to be completed at each visit

The CRF Builder's output (CRF definitions) is tightly integrated with this grid to orchestrate data collection workflows.

## Visual Representation

### Subject Matrix View
```
┌────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
│ Subject ID │  Screening   │   Visit 1    │   Visit 2    │   Visit 3    │
├────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│ S-001      │ [✓✓✓✓]       │ [✓✓●○]       │ [○○○○]       │ [○○○○]       │
│            │ 2023-01-15   │ 2023-02-01   │ Scheduled    │ Not Started  │
├────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│ S-002      │ [✓✓✓✓]       │ [✓✓✓✓]       │ [✓✓●○]       │ [○○○○]       │
│            │ 2023-01-20   │ 2023-02-10   │ 2023-03-05   │ Scheduled    │
├────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│ S-003      │ [✓✓✓✓]       │ [✓✓✓✓]       │ [✓✓✓✓]       │ [✓✓●○]       │
│            │ 2023-01-25   │ 2023-02-15   │ 2023-03-10   │ 2023-04-01   │
└────────────┴──────────────┴──────────────┴──────────────┴──────────────┘

Legend for each cell [CRF Status Icons]:
✓ = CRF Completed
● = CRF In Progress  
○ = CRF Not Started
□ = CRF Not Required
```

### Expanded Event Cell View
When you click on a cell (e.g., "Visit 1" for "S-001"), you see:
```
┌─────────────────────────────────────────────────────────────┐
│ Visit 1 - Subject S-001                                      │
├─────────────────────────────────────────────────────────────┤
│ Event Date: 2023-02-01                                       │
│ Location: Main Clinic                                        │
│ Status: Data Entry Started                                   │
├─────────────────────────────────────────────────────────────┤
│ CRF Name              │ Status      │ Actions               │
├───────────────────────┼─────────────┼───────────────────────┤
│ Demographics          │ Complete ✓  │ [View] [Edit] [Print] │
│ Medical History       │ Complete ✓  │ [View] [Edit] [Print] │
│ Vital Signs           │ In Progress │ [Resume] [Print]      │
│ Adverse Events        │ Not Started │ [Start] [Print]       │
└───────────────────────┴─────────────┴───────────────────────┘
```

## Architecture: How CRFs Connect to the Visit Grid

### 1. Study Event Definition (Visit Type)
**Database:** `study_event_definition`

Defines the **columns** of the visit grid:
- Visit name (e.g., "Screening", "Week 4", "Month 6")
- Repeating or non-repeating
- Display order (ordinal)

```java
public class StudyEventDefinitionBean {
    private int studyEventDefinitionId;
    private String name;              // "Screening", "Visit 1"
    private String description;
    private boolean repeating;        // Can occur multiple times?
    private String type;              // scheduled, unscheduled, common
    private int ordinal;              // Display order in grid
    private String oid;
}
```

### 2. Event Definition CRF (CRF-Visit Association)
**Database:** `event_definition_crf`

Defines which CRFs appear in which visits:
```java
public class EventDefinitionCRFBean {
    private int studyEventDefinitionId;  // Which visit
    private int crfId;                    // Which CRF
    private int defaultVersionId;         // Which version to use
    private boolean requiredCRF;          // Must be completed?
    private int ordinal;                  // Display order within visit
    private boolean doubleEntry;          // Requires dual entry?
    private boolean electronicSignature;  // Requires e-signature?
    private boolean hideCrf;              // Hidden from sites?
    private int sourceDataVerification;   // SDV requirement level
}
```

**Key Operations:**
- **Add CRF to Event:** `AddCRFToDefinitionServlet.java`
- **Remove CRF from Event:** `RemoveCRFFromDefinitionServlet.java`
- **Change CRF Order:** `ChangeDefinitionCRFOrdinalServlet.java`

### 3. Study Event (Subject Visit Instance)
**Database:** `study_event`

Represents an actual visit for a specific subject:
```java
public class StudyEventBean {
    private int studyEventId;
    private int studyEventDefinitionId;   // Which visit type
    private int studySubjectId;           // Which subject
    private int sampleOrdinal;            // Repetition # (for repeating events)
    private Date dateStart;               // Visit date
    private Date dateEnd;
    private String location;
    private int subjectEventStatusId;     // scheduled, started, completed, etc.
}
```

### 4. Event CRF (CRF Instance)
**Database:** `event_crf`

Represents one CRF filled out for one visit for one subject:
```java
public class EventCRFBean {
    private int eventCRFId;
    private int studyEventId;             // Parent visit instance
    private int crfVersionId;             // Which version used
    private Date dateInterviewed;
    private String interviewerName;
    private int completionStatusId;       // initial, in progress, complete
    private Date dateCompleted;
    private int validatorId;
    private boolean sdvStatus;
}
```

## Data Flow: From CRF Design to Visit Grid

### Phase 1: Study Design (Setup)
```
1. Create CRF (via CRF Builder)
   ↓
2. Upload CRF Version (Excel template)
   ↓
3. System creates: crf → crf_version → sections → items
   ↓
4. Define Study Events (visits)
   ↓
5. Associate CRFs with Events
   ↓
6. event_definition_crf records created
```

**Example Workflow:**
```sql
-- Step 1: CRF exists
INSERT INTO crf (name, description, study_id) 
VALUES ('Demographics', 'Patient demographics', 101);

-- Step 2: CRF Version uploaded
INSERT INTO crf_version (crf_id, name) 
VALUES (1, 'v1.0');

-- Step 3: Sections and items created (by system during upload)

-- Step 4: Study events defined
INSERT INTO study_event_definition (study_id, name, ordinal) 
VALUES (101, 'Screening', 1);
INSERT INTO study_event_definition (study_id, name, ordinal) 
VALUES (101, 'Visit 1', 2);

-- Step 5: Associate Demographics CRF with Screening
INSERT INTO event_definition_crf 
    (study_event_definition_id, crf_id, required_crf, ordinal) 
VALUES (1, 1, true, 1);
```

### Phase 2: Subject Enrollment
```
1. Subject enrolled in study
   ↓
2. System creates study_subject record
   ↓
3. System schedules events
   ↓
4. study_event records created (one per visit)
   ↓
5. Visit Grid displays scheduled visits
```

### Phase 3: Data Collection
```
1. User clicks on visit cell in grid
   ↓
2. System shows list of CRFs for that event
   ↓
3. User clicks "Start Data Entry" for a CRF
   ↓
4. System creates event_crf record
   ↓
5. System renders form using FormBuilder
   ↓
6. User enters data
   ↓
7. System saves to item_data table
   ↓
8. Visit Grid updates status indicators
```

## Key Components

### Backend: Visit Grid Data Generation

**ListEventsForSubjectsServlet.java**
- Main servlet for subject matrix display
- Queries all subjects and their events
- Builds grid data structure

```java
// Simplified pseudo-code
public void processRequest() {
    // Get all subjects
    List<StudySubjectBean> subjects = studySubjectDAO.findAllByStudy(studyId);
    
    // Get all event definitions
    List<StudyEventDefinitionBean> eventDefs = 
        eventDefinitionDAO.findAllByStudy(studyId);
    
    // For each subject
    for (StudySubjectBean subject : subjects) {
        // Get their events
        List<StudyEventBean> events = 
            eventDAO.findAllBySubject(subject.getId());
        
        // For each event definition
        for (StudyEventDefinitionBean eventDef : eventDefs) {
            // Find matching event instance
            StudyEventBean event = findEvent(events, eventDef.getId());
            
            if (event != null) {
                // Get CRFs for this event
                List<EventCRFBean> crfs = 
                    eventCRFDAO.findAllByEvent(event.getId());
                
                // Calculate completion status
                int totalCRFs = countExpectedCRFs(eventDef.getId());
                int completedCRFs = countCompletedCRFs(crfs);
                
                // Add to grid data
                gridData.addCell(subject, eventDef, 
                    completedCRFs, totalCRFs, event.getStatus());
            }
        }
    }
    
    // Render grid
    request.setAttribute("gridData", gridData);
    forwardPage(Page.SUBJECT_MATRIX);
}
```

**ViewStudySubjectServlet.java**
- Individual subject timeline view
- Shows all events for one subject
- Displays CRF completion status per event

**EventCrfLayerBuilder.java**
- Builds the visual layers for event cells
- Handles status calculations
- Generates HTML/CSS for status indicators

### Frontend: Visit Grid Display

**JSP Pages:**
- `listEventsForSubjects.jsp` - Main grid view
- `listEventsForSubject.jsp` - Single subject view
- `showEventsForSubjectRow.jsp` - Subject row renderer
- `enterDataForStudyEvent.jsp` - Data entry page for event

**JavaScript:**
- Grid interaction handlers
- Status updates
- AJAX for real-time updates (in newer versions)

### Visit Grid Status Calculation

```java
// EventCrfLayerBuilder.java simplified logic
public String calculateEventStatus(StudyEventBean event) {
    List<EventDefinitionCRFBean> expectedCRFs = 
        getExpectedCRFs(event.getStudyEventDefinitionId());
    
    List<EventCRFBean> actualCRFs = 
        getActualCRFs(event.getId());
    
    int total = expectedCRFs.size();
    int completed = 0;
    int inProgress = 0;
    
    for (EventDefinitionCRFBean expectedCRF : expectedCRFs) {
        EventCRFBean actualCRF = findMatchingCRF(actualCRFs, expectedCRF);
        
        if (actualCRF != null) {
            if (actualCRF.isComplete()) {
                completed++;
            } else {
                inProgress++;
            }
        }
    }
    
    if (completed == total) return "COMPLETE";
    if (inProgress > 0 || completed > 0) return "IN_PROGRESS";
    return "NOT_STARTED";
}
```

## CRF Requirements in Events

### Required vs Optional CRFs
Set via `event_definition_crf.required_crf`:
- **Required (true)**: Must be completed before event is complete
- **Optional (false)**: Can skip without blocking event completion

### Default Version Selection
Set via `event_definition_crf.default_version_id`:
- Specifies which CRF version to use by default
- Can be changed at data entry time
- Important for studies with evolving forms

### CRF Ordering
Set via `event_definition_crf.ordinal`:
- Determines display order within event
- Can be changed via `ChangeDefinitionCRFOrdinalServlet`

### Site-Specific Overrides
Set via `event_definition_crf.hide_crf` and `parent_id`:
- Parent study defines CRFs
- Sites can hide CRFs not applicable to them
- Enables multi-site studies with variation

## User Workflows

### Study Designer Workflow: Adding CRF to Event

```
1. Navigate to Study Setup → Study Event Definitions
   ↓
2. Click on event (e.g., "Screening")
   ↓
3. Click "Add CRF to Event"
   ↓
4. AddCRFToDefinitionServlet displays form
   ↓
5. Select:
   - CRF
   - Default version
   - Required/Optional
   - Double entry
   - E-signature
   - SDV level
   ↓
6. Submit
   ↓
7. System creates event_definition_crf record
   ↓
8. Visit Grid now shows this CRF in this event
```

### Data Manager Workflow: Data Entry

```
1. Navigate to Subject Matrix
   ↓
2. Find subject row
   ↓
3. Click on event cell (e.g., "Visit 1")
   ↓
4. System displays list of CRFs for event:
   - Demographics ✓ Complete
   - Vitals ● In Progress
   - Adverse Events ○ Not Started
   ↓
5. Click "Resume" on Vitals CRF
   ↓
6. System:
   - Loads event_crf record
   - Gets crf_version
   - Loads all item_data
   - Renders form using FormBuilder
   ↓
7. User enters/edits data
   ↓
8. Click "Save" or "Mark Complete"
   ↓
9. System updates item_data records
   ↓
10. System updates event_crf.completion_status
   ↓
11. Visit Grid refreshes showing updated status
```

## Performance Considerations

### Query Optimization for Visit Grid

The visit grid can be slow with:
- Many subjects (hundreds/thousands)
- Many events per subject (10-50+)
- Many CRFs per event (5-20)

**Problem:** Naive implementation = O(subjects × events × CRFs) queries

**Solution:** Batch queries and caching

```java
// BAD: N+1 queries
for (StudySubjectBean subject : subjects) {
    for (StudyEventBean event : getEventsForSubject(subject.getId())) {
        for (EventCRFBean crf : getCRFsForEvent(event.getId())) {
            // Process each CRF individually
        }
    }
}

// GOOD: Batch queries
List<StudySubjectBean> subjects = studySubjectDAO.findAllByStudy(studyId);
Map<Integer, List<StudyEventBean>> eventsBySubject = 
    studyEventDAO.findAllBySubjects(subjectIds);
Map<Integer, List<EventCRFBean>> crfsByEvent = 
    eventCRFDAO.findAllByEvents(eventIds);

// Now process with lookups instead of queries
for (StudySubjectBean subject : subjects) {
    List<StudyEventBean> events = eventsBySubject.get(subject.getId());
    for (StudyEventBean event : events) {
        List<EventCRFBean> crfs = crfsByEvent.get(event.getId());
        // Process
    }
}
```

### Caching Strategy
- Cache event definitions (rarely change)
- Cache CRF definitions (rarely change)
- Cache subject list (update on enrollment)
- Invalidate on status changes

## Integration Points with CRF Builder

### 1. CRF Metadata
Visit grid needs to know:
- CRF name
- CRF version
- CRF status (available/locked/deleted)

### 2. Form Rendering
When user clicks to enter data:
- Loads CRF version metadata
- Uses FormBuilder to generate HTML
- Applies validation rules from item_form_metadata

### 3. Status Updates
As user completes items:
- Updates item_data
- Recalculates event_crf completion status
- Updates visit grid display

### 4. Versioning
If CRF version changes:
- Existing event_crfs stay on old version
- New event_crfs use new version
- Visit grid shows version used

## Reporting & Analytics

### Common Queries

**1. Study Completion Report:**
```sql
-- How many subjects completed each event?
SELECT 
    sed.name AS event_name,
    COUNT(DISTINCT se.study_subject_id) AS subjects_with_event,
    COUNT(DISTINCT CASE WHEN se.subject_event_status_id = 7 
          THEN se.study_subject_id END) AS completed_subjects
FROM study_event_definition sed
LEFT JOIN study_event se ON se.study_event_definition_id = sed.study_event_definition_id
WHERE sed.study_id = ?
GROUP BY sed.study_event_definition_id, sed.name
ORDER BY sed.ordinal;
```

**2. CRF Completion Report:**
```sql
-- Which CRFs are most/least complete?
SELECT 
    c.name AS crf_name,
    sed.name AS event_name,
    COUNT(ec.event_crf_id) AS total_crfs,
    COUNT(CASE WHEN ec.completion_status_id = 2 
          THEN 1 END) AS completed_crfs,
    ROUND(COUNT(CASE WHEN ec.completion_status_id = 2 THEN 1 END) * 100.0 
          / NULLIF(COUNT(ec.event_crf_id), 0), 1) AS completion_percentage
FROM event_definition_crf edc
JOIN crf c ON c.crf_id = edc.crf_id
JOIN study_event_definition sed ON sed.study_event_definition_id = edc.study_event_definition_id
LEFT JOIN study_event se ON se.study_event_definition_id = sed.study_event_definition_id
LEFT JOIN event_crf ec ON ec.study_event_id = se.study_event_id 
    AND ec.crf_version_id IN (SELECT crf_version_id FROM crf_version WHERE crf_id = c.crf_id)
WHERE edc.study_id = ?
GROUP BY c.crf_id, c.name, sed.study_event_definition_id, sed.name
ORDER BY completion_percentage DESC;
```

**3. Subject Progress:**
```sql
-- Show progress for each subject
SELECT 
    ss.label AS subject_id,
    COUNT(DISTINCT se.study_event_id) AS total_events,
    COUNT(DISTINCT CASE WHEN se.subject_event_status_id = 7 
          THEN se.study_event_id END) AS completed_events,
    COUNT(ec.event_crf_id) AS total_crfs,
    COUNT(CASE WHEN ec.completion_status_id = 2 
          THEN 1 END) AS completed_crfs
FROM study_subject ss
LEFT JOIN study_event se ON se.study_subject_id = ss.study_subject_id
LEFT JOIN event_crf ec ON ec.study_event_id = se.study_event_id
WHERE ss.study_id = ?
GROUP BY ss.study_subject_id, ss.label
ORDER BY ss.label;
```

## Modern UI/UX Improvements

### Current Limitations
1. **Static Grid**: Requires page refresh to see updates
2. **Limited Filtering**: Basic search only
3. **No Sorting**: Fixed order
4. **No Drill-down**: Multiple page transitions
5. **No Visualization**: Text-only status
6. **Desktop-Only**: Not mobile-responsive

### Recommended Improvements
1. **Real-time Updates**: WebSocket for live status
2. **Advanced Filtering**: Multi-column filters, saved views
3. **Sortable Columns**: Click to sort by any column
4. **Inline Expansion**: Click to expand cell in place
5. **Visual Indicators**: Color-coded heatmap
6. **Responsive Design**: Mobile-friendly grid
7. **Keyboard Navigation**: Arrow keys to navigate
8. **Bulk Actions**: Select multiple cells for actions
9. **Progress Bars**: Visual completion indicators
10. **Drag-and-Drop**: Reorder events/CRFs

### Example Modern Grid (React)
```jsx
<SubjectMatrix
  subjects={subjects}
  events={events}
  onCellClick={(subject, event) => openDataEntry(subject, event)}
  onFilter={(filters) => updateGrid(filters)}
  realTimeUpdates={true}
  responsive={true}
/>
```

## Standalone Application Considerations

### What's Needed from Visit Grid
If extracting CRF Builder as standalone:

**Option 1: Keep Visit Grid Integration**
- Export APIs for grid data
- Webhook for status updates
- Embed iframe for data entry

**Option 2: Simplify to Task List**
- Remove grid visualization
- Provide simple "Tasks to Complete" list
- Focus on individual form completion

**Option 3: Build New Grid**
- Recreate grid in new framework
- Modern responsive design
- Real-time updates
- Better UX

### API Requirements
```json
// GET /api/subjects/{subjectId}/tasks
{
  "subject_id": "S-001",
  "tasks": [
    {
      "event": "Screening",
      "event_date": "2023-01-15",
      "crfs": [
        {
          "crf_id": 1,
          "crf_name": "Demographics",
          "version": "v1.0",
          "status": "complete",
          "completion_date": "2023-01-15",
          "url": "/data-entry/event-crf/12345"
        },
        {
          "crf_id": 2,
          "crf_name": "Vitals",
          "version": "v1.0",
          "status": "in_progress",
          "completion_date": null,
          "url": "/data-entry/event-crf/12346"
        }
      ]
    }
  ]
}
```

## Summary

The Visit Grid is the **orchestration layer** that connects:
- CRF Builder (form design)
- Study design (events/visits)
- Subject enrollment (participants)
- Data collection (form instances)
- Progress tracking (status visualization)

**Key Integration Points:**
1. `event_definition_crf` - Associates CRFs with events
2. `study_event` - Creates visit instances for subjects
3. `event_crf` - Creates form instances for data entry
4. FormBuilder - Renders forms based on CRF metadata
5. Status calculation - Tracks completion for grid display

**For Standalone CRF Builder:**
- Decide if visit grid is in/out of scope
- If out: Provide APIs for external integration
- If in: Rebuild with modern tech stack
- Consider task-based UX as simpler alternative
