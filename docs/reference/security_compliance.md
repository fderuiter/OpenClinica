# Security Compliance and Audit Specification

## 1. Compliance Architecture Overview
This specification details how data integrity and security rules are enforced across the platform. All audit constraints and compliance checks are managed in Python application code.

## 2. Data Mutation Controls
- **Hard-Delete Blocks:** The primary hard-delete block is implemented using an application-layer SQLAlchemy `before-flush` handler. This reliably prevents physical row deletion without relying on database-level implementations.

## 3. Audit Logging
- **Version Tracking:** All entity state changes and audit logging operations execute synchronously inside the application thread to guarantee consistency.
- **Change Reasons:** When modifying versioned records, change reasons are inherently nullable and handled at the application layer.

## 4. SDLC and Platform Constraints
- The platform relies exclusively on standard SQL database functionality, enforcing all complex data mutability rules at the application event level.
