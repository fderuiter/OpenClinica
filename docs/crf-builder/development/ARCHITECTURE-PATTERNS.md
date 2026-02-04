# Architecture & Design Patterns

## Overview

This document outlines the architectural principles and design patterns for the CRF Design Studio. Following these patterns ensures consistency, maintainability, and scalability.

---

## Table of Contents
1. [Architectural Principles](#architectural-principles)
2. [Design Patterns](#design-patterns)
3. [Frontend Architecture](#frontend-architecture)
4. [Backend Architecture](#backend-architecture)
5. [Code Organization](#code-organization)
6. [Naming Conventions](#naming-conventions)
7. [Best Practices](#best-practices)

---

## Architectural Principles

### 1. Separation of Concerns
**Principle**: Each component should have a single, well-defined responsibility.

```
Frontend:  Presentation → Business Logic → Data Management
Backend:   Controller → Service → Repository → Database
```

### 2. Domain-Driven Design (DDD)
**Principle**: Model the software based on the domain (CRF design).

**Core Domains**:
- **CRF Design** - Form structure and metadata
- **Validation** - Rules and constraints
- **Export** - Format conversion and output
- **Visit Configuration** - Event scheduling

### 3. Clean Architecture
**Layers** (from outer to inner):
1. **Presentation Layer** - UI components, API controllers
2. **Application Layer** - Use cases, application services
3. **Domain Layer** - Business logic, domain models
4. **Infrastructure Layer** - Database, external services

### 4. API-First Design
**Principle**: Design APIs before implementation.

- Define OpenAPI/Swagger specs first
- Use contract testing
- Version APIs from the start

### 5. Progressive Enhancement
**Principle**: Build core functionality first, then enhance.

- Works without JavaScript (where possible)
- Add rich interactions progressively
- Graceful degradation

---

## Design Patterns

### Frontend Patterns

#### 1. Container/Presentational Components
**Purpose**: Separate data logic from presentation.

```typescript
// Container Component (Smart)
const CRFDesignerContainer: React.FC = () => {
  const { crfs, loading, error } = useCRFs();
  const { saveCRF } = useCRFMutations();
  
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorDisplay error={error} />;
  
  return <CRFDesigner crfs={crfs} onSave={saveCRF} />;
};

// Presentational Component (Dumb)
interface CRFDesignerProps {
  crfs: CRF[];
  onSave: (crf: CRF) => void;
}

const CRFDesigner: React.FC<CRFDesignerProps> = ({ crfs, onSave }) => {
  return (
    <div>
      {/* Pure UI, no data fetching */}
    </div>
  );
};
```

#### 2. Custom Hooks
**Purpose**: Reusable stateful logic.

```typescript
// hooks/useCRFs.ts
export const useCRFs = () => {
  return useQuery({
    queryKey: ['crfs'],
    queryFn: () => api.getCRFs(),
  });
};

// hooks/useCRFMutations.ts
export const useCRFMutations = () => {
  const queryClient = useQueryClient();
  
  const saveCRF = useMutation({
    mutationFn: (crf: CRF) => api.saveCRF(crf),
    onSuccess: () => {
      queryClient.invalidateQueries(['crfs']);
    },
  });
  
  return { saveCRF };
};
```

#### 3. Compound Components
**Purpose**: Related components that work together.

```typescript
// Compound component pattern for field configuration
const FieldConfig = ({ children }) => {
  const [field, setField] = useState({});
  
  return (
    <FieldConfigContext.Provider value={{ field, setField }}>
      {children}
    </FieldConfigContext.Provider>
  );
};

FieldConfig.Name = ({ }) => {
  const { field, setField } = useFieldConfig();
  return <input value={field.name} onChange={...} />;
};

FieldConfig.Type = ({ }) => {
  const { field, setField } = useFieldConfig();
  return <select value={field.type} onChange={...} />;
};

// Usage
<FieldConfig>
  <FieldConfig.Name />
  <FieldConfig.Type />
  <FieldConfig.Validation />
</FieldConfig>
```

#### 4. Render Props / Children as Function
**Purpose**: Share code between components using a prop whose value is a function.

```typescript
const DataProvider = ({ children, queryKey }) => {
  const { data, loading, error } = useQuery(queryKey);
  return children({ data, loading, error });
};

// Usage
<DataProvider queryKey="crfs">
  {({ data, loading, error }) => (
    loading ? <Spinner /> : <CRFList crfs={data} />
  )}
</DataProvider>
```

#### 5. Higher-Order Components (HOC)
**Purpose**: Enhance components with additional functionality.

```typescript
// withAuth.tsx
export const withAuth = <P extends object>(
  Component: React.ComponentType<P>
) => {
  return (props: P) => {
    const { user, loading } = useAuth();
    
    if (loading) return <Spinner />;
    if (!user) return <Redirect to="/login" />;
    
    return <Component {...props} />;
  };
};

// Usage
const ProtectedDesigner = withAuth(CRFDesigner);
```

### Backend Patterns

#### 1. Repository Pattern
**Purpose**: Abstract data access logic.

```typescript
// repositories/CRFRepository.ts
export interface ICRFRepository {
  findAll(): Promise<CRF[]>;
  findById(id: string): Promise<CRF | null>;
  create(crf: CreateCRFDto): Promise<CRF>;
  update(id: string, crf: UpdateCRFDto): Promise<CRF>;
  delete(id: string): Promise<void>;
}

export class CRFRepository implements ICRFRepository {
  constructor(private prisma: PrismaClient) {}
  
  async findAll(): Promise<CRF[]> {
    return this.prisma.crf.findMany({
      orderBy: { createdAt: 'desc' },
    });
  }
  
  async findById(id: string): Promise<CRF | null> {
    return this.prisma.crf.findUnique({
      where: { id },
      include: { versions: true },
    });
  }
  
  // ... other methods
}
```

#### 2. Service Layer Pattern
**Purpose**: Encapsulate business logic.

```typescript
// services/CRFService.ts
@Injectable()
export class CRFService {
  constructor(
    private crfRepository: ICRFRepository,
    private validationService: ValidationService,
    private exportService: ExportService,
  ) {}
  
  async createCRF(dto: CreateCRFDto): Promise<CRF> {
    // Validate
    await this.validationService.validateCRF(dto);
    
    // Business logic
    const crf = await this.crfRepository.create(dto);
    
    // Side effects
    await this.exportService.generatePreview(crf);
    
    return crf;
  }
  
  async validateAndSave(dto: UpdateCRFDto): Promise<CRF> {
    // Validation logic
    const errors = await this.validationService.validate(dto);
    if (errors.length > 0) {
      throw new ValidationException(errors);
    }
    
    // Save
    return this.crfRepository.update(dto.id, dto);
  }
}
```

#### 3. Factory Pattern
**Purpose**: Create objects without specifying exact class.

```typescript
// factories/ExporterFactory.ts
export interface IExporter {
  export(crf: CRF): Promise<Buffer>;
}

export class ExporterFactory {
  static create(format: ExportFormat): IExporter {
    switch (format) {
      case 'odm':
        return new ODMExporter();
      case 'excel':
        return new ExcelExporter();
      case 'json':
        return new JSONExporter();
      default:
        throw new Error(`Unsupported format: ${format}`);
    }
  }
}

// Usage
const exporter = ExporterFactory.create('odm');
const output = await exporter.export(crf);
```

#### 4. Strategy Pattern
**Purpose**: Define a family of algorithms, make them interchangeable.

```typescript
// strategies/ValidationStrategy.ts
export interface IValidationStrategy {
  validate(field: Field): ValidationResult;
}

export class RequiredValidationStrategy implements IValidationStrategy {
  validate(field: Field): ValidationResult {
    if (!field.value || field.value.trim() === '') {
      return { valid: false, message: 'Field is required' };
    }
    return { valid: true };
  }
}

export class RegexValidationStrategy implements IValidationStrategy {
  constructor(private pattern: string) {}
  
  validate(field: Field): ValidationResult {
    const regex = new RegExp(this.pattern);
    if (!regex.test(field.value)) {
      return { valid: false, message: 'Invalid format' };
    }
    return { valid: true };
  }
}

// Validator uses strategies
export class FieldValidator {
  private strategies: IValidationStrategy[] = [];
  
  addStrategy(strategy: IValidationStrategy) {
    this.strategies.push(strategy);
  }
  
  validate(field: Field): ValidationResult[] {
    return this.strategies.map(s => s.validate(field));
  }
}
```

#### 5. Builder Pattern
**Purpose**: Construct complex objects step by step.

```typescript
// builders/CRFBuilder.ts
export class CRFBuilder {
  private crf: Partial<CRF> = {};
  
  setName(name: string): this {
    this.crf.name = name;
    return this;
  }
  
  setDescription(description: string): this {
    this.crf.description = description;
    return this;
  }
  
  addSection(section: Section): this {
    if (!this.crf.sections) {
      this.crf.sections = [];
    }
    this.crf.sections.push(section);
    return this;
  }
  
  build(): CRF {
    if (!this.crf.name) {
      throw new Error('CRF must have a name');
    }
    return this.crf as CRF;
  }
}

// Usage
const crf = new CRFBuilder()
  .setName('Demographics')
  .setDescription('Patient demographics form')
  .addSection(demographicsSection)
  .addSection(contactSection)
  .build();
```

#### 6. Decorator Pattern
**Purpose**: Add behavior to objects dynamically.

```typescript
// decorators/logging.decorator.ts
export function LogExecutionTime() {
  return function (
    target: any,
    propertyKey: string,
    descriptor: PropertyDescriptor
  ) {
    const originalMethod = descriptor.value;
    
    descriptor.value = async function (...args: any[]) {
      const start = Date.now();
      const result = await originalMethod.apply(this, args);
      const end = Date.now();
      
      console.log(`${propertyKey} took ${end - start}ms`);
      return result;
    };
    
    return descriptor;
  };
}

// Usage
@Injectable()
export class CRFService {
  @LogExecutionTime()
  async createCRF(dto: CreateCRFDto): Promise<CRF> {
    // Method implementation
  }
}
```

#### 7. Observer Pattern (Event-Driven)
**Purpose**: Notify multiple objects about state changes.

```typescript
// events/CRFEvents.ts
export class CRFEventEmitter extends EventEmitter {
  onCRFCreated(callback: (crf: CRF) => void) {
    this.on('crf:created', callback);
  }
  
  onCRFUpdated(callback: (crf: CRF) => void) {
    this.on('crf:updated', callback);
  }
  
  emitCRFCreated(crf: CRF) {
    this.emit('crf:created', crf);
  }
}

// Service
@Injectable()
export class CRFService {
  constructor(private events: CRFEventEmitter) {}
  
  async createCRF(dto: CreateCRFDto): Promise<CRF> {
    const crf = await this.repository.create(dto);
    
    // Emit event
    this.events.emitCRFCreated(crf);
    
    return crf;
  }
}

// Listener
@Injectable()
export class NotificationService {
  constructor(events: CRFEventEmitter) {
    events.onCRFCreated(crf => {
      this.sendNotification(`CRF ${crf.name} created`);
    });
  }
}
```

---

## Frontend Architecture

### State Management

#### Local State (useState)
Use for component-specific state.
```typescript
const [isOpen, setIsOpen] = useState(false);
```

#### Global State (Zustand)
Use for app-wide state.
```typescript
// stores/designerStore.ts
export const useDesignerStore = create<DesignerState>((set) => ({
  selectedField: null,
  setSelectedField: (field) => set({ selectedField: field }),
  
  canvasNodes: [],
  addNode: (node) => set((state) => ({
    canvasNodes: [...state.canvasNodes, node],
  })),
}));
```

#### Server State (TanStack Query)
Use for API data.
```typescript
const { data, isLoading } = useQuery({
  queryKey: ['crfs'],
  queryFn: getCRFs,
});
```

### Component Structure

```
src/
├── features/          # Feature-based modules
│   ├── designer/
│   │   ├── components/    # Feature components
│   │   ├── hooks/         # Feature hooks
│   │   ├── types/         # Feature types
│   │   └── index.ts       # Public API
│   ├── library/
│   └── visitgrid/
├── components/        # Shared components
│   ├── ui/           # Base UI components
│   ├── forms/        # Form components
│   └── layout/       # Layout components
├── lib/              # Utilities
│   ├── api/          # API client
│   ├── validation/   # Validation utils
│   └── export/       # Export utils
└── types/            # Shared types
```

---

## Backend Architecture

### NestJS Module Structure

```
src/
├── modules/
│   ├── crf/
│   │   ├── crf.controller.ts      # HTTP endpoints
│   │   ├── crf.service.ts         # Business logic
│   │   ├── crf.repository.ts      # Data access
│   │   ├── dto/                   # DTOs
│   │   │   ├── create-crf.dto.ts
│   │   │   └── update-crf.dto.ts
│   │   ├── entities/              # Domain models
│   │   │   └── crf.entity.ts
│   │   └── crf.module.ts          # Module definition
│   ├── template/
│   ├── export/
│   └── visitgrid/
├── common/
│   ├── decorators/    # Custom decorators
│   ├── filters/       # Exception filters
│   ├── guards/        # Auth guards
│   ├── interceptors/  # Interceptors
│   ├── pipes/         # Validation pipes
│   └── utils/         # Utilities
└── config/            # Configuration
```

### API Response Format

**Standard Success Response**:
```typescript
{
  "success": true,
  "data": {
    // Response data
  },
  "meta": {
    "timestamp": "2026-02-03T12:00:00Z",
    "version": "1.0"
  }
}
```

**Standard Error Response**:
```typescript
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "name",
        "message": "Name is required"
      }
    ]
  },
  "meta": {
    "timestamp": "2026-02-03T12:00:00Z"
  }
}
```

---

## Code Organization

### File Naming Conventions

```
# Components
CRFDesigner.tsx          # React component
CRFDesigner.test.tsx     # Component tests
CRFDesigner.stories.tsx  # Storybook stories
CRFDesigner.module.css   # CSS modules

# Hooks
useCRFs.ts              # Custom hook
useCRFs.test.ts         # Hook tests

# Services
crf.service.ts          # Service
crf.service.spec.ts     # Service tests

# Types
crf.types.ts            # TypeScript types
crf.interface.ts        # Interfaces

# Utils
validation.util.ts      # Utility functions
validation.util.test.ts # Util tests
```

### Import Organization

```typescript
// 1. External imports (third-party)
import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';

// 2. Internal imports (features)
import { useCRFs } from '@/features/designer/hooks';
import { CRFList } from '@/features/designer/components';

// 3. Shared imports (components, utils)
import { Button } from '@/components/ui';
import { api } from '@/lib/api';

// 4. Type imports
import type { CRF, Section } from '@/types';

// 5. Styles
import styles from './CRFDesigner.module.css';
```

---

## Naming Conventions

### Variables
```typescript
// Use camelCase
const crfName = 'Demographics';
const isValid = true;

// Boolean variables start with is/has/should
const isLoading = false;
const hasErrors = true;
const shouldValidate = true;

// Constants use UPPER_SNAKE_CASE
const MAX_FIELD_LENGTH = 255;
const DEFAULT_TIMEOUT = 5000;
```

### Functions
```typescript
// Use camelCase, verb-noun format
function createCRF() {}
function getCRFById() {}
function validateField() {}
function handleSubmit() {}

// Event handlers start with handle
function handleClick() {}
function handleChange() {}

// Boolean functions use is/has/can
function isValid() {}
function hasPermission() {}
function canEdit() {}
```

### Classes & Interfaces
```typescript
// Use PascalCase
class CRFService {}
interface ICRFRepository {}
type CRFDefinition = {};

// Interface names can start with I (optional)
interface ICRFExporter {}
// Or use descriptive names
interface CRFExporter {}
```

### Types
```typescript
// Use PascalCase for types
type CRFStatus = 'draft' | 'published' | 'archived';
type CRF = { /* ... */ };

// Use descriptive names
type ValidationResult = { valid: boolean; message: string };
type ExportFormat = 'odm' | 'excel' | 'json';
```

---

## Best Practices

### 1. Error Handling

```typescript
// Backend
@Get(':id')
async getCRF(@Param('id') id: string) {
  try {
    const crf = await this.crfService.findById(id);
    if (!crf) {
      throw new NotFoundException(`CRF with ID ${id} not found`);
    }
    return crf;
  } catch (error) {
    if (error instanceof NotFoundException) {
      throw error;
    }
    throw new InternalServerErrorException('Failed to fetch CRF');
  }
}

// Frontend
const { data, error, isLoading } = useQuery({
  queryKey: ['crf', id],
  queryFn: () => api.getCRF(id),
  onError: (error) => {
    toast.error(error.message);
  },
});
```

### 2. Input Validation

```typescript
// Use class-validator for DTOs
export class CreateCRFDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  name: string;
  
  @IsString()
  @IsOptional()
  @MaxLength(1000)
  description?: string;
  
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SectionDto)
  sections: SectionDto[];
}
```

### 3. Database Transactions

```typescript
async createCRFWithSections(dto: CreateCRFDto): Promise<CRF> {
  return this.prisma.$transaction(async (tx) => {
    // Create CRF
    const crf = await tx.crf.create({
      data: {
        name: dto.name,
        description: dto.description,
      },
    });
    
    // Create sections
    await tx.section.createMany({
      data: dto.sections.map(s => ({
        ...s,
        crfId: crf.id,
      })),
    });
    
    return crf;
  });
}
```

### 4. Logging

```typescript
// Use structured logging
this.logger.log({
  message: 'CRF created',
  crfId: crf.id,
  userId: user.id,
  timestamp: new Date(),
});

this.logger.error({
  message: 'Failed to create CRF',
  error: error.message,
  stack: error.stack,
  userId: user.id,
});
```

### 5. Testing

```typescript
// Unit test
describe('CRFService', () => {
  let service: CRFService;
  let repository: MockType<ICRFRepository>;
  
  beforeEach(() => {
    repository = createMock<ICRFRepository>();
    service = new CRFService(repository);
  });
  
  it('should create CRF', async () => {
    const dto = { name: 'Test CRF' };
    repository.create.mockResolvedValue({ id: '1', ...dto });
    
    const result = await service.createCRF(dto);
    
    expect(result).toHaveProperty('id');
    expect(repository.create).toHaveBeenCalledWith(dto);
  });
});
```

### 6. Security

```typescript
// Sanitize user input
import { sanitize } from 'dompurify';

const cleanInput = sanitize(userInput);

// Use parameterized queries (Prisma does this automatically)
await prisma.crf.findMany({
  where: { name: { contains: searchTerm } }, // Safe
});

// Validate permissions
@UseGuards(AuthGuard, PermissionGuard)
@RequirePermission('crf:create')
@Post()
async createCRF() {}
```

### 7. Performance

```typescript
// Lazy loading
const CRFDesigner = lazy(() => import('./CRFDesigner'));

// Memoization
const expensiveComputation = useMemo(() => {
  return computeComplexValue(data);
}, [data]);

// Virtualization for large lists
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={items.length}
  itemSize={50}
>
  {({ index, style }) => (
    <div style={style}>{items[index]}</div>
  )}
</FixedSizeList>
```

---

## Conclusion

Following these architectural principles and design patterns ensures:
- **Consistency** across the codebase
- **Maintainability** for future developers
- **Scalability** as the application grows
- **Testability** with clear interfaces
- **Readability** with standard conventions

Refer to this guide when:
- Starting new features
- Reviewing code
- Refactoring existing code
- Onboarding new developers

---

**Next Steps**:
- Review [API Design Guide](API-DESIGN.md)
- Check [Codebase Structure](CODEBASE-STRUCTURE.md)
- Follow [Step-by-Step Guide](../guides/STEP-BY-STEP-GUIDE.md)
