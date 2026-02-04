# CRF Design Studio - Documentation

## 📚 Documentation Hub

Welcome to the CRF Design Studio documentation! This is a standalone visual design tool for creating Case Report Forms and configuring study visit schedules.

**Important:** This is a **design/configuration tool only** - NOT a data collection system.

---

## 🚀 Quick Navigation

**👉 [VIEW COMPLETE DOCUMENTATION INDEX](INDEX.md)** 👈

### Quick Links by Role

**Developers** → [Development Setup](guides/DEVELOPMENT-SETUP.md) | [Step-by-Step Guide](guides/STEP-BY-STEP-GUIDE.md)
**Product Managers** → [Scope Definition](00-SCOPE-DEFINITION.md) | [Roadmap](07-IMPLEMENTATION-ROADMAP-DESIGN-TOOL.md)
**Architects** → [Architecture Patterns](development/ARCHITECTURE-PATTERNS.md) | [API Design](development/API-DESIGN.md)
**QA Engineers** → [Testing Strategy](guides/TESTING-STRATEGY.md)

---

## Executive Summary

## Document Structure

**New Focus: Design Tool Only**
0. **00-SCOPE-DEFINITION.md** - ⭐ Clear scope: What we ARE and ARE NOT building
1. **01-OVERVIEW.md** - High-level introduction to CRF Builder (OpenClinica analysis)
2. **02-ARCHITECTURE.md** - Technical architecture (OpenClinica analysis)
3. **03-DATA-MODEL.md** - Database schema (OpenClinica analysis)
4. **04-VISIT-GRID-INTEGRATION.md** - Visit grid integration (OpenClinica analysis)
5. **05-MIGRATION-PLAN.md** - Original full-system plan (ARCHIVED - too broad)
6. **06-FRAMEWORK-ANALYSIS-DESIGN-TOOL.md** - ⭐ Framework analysis for DESIGN tool
7. **07-IMPLEMENTATION-ROADMAP-DESIGN-TOOL.md** - ⭐ 6-month plan for design studio
8. **README.md** - This file (executive summary)

## Quick Reference

### What Does the CRF Design Studio Do?

The CRF Design Studio enables study designers to:
1. **Design forms visually** using drag-and-drop interface
2. **Configure fields** with validation rules
3. **Organize sections** and pages
4. **Support repeating groups** for complex data
5. **Import/export Excel templates**
6. **Export to ODM XML** (CDISC format)
7. **Configure visit schedules** and associate CRFs with visits
8. **Preview forms** before export

**What it does NOT do:**
- ❌ Collect actual patient data
- ❌ Manage study subjects
- ❌ Clinical workflows
- ❌ Data monitoring
- ❌ Query management

### Key Technologies (Current)
- **Backend**: Java, Spring Framework, JDBC, Servlets
- **Frontend**: JSP, JavaScript, jQuery
- **Database**: PostgreSQL / Oracle
- **Excel**: Apache POI
- **Standards**: CDISC ODM 1.3, OpenRosa/XForm

### Recommended Technologies (Design Studio)

| Component | Technology | Why |
|-----------|-----------|-----|
| **Frontend** | React 18 + TypeScript | Best for visual design tools, React Flow |
| **Visual Designer** | React Flow | Perfect for node-based form designer |
| **Drag-Drop** | dnd-kit | Modern, performant drag-and-drop |
| **UI Library** | Material-UI (MUI) | Professional, accessible components |
| **Backend** | Node.js 20 + NestJS | TypeScript full-stack, lighter weight |
| **Database** | PostgreSQL 15+ with JSONB | Flexible metadata storage |
| **API** | REST | Simple, standard |
| **State** | Zustand + TanStack Query | Lightweight state + server state |
| **Forms** | React Hook Form + Zod | Best-in-class form management |
| **Testing** | Vitest + Playwright | Fast unit tests + E2E |
| **DevOps** | Docker + GitHub Actions | Modern deployment |

## Core Concepts

### CRF Hierarchy
```
Study
  └── Study Event (Visit)
       └── CRF (Form Definition)
            └── CRF Version
                 ├── Sections (Pages)
                 │    └── Items (Fields)
                 └── Item Groups (Repeating Data)
```

### Data Flow
```
Design → Upload → Parse → Store → Associate → Collect → Export
```

### Key Database Tables
- **crf** - Form definitions
- **crf_version** - Versioned forms
- **section** - Form pages
- **item** - Data fields
- **item_form_metadata** - Field configuration
- **event_definition_crf** - CRF-Visit association
- **event_crf** - Form instances
- **item_data** - Collected values

## Migration Timeline

**Focus: Design Studio Only (NOT full data collection system)**

**Total Duration**: 24 weeks (6 months)
**Team Size**: 5-6 people

### Phase Breakdown

| Phase | Duration | Focus |
|-------|----------|-------|
| **1. Foundation** | 4 weeks | React + NestJS + PostgreSQL setup |
| **2. Visual Designer** | 6 weeks | React Flow integration, drag-drop |
| **3. Advanced Designer** | 4 weeks | Validation, logic, repeating groups |
| **4. Preview & Import** | 4 weeks | Form preview, Excel import/export |
| **5. Visit Grid & ODM** | 4 weeks | Visit configuration, ODM export |
| **6. Polish & Deploy** | 2 weeks | Testing, documentation, deployment |

## Key Features to Build

### Essential (Design Studio)
- ✅ Visual form designer with drag-and-drop
- ✅ Field configuration (properties, validation)
- ✅ Section organization
- ✅ Excel template import/parsing
- ✅ Excel template export
- ✅ ODM XML export (CDISC compliant)
- ✅ Form preview mode
- ✅ Visit grid configuration
- ✅ CRF-visit associations
- ✅ Version control
- ✅ Template library
- ✅ User authentication (simple)

### Nice-to-Have (Future)
- ⭕ Real-time collaboration (multiple designers)
- ⭕ AI-powered validation suggestions
- ⭕ Natural language rule builder
- ⭕ REDCap import
- ⭕ More export formats (JSON Schema, OpenAPI)
- ⭕ Desktop app version (Electron)

### Explicitly NOT Building
- ❌ Data entry forms (actual collection)
- ❌ Subject enrollment
- ❌ Study participant management
- ❌ Clinical workflows
- ❌ Data monitoring dashboards
- ❌ Query/discrepancy management
- ❌ Progress tracking for subjects
- ❌ Audit trails for patient data
- ❌ Electronic signatures for data
- ❌ Real-time data validation

## UI/UX Improvements

### Current Issues
- Excel dependency (requires desktop tool)
- Desktop-only design (not mobile-friendly)
- Static pages (requires refresh)
- Multiple page transitions
- Limited visual feedback
- No real-time collaboration

### Modern Solutions
1. **Web-Based Designer**
   - Drag-and-drop form builder
   - Live preview
   - No Excel required

2. **Responsive Design**
   - Mobile-first approach
   - Touch-friendly controls
   - Adaptive layouts

3. **Real-Time Updates**
   - WebSocket for live changes
   - Instant status updates
   - Live collaboration indicators

4. **Visual Feedback**
   - Progress indicators
   - Color-coded status
   - Animated transitions

5. **Single Page Application**
   - No page reloads
   - Instant navigation
   - Better performance

6. **Accessibility**
   - WCAG 2.1 Level AA compliant
   - Keyboard navigation
   - Screen reader support

## Development Approach

### Agile/Scrum
- **2-week sprints**
- **Daily standups**
- **Sprint planning**
- **Sprint review/demo**
- **Sprint retrospective**

### Deliverables
- **Every sprint**: Working software increment
- **Every 4 weeks**: Major milestone demo
- **Monthly**: Progress report to stakeholders

### Quality Gates
- [ ] Code review required
- [ ] Unit tests passing (>80% coverage)
- [ ] Integration tests passing
- [ ] E2E tests passing for critical flows
- [ ] Security scan passing
- [ ] Performance benchmarks met
- [ ] Accessibility checks passing

## Data Migration Strategy

### Preparation
1. **Export from OpenClinica**
   - Extract CRF definitions
   - Extract versions
   - Extract all metadata
   - Export to JSON format

2. **Validate Export**
   - Check completeness
   - Verify relationships
   - Test with subset

3. **Transform Data**
   - Map to new schema
   - Convert data types
   - Handle edge cases

4. **Import to New System**
   - Load CRFs
   - Load versions
   - Load sections and items
   - Verify integrity

5. **Reconciliation**
   - Compare counts
   - Spot-check data
   - User validation

### Rollback Plan
- Keep OpenClinica running in parallel
- Can revert to old system if issues
- Gradual migration (study-by-study)

## Cost Estimation (Design Studio Only)

### Development Team (6 months)
- 2 Full-Stack Developers @ $130k/yr = $130k
- 1 UI/UX Designer @ $110k/yr = $55k
- 0.5 DevOps Engineer @ $140k/yr = $35k
- 1 Product Manager @ $150k/yr = $75k
- 0.5 QA Engineer @ $100k/yr = $25k

**Total Personnel**: ~$320k

### Infrastructure (6 months)
- Cloud hosting (dev/staging): $3k
- Development tools & licenses: $5k
- Testing tools: $2k

**Total Infrastructure**: ~$10k

### Other Costs
- Contingency (10%): $33k

**Grand Total**: ~$363k
**Cost per month**: ~$61k

### Comparison to Full System
| System Type | Duration | Team | Cost |
|-------------|----------|------|------|
| **Design Studio** (This) | 6 months | 5-6 | ~$363k |
| Full EDC System (Original) | 9 months | 10 | ~$1.08M |

**Savings**: ~$720k (67% less) by focusing on design tool only

## Return on Investment (Design Studio)

### Benefits (annual)
- Faster form design: $100k (time savings vs Excel)
- Reduced errors: $50k (better validation)
- Standardization: $30k (template library)
- Easier collaboration: $40k (web-based)

**Total Annual Benefits**: ~$220k

**ROI**: Break even in ~20 months, continuous benefits thereafter

### Compared to Manual Process
Without design studio, typical organization spends:
- Excel template creation and debugging: $80k/yr
- Manual validation checks: $40k/yr
- Version control confusion: $30k/yr
- Training on Excel templates: $20k/yr
- ODM export preparation: $50k/yr

**Total Manual Cost**: ~$220k/yr

Design Studio eliminates most of these costs.
- Faster form design: $100k (time savings)
- Better data quality: $150k (fewer errors)
- Reduced training time: $30k
- Lower maintenance: $70k (modern tech)
- Increased user satisfaction: Priceless

**Total Annual Benefits**: ~$400k

**ROI**: Break even in ~3 years, but continuous benefits

## Success Metrics

### Technical KPIs
- System uptime: > 99.9%
- Page load time: < 2 seconds
- API response time: < 500ms (p95)
- Test coverage: > 80%
- Security vulnerabilities: 0 critical

### User KPIs
- User satisfaction: > 4.0/5.0
- Task completion rate: > 90%
- Form design time: 50% reduction
- Training time: 30% reduction
- Error rate: < 2%

### Business KPIs
- On-time delivery: Yes
- On-budget: Within 10%
- Feature completeness: 100% MVP
- User adoption: > 80% within 6 months
- Support tickets: < 10/month after 3 months

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Data migration issues | Medium | High | Extensive testing, rollback plan |
| Performance problems | Low | Medium | Early performance testing |
| User resistance | Medium | High | Involve users early, training |
| Scope creep | High | Medium | Strict prioritization, MVP focus |
| Team turnover | Low | High | Documentation, knowledge sharing |
| Security vulnerabilities | Low | High | Security audits, best practices |
| Integration challenges | Medium | Medium | Early API definition, testing |
| Budget overrun | Medium | Medium | Regular tracking, contingency |

## Next Steps

### Immediate (Week 1)
1. Review these documents with stakeholders
2. Get approval for recommended approach
3. Secure budget and resources
4. Assemble core team
5. Set up project infrastructure

### Short Term (Weeks 2-4)
1. Complete detailed requirements
2. Create project plan in detail
3. Set up development environment
4. Begin Phase 1 (Foundation)
5. Start UI/UX design work

### Medium Term (Months 2-6)
1. Develop core features (Phases 2-5)
2. Regular demos to stakeholders
3. Iterative feedback and improvements
4. Begin testing and QA
5. Plan data migration

### Long Term (Months 7-9)
1. Complete all features
2. Comprehensive testing
3. User acceptance testing
4. Data migration
5. Production deployment
6. Training and support

## Questions & Answers

**Q: Why not just upgrade OpenClinica?**
A: The technology stack is outdated (JSP, JDBC). A rewrite with modern technologies provides better UX, performance, and maintainability.

**Q: Can we do this faster?**
A: Yes, by reducing scope (e.g., skip visual designer, use Excel only). But 6 months minimum for a quality system.

**Q: Can we do this cheaper?**
A: Yes, with a smaller team or offshore resources. But may compromise quality or timeline.

**Q: What if requirements change?**
A: Agile approach allows flexibility. Regular demos ensure alignment. Some changes expected and budgeted.

**Q: How do we maintain the old and new systems?**
A: Run in parallel during transition. Gradually migrate studies. Old system read-only after migration.

**Q: What about existing data?**
A: Migration scripts preserve all data. Can keep old system for historical data reference.

**Q: Is this compatible with regulatory requirements?**
A: Yes, maintains 21 CFR Part 11 compliance. Audit trail, e-signatures, validation all supported.

## Contacts

- **Technical Questions**: Architecture team
- **Feature Questions**: Product manager
- **Timeline Questions**: Project manager
- **Budget Questions**: Finance team

## Appendices

### A. File Inventory
See separate document: `06-CODE-INVENTORY.md`

### B. Database Schema
See section in: `03-DATA-MODEL.md`

### C. API Specification
See separate document: `OpenAPI-Specification.yaml` (to be created)

### D. UI Mockups
See separate directory: `/docs/crf-builder/mockups/` (to be created)

### E. Test Plan
See separate document: `Test-Plan.md` (to be created)

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-03 | Analysis Team | Initial comprehensive analysis |

## License

This documentation is for internal use only. Proprietary and confidential.

---

**For questions or clarifications, please contact the project team.**
