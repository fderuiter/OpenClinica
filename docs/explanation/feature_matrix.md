# Feature Matrix

This document outlines the core technical features and compliance mechanisms supported by the system.

## 1. Context & Objectives
The feature matrix tracks application-level capabilities and architectural choices.

## 2. Compliance and Data Integrity Features

### Data Deletion and Hard-Delete Blocks
Physical data deletion is prevented entirely at the application layer.
- **Mechanism:** Implemented via application-layer SQLAlchemy `before-flush` mechanisms.

### Audit Trails and Version Tracking
- **Execution:** Version tracking runs synchronously inside the application thread.
- **Change Tracking:** Change reasons are nullable.

*Note: The system leverages purely application-level event listeners to enforce compliance. Native database schemas remain strictly structural.*
