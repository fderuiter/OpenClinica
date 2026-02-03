# OpenClinica Architectural Analysis Report

## 1. Executive Summary
OpenClinica is a mature, enterprise-grade clinical data management system. The codebase represents a **Legacy Hybrid Architecture**, currently in a transitional state between older Java EE patterns (Servlets, JDBC, manual wiring) and modern Spring-based practices (Spring MVC, Dependency Injection, Hibernate). While the system is functional and feature-rich, this hybrid state introduces significant technical debt, characterized by architectural drift, code duplication, and tight coupling.

## 2. High-Level Architecture
The application follows a traditional **Multi-Module Layered Architecture** managed by Maven.

*   **Style**: Layered / N-Tier.
*   **Deployment**: Java EE Web Application (WAR) deployed on a Servlet Container (e.g., Tomcat).
*   **Database**: Relational Database (Support for PostgreSQL and Oracle).

### Module Structure
*   **`core`**: The backend foundation. Contains domain entities (`org.akaza.openclinica.bean`), business logic (`service`), and the data access layer (`dao`).
*   **`web`**: The presentation layer. Contains the UI (JSP), legacy Servlets (`org.akaza.openclinica.control`), Spring MVC Controllers, and RESTful endpoints (Jersey).
*   **`ws`**: SOAP Web Services implementation using Spring-WS.

## 3. Technology Stack
*   **Language**: Java 7.
*   **Frameworks**:
    *   **Spring Framework (3.2.18.RELEASE)**: Core Container, MVC, Security, JDBC, ORM.
    *   **Hibernate (3.5.1-Final)**: ORM for specific domain entities.
    *   **Liquibase**: Database schema migration.
    *   **SiteMesh**: Web page layout and decoration.
    *   **Jersey (JAX-RS)**: RESTful web services.
*   **Build Tool**: Maven.
*   **Logging**: SLF4J / Logback.

## 4. Deep-Dive Layer Analysis

### A. Data Access Layer (Hybrid & Drifted)
The data access layer exhibits significant **Architectural Drift**, with two parallel hierarchies:
1.  **Custom DAO Framework (Legacy/Dominant)**:
    *   Relies on `SQLFactory` (Singleton) to load externalized SQL queries from XML files in `src/main/resources/properties`.
    *   DAOs extend `EntityDAO` or `AuditableEntityDAO`.
    *   Uses a Template Method pattern for executing SQL and mapping `ResultSet` to `HashMap`/Beans.
    *   *Example*: `StudyDAO` (in `dao.managestudy`).
2.  **Hibernate (Modern/Minority)**:
    *   Uses standard Hibernate sessions and object-relational mapping.
    *   Located in `org.akaza.openclinica.dao.hibernate`.
    *   *Example*: `RuleSetDao`.

### B. Service Layer (Coupled)
*   Business logic is distributed across `org.akaza.openclinica.service` and various Controller/Servlet classes.
*   **Critical Anti-Pattern**: The service layer frequently relies on **Manual Dependency Instantiation** rather than Inversion of Control (IoC).
    *   *Code Evidence*: `subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);`
    *   *Impact*: This creates tight coupling between Services and concrete DAO implementations, making unit testing difficult without mocking the entire database connection.

### C. Web / Presentation Layer (Hybrid)
*   **Legacy Servlets**: A substantial portion of the application logic resides in `HttpServlet` implementations mapped in `web.xml`.
    *   *Risk*: These classes are often "God Objects" handling routing, validation, business logic, and view rendering.
    *   *Example*: `DataEntryServlet.java`.
*   **Spring MVC**: Newer features use `@Controller` and `DispatcherServlet` mapped to `/pages/*`.
*   **REST**: Implemented via Jersey (`SpringServlet` mapped to `/rest/*`).

## 5. Design Patterns Identified

| Pattern | Type | Implementation Status | Notes |
| :--- | :--- | :--- | :--- |
| **DAO** | Structural | **High Drift** | Split between Custom `EntityDAO` and Hibernate `AbstractDomainDao`. |
| **Singleton** | Creational | **Standard** | `SQLFactory` is a classic Singleton used to manage SQL resources. |
| **Template Method**| Behavioral | **Standard** | `EntityDAO` provides the skeleton for JDBC operations, subclasses provide specifics. |
| **Observer** | Behavioral | **Custom** | Implemented in `org.akaza.openclinica.patterns.ocobserver`. Tightly coupled to Domain Beans (e.g., `StudyEventBean`). |
| **Decorator** | Structural | **Standard** | **SiteMesh** is used to decorate web pages with consistent headers/footers. |
| **Front Controller**| Behavioral | **Standard** | Spring `DispatcherServlet` handles MVC requests. |
| **Factory** | Creational | **Standard** | `ListStudySubjectTableFactory` generates UI table components. |

## 6. Static Analysis Findings

### A. "God Objects" (SRP Violations)
Several classes have grown excessively large, indicating they handle too many responsibilities:
1.  **`DataEntryServlet.java` (~300KB)**: Likely handles the entire data entry workflow, validation, and navigation.
2.  **`OdmExtractDAO.java` (~200KB)**: A DAO that likely contains complex reporting logic that belongs in the Service layer.
3.  **`SpreadSheetTableRepeating.java` (~184KB)**: UI generation logic that is overly complex.

### B. Coupling & Cohesion
*   **Tight Coupling**: The widespread use of `new DAO(dataSource)` inside Services and Controllers prevents the effective use of Spring's Dependency Injection features.
*   **Logic Leaks**: Business logic leaks into the View layer (JSP scriptlets) and Data Access layer (complex SQL queries doing business calculation).

## 7. Recommendations

### Short-Term (Refactoring)
1.  **Enforce Dependency Injection**: Systematically remove `new Dao(ds)` calls. Annotate DAOs with `@Repository` and Services with `@Service`. Use `@Autowired` to inject dependencies. This is the single highest-impact change for testability.
2.  **Break Down God Objects**: Extract logic from `DataEntryServlet` into smaller, focused Spring MVC Controllers and helper Services.

### Long-Term (Modernization)
1.  **Consolidate Data Access**: Halt development on the Custom XML DAO framework. Migrate critical paths to Hibernate/JPA to standardize the persistence layer.
2.  **Phase Out Servlets**: Migrate remaining `HttpServlet` implementations to Spring MVC Controllers to unify the web request handling pipeline.
3.  **Standardize Event Handling**: Replace the custom Observer pattern with Spring's `ApplicationEventPublisher` for looser coupling.
