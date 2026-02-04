# CRF Design Studio - Documentation

## 📚 Complete Documentation Suite

Welcome to the **CRF Design Studio** documentation - a standalone visual tool for designing Case Report Forms and configuring study visit schedules.

**🎯 Important:** This is a **design/configuration tool ONLY** - NOT a data collection system.

---

## 🚀 Quick Start

### By Role

| Role | Start Here |
|------|------------|
| 👨‍💻 **Developer** | [Development Setup](guides/DEVELOPMENT-SETUP.md) → [Step-by-Step Guide](guides/STEP-BY-STEP-GUIDE.md) |
| 📋 **Product Manager** | [Scope Definition](00-SCOPE-DEFINITION.md) → [Implementation Roadmap](07-IMPLEMENTATION-ROADMAP-DESIGN-TOOL.md) |
| 🏗️ **Architect** | [Architecture](02-DESIGN-STUDIO-ARCHITECTURE.md) → [Design Patterns](development/ARCHITECTURE-PATTERNS.md) |
| 🧪 **QA Engineer** | [Testing Strategy](guides/TESTING-STRATEGY.md) |
| 🎨 **UX Designer** | [Scope Definition](00-SCOPE-DEFINITION.md) → [Visit Grid Configuration](04-VISIT-GRID-CONFIGURATION.md) |

### By Task

| Task | Documentation |
|------|---------------|
| **Understand what we're building** | [Scope Definition](00-SCOPE-DEFINITION.md) |
| **Set up development environment** | [Development Setup](guides/DEVELOPMENT-SETUP.md) |
| **Learn the architecture** | [Architecture](02-DESIGN-STUDIO-ARCHITECTURE.md) |
| **Understand the database** | [Data Model](03-DESIGN-STUDIO-DATA-MODEL.md) |
| **Choose technologies** | [Framework Analysis](06-FRAMEWORK-ANALYSIS-DESIGN-TOOL.md) |
| **Start building** | [Step-by-Step Guide](guides/STEP-BY-STEP-GUIDE.md) |
| **Write tests** | [Testing Strategy](guides/TESTING-STRATEGY.md) |
| **Follow best practices** | [Architecture Patterns](development/ARCHITECTURE-PATTERNS.md) |

---

## �� Complete Documentation Index

**➡️ [VIEW FULL DOCUMENTATION INDEX](INDEX.md) ⬅️**

---

## 🎯 What is the CRF Design Studio?

### Purpose

The CRF Design Studio enables clinical study designers to:

✅ **Design forms visually** - Drag-and-drop interface, no coding required  
✅ **Configure fields** - Set validation rules, data types, display options  
✅ **Organize sections** - Create pages and logical groupings  
✅ **Support repeating groups** - Handle complex repeating data structures  
✅ **Import Excel templates** - Parse existing Excel-based CRF designs  
✅ **Export to ODM XML** - Generate CDISC-compliant metadata  
✅ **Export to Excel** - Create Excel templates from designs  
✅ **Configure visit schedules** - Define study timeline and events  
✅ **Associate CRFs with visits** - Specify which forms for which visits  
✅ **Preview forms** - See how forms will appear to end users  
✅ **Version control** - Track changes with Git integration  
✅ **Template library** - Reuse common form designs  

### What It Does NOT Do

❌ Collect actual patient data  
❌ Manage study subjects/participants  
❌ Handle clinical workflows  
❌ Perform data monitoring  
❌ Manage queries or discrepancies  

### Analogy

- **Google Forms Builder** (create forms) - ✅ We're building this
- **Google Forms** (collect responses) - ❌ We're NOT building this

---

## 🏗️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18 + TypeScript + Material-UI + React Flow |
| **Backend** | NestJS + TypeScript + Prisma ORM |
| **Database** | PostgreSQL 15+ with JSONB |
| **Version Control** | Git (for CRF versions) |
| **Testing** | Vitest + Jest + Playwright |
| **Deployment** | Docker + Kubernetes |

---

## 🗺️ 6-Month Plan

- **Month 1:** Foundation (React + NestJS + PostgreSQL)
- **Month 2:** Visual Designer (React Flow, drag-and-drop)
- **Month 3:** Advanced Features (validation, logic, groups)
- **Month 4:** Preview & Import (form preview, Excel)
- **Month 5:** Visit Grid & ODM (configuration, ODM export)
- **Month 6:** Polish & Deploy (testing, docs, deployment)

**Team:** 5-6 people | **Budget:** ~$363k

---

## 📚 All Documentation

### Core Documents
- [README.md](README.md) - This file
- [INDEX.md](INDEX.md) - Complete navigation
- [00-SCOPE-DEFINITION.md](00-SCOPE-DEFINITION.md) - What we're building

### Architecture & Design
- [02-DESIGN-STUDIO-ARCHITECTURE.md](02-DESIGN-STUDIO-ARCHITECTURE.md) - System architecture
- [03-DESIGN-STUDIO-DATA-MODEL.md](03-DESIGN-STUDIO-DATA-MODEL.md) - Database schema
- [04-VISIT-GRID-CONFIGURATION.md](04-VISIT-GRID-CONFIGURATION.md) - Visit grid details

### Planning
- [06-FRAMEWORK-ANALYSIS-DESIGN-TOOL.md](06-FRAMEWORK-ANALYSIS-DESIGN-TOOL.md) - Technology choices
- [07-IMPLEMENTATION-ROADMAP-DESIGN-TOOL.md](07-IMPLEMENTATION-ROADMAP-DESIGN-TOOL.md) - 6-month roadmap

### Development Standards
- [development/ARCHITECTURE-PATTERNS.md](development/ARCHITECTURE-PATTERNS.md) - Design patterns
- [development/GIT-VERSIONING-STRATEGY.md](development/GIT-VERSIONING-STRATEGY.md) - Git versioning
- [development/API-DESIGN.md](development/API-DESIGN.md) - API conventions

### Practical Guides
- [guides/DEVELOPMENT-SETUP.md](guides/DEVELOPMENT-SETUP.md) - Setup in 30 minutes
- [guides/STEP-BY-STEP-GUIDE.md](guides/STEP-BY-STEP-GUIDE.md) - Implementation walkthrough
- [guides/TESTING-STRATEGY.md](guides/TESTING-STRATEGY.md) - Testing guide

---

## ❓ FAQs

**Q: Why not upgrade OpenClinica?**  
A: Focused design tool, not full EDC. Modern stack = better UX.

**Q: What about existing Excel templates?**  
A: Full import support. Continue using Excel or switch to visual designer.

**Q: Is this CDISC compliant?**  
A: Yes, exports to ODM XML (CDISC standard).

---

## 🚀 Get Started

1. Read [Scope Definition](00-SCOPE-DEFINITION.md)
2. Follow [Development Setup](guides/DEVELOPMENT-SETUP.md)
3. Build with [Step-by-Step Guide](guides/STEP-BY-STEP-GUIDE.md)

**Ready to build? Let's go! 🚀**
