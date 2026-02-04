# Step-by-Step Development Guide

## Overview

This comprehensive guide walks through building the CRF Design Studio from scratch, with detailed steps, code examples, testing, and validation at each stage.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Phase 1: Project Setup](#phase-1-project-setup)
3. [Phase 2: Backend Foundation](#phase-2-backend-foundation)
4. [Phase 3: Frontend Foundation](#phase-3-frontend-foundation)
5. [Phase 4: Visual Designer](#phase-4-visual-designer)
6. [Phase 5: Testing & Validation](#phase-5-testing--validation)

---

## Prerequisites

### Required Tools
```bash
# Node.js 20+
node --version  # v20.0.0+

# npm or yarn
npm --version   # 10.0.0+

# Git
git --version   # 2.0.0+

# Docker (for PostgreSQL)
docker --version  # 20.0.0+

# IDE
# VS Code, WebStorm, or similar
```

### Recommended VS Code Extensions
```json
{
  "recommendations": [
    "dbaeumer.vscode-eslint",
    "esbenp.prettier-vscode",
    "bradlc.vscode-tailwindcss",
    "prisma.prisma",
    "ms-playwright.playwright"
  ]
}
```

---

## Phase 1: Project Setup

### Step 1.1: Initialize Monorepo

```bash
# Create project directory
mkdir crf-design-studio
cd crf-design-studio

# Initialize Git
git init
git branch -M main

# Create .gitignore
cat > .gitignore << 'EOF'
node_modules/
dist/
build/
.env
.env.local
*.log
.DS_Store
coverage/
.vscode/
EOF

# Initialize npm workspace
npm init -y
```

### Step 1.2: Configure Monorepo Structure

```bash
# Create workspace structure
mkdir -p packages/backend
mkdir -p packages/frontend
mkdir -p packages/shared

# Update root package.json
cat > package.json << 'EOF'
{
  "name": "crf-design-studio",
  "version": "1.0.0",
  "private": true,
  "workspaces": [
    "packages/*"
  ],
  "scripts": {
    "dev": "concurrently \"npm run dev -w backend\" \"npm run dev -w frontend\"",
    "build": "npm run build --workspaces",
    "test": "npm run test --workspaces",
    "lint": "npm run lint --workspaces"
  },
  "devDependencies": {
    "concurrently": "^8.2.0"
  }
}
EOF
```

### Step 1.3: Set Up Docker for PostgreSQL

```bash
# Create docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: crf-postgres
    environment:
      POSTGRES_DB: crf_design_studio
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - '5432:5432'
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
EOF

# Start PostgreSQL
docker-compose up -d

# Verify
docker ps
```

### ✅ Validation Checkpoint 1
```bash
# Test PostgreSQL connection
docker exec -it crf-postgres psql -U postgres -d crf_design_studio -c "SELECT version();"

# Expected output: PostgreSQL version info
```

---

## Phase 2: Backend Foundation

### Step 2.1: Initialize NestJS Backend

```bash
cd packages/backend

# Install NestJS CLI
npm install -g @nestjs/cli

# Create NestJS app
npx @nestjs/cli new . --skip-git

# Install dependencies
npm install @nestjs/config \
  @prisma/client \
  prisma \
  class-validator \
  class-transformer \
  @nestjs/swagger \
  swagger-ui-express

# Install dev dependencies
npm install -D @types/node typescript ts-node
```

### Step 2.2: Configure Prisma

```bash
# Initialize Prisma
npx prisma init

# Update .env
cat > .env << 'EOF'
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/crf_design_studio"
EOF

# Create schema
cat > prisma/schema.prisma << 'EOF'
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model CRF {
  id          String   @id @default(uuid())
  name        String
  description String?
  status      String   @default("draft")
  definition  Json
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  versions CRFVersion[]

  @@map("crfs")
}

model CRFVersion {
  id         String   @id @default(uuid())
  crfId      String
  version    String
  definition Json
  createdAt  DateTime @default(now())

  crf CRF @relation(fields: [crfId], references: [id])

  @@map("crf_versions")
}
EOF

# Run migration
npx prisma migrate dev --name init

# Generate Prisma client
npx prisma generate
```

### Step 2.3: Create CRF Module

```bash
# Generate module, service, controller
npx nest generate module crf
npx nest generate service crf
npx nest generate controller crf
```

**File: `src/crf/crf.service.ts`**
```typescript
import { Injectable } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';
import { CreateCRFDto, UpdateCRFDto } from './dto';

@Injectable()
export class CRFService {
  constructor(private prisma: PrismaClient) {}

  async findAll() {
    return this.prisma.cRF.findMany({
      orderBy: { createdAt: 'desc' },
    });
  }

  async findOne(id: string) {
    return this.prisma.cRF.findUnique({
      where: { id },
      include: { versions: true },
    });
  }

  async create(data: CreateCRFDto) {
    return this.prisma.cRF.create({
      data: {
        name: data.name,
        description: data.description,
        definition: data.definition || {},
      },
    });
  }

  async update(id: string, data: UpdateCRFDto) {
    return this.prisma.cRF.update({
      where: { id },
      data: {
        name: data.name,
        description: data.description,
        definition: data.definition,
      },
    });
  }

  async delete(id: string) {
    return this.prisma.cRF.delete({
      where: { id },
    });
  }
}
```

**File: `src/crf/dto/create-crf.dto.ts`**
```typescript
import { IsString, IsOptional, IsObject } from 'class-validator';

export class CreateCRFDto {
  @IsString()
  name: string;

  @IsString()
  @IsOptional()
  description?: string;

  @IsObject()
  @IsOptional()
  definition?: any;
}
```

**File: `src/crf/crf.controller.ts`**
```typescript
import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
} from '@nestjs/common';
import { CRFService } from './crf.service';
import { CreateCRFDto, UpdateCRFDto } from './dto';

@Controller('api/crfs')
export class CRFController {
  constructor(private crfService: CRFService) {}

  @Get()
  findAll() {
    return this.crfService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.crfService.findOne(id);
  }

  @Post()
  create(@Body() data: CreateCRFDto) {
    return this.crfService.create(data);
  }

  @Put(':id')
  update(@Param('id') id: string, @Body() data: UpdateCRFDto) {
    return this.crfService.update(id, data);
  }

  @Delete(':id')
  delete(@Param('id') id: string) {
    return this.crfService.delete(id);
  }
}
```

### Step 2.4: Test Backend

```bash
# Run backend
npm run start:dev

# Test in another terminal
curl http://localhost:3000/api/crfs

# Create test CRF
curl -X POST http://localhost:3000/api/crfs \
  -H "Content-Type: application/json" \
  -d '{"name":"Test CRF","description":"Test"}'
```

### ✅ Validation Checkpoint 2
```bash
# Run tests
npm run test

# Verify API endpoints
curl http://localhost:3000/api/crfs | jq
# Should return empty array or list of CRFs
```

---

## Phase 3: Frontend Foundation

### Step 3.1: Initialize React App

```bash
cd ../frontend

# Create Vite app
npm create vite@latest . -- --template react-ts

# Install dependencies
npm install

# Install additional packages
npm install @tanstack/react-query \
  zustand \
  react-router-dom \
  @mui/material @emotion/react @emotion/styled \
  axios \
  reactflow \
  dnd-kit
```

### Step 3.2: Configure Project Structure

```bash
mkdir -p src/features/designer/components
mkdir -p src/features/designer/hooks
mkdir -p src/features/library
mkdir -p src/components/ui
mkdir -p src/lib/api
mkdir -p src/types
```

### Step 3.3: Set Up API Client

**File: `src/lib/api/client.ts`**
```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: 'http://localhost:3000/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const api = {
  getCRFs: () => apiClient.get('/crfs').then(res => res.data),
  getCRF: (id: string) => apiClient.get(`/crfs/${id}`).then(res => res.data),
  createCRF: (data: any) => apiClient.post('/crfs', data).then(res => res.data),
  updateCRF: (id: string, data: any) => 
    apiClient.put(`/crfs/${id}`, data).then(res => res.data),
  deleteCRF: (id: string) => apiClient.delete(`/crfs/${id}`).then(res => res.data),
};
```

### Step 3.4: Set Up React Query

**File: `src/main.tsx`**
```typescript
import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
);
```

### Step 3.5: Create CRF List Component

**File: `src/features/library/CRFList.tsx`**
```typescript
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api/client';
import { 
  Card, 
  CardContent, 
  Typography, 
  Button, 
  Grid 
} from '@mui/material';

export const CRFList: React.FC = () => {
  const { data: crfs, isLoading, error } = useQuery({
    queryKey: ['crfs'],
    queryFn: api.getCRFs,
  });

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading CRFs</div>;

  return (
    <Grid container spacing={2}>
      {crfs?.map((crf: any) => (
        <Grid item xs={12} sm={6} md={4} key={crf.id}>
          <Card>
            <CardContent>
              <Typography variant="h6">{crf.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {crf.description}
              </Typography>
              <Typography variant="caption">
                Created: {new Date(crf.createdAt).toLocaleDateString()}
              </Typography>
              <div style={{ marginTop: 16 }}>
                <Button variant="contained" size="small">
                  Edit
                </Button>
              </div>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
```

**File: `src/App.tsx`**
```typescript
import React from 'react';
import { Container, Typography, Button } from '@mui/material';
import { CRFList } from './features/library/CRFList';

function App() {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h3" gutterBottom>
        CRF Design Studio
      </Typography>
      <Button variant="contained" sx={{ mb: 3 }}>
        Create New CRF
      </Button>
      <CRFList />
    </Container>
  );
}

export default App;
```

### Step 3.6: Test Frontend

```bash
# Start dev server
npm run dev

# Open browser to http://localhost:5173
```

### ✅ Validation Checkpoint 3
```bash
# Verify frontend displays
# - Title "CRF Design Studio"
# - "Create New CRF" button
# - CRF list (empty or with test data)

# Test API integration
# - Create CRF via API
# - Verify it appears in UI
```

---

## Phase 4: Visual Designer

### Step 4.1: Install React Flow

```bash
cd packages/frontend
npm install reactflow
```

### Step 4.2: Create Designer Component

**File: `src/features/designer/components/CRFDesigner.tsx`**
```typescript
import React, { useCallback } from 'react';
import ReactFlow, {
  Node,
  Edge,
  addEdge,
  Connection,
  useNodesState,
  useEdgesState,
  Background,
  Controls,
  MiniMap,
} from 'reactflow';
import 'reactflow/dist/style.css';

const initialNodes: Node[] = [
  {
    id: '1',
    type: 'input',
    data: { label: 'Start' },
    position: { x: 250, y: 0 },
  },
];

const initialEdges: Edge[] = [];

export const CRFDesigner: React.FC = () => {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  return (
    <div style={{ height: '600px', border: '1px solid #ddd' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        fitView
      >
        <Background />
        <Controls />
        <MiniMap />
      </ReactFlow>
    </div>
  );
};
```

### Step 4.3: Create Custom Field Nodes

**File: `src/features/designer/components/nodes/TextFieldNode.tsx`**
```typescript
import React from 'react';
import { Handle, Position } from 'reactflow';

export const TextFieldNode: React.FC<{ data: any }> = ({ data }) => {
  return (
    <div style={{
      padding: '10px',
      border: '2px solid #1976d2',
      borderRadius: '4px',
      background: 'white',
      minWidth: '200px',
    }}>
      <Handle type="target" position={Position.Top} />
      <div style={{ marginBottom: '8px', fontWeight: 'bold' }}>
        {data.label}
      </div>
      <input 
        type="text" 
        placeholder={data.placeholder}
        style={{
          width: '100%',
          padding: '4px',
          border: '1px solid #ccc',
          borderRadius: '2px',
        }}
      />
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};
```

### Step 4.4: Create Field Palette

**File: `src/features/designer/components/FieldPalette.tsx`**
```typescript
import React from 'react';
import { Paper, Typography, List, ListItem, ListItemText } from '@mui/material';

const fieldTypes = [
  { type: 'text', label: 'Text Field' },
  { type: 'number', label: 'Number Field' },
  { type: 'date', label: 'Date Field' },
  { type: 'select', label: 'Dropdown' },
  { type: 'radio', label: 'Radio Buttons' },
  { type: 'checkbox', label: 'Checkboxes' },
];

export const FieldPalette: React.FC = () => {
  const onDragStart = (event: React.DragEvent, type: string) => {
    event.dataTransfer.setData('application/reactflow', type);
    event.dataTransfer.effectAllowed = 'move';
  };

  return (
    <Paper sx={{ p: 2, width: 200 }}>
      <Typography variant="h6" gutterBottom>
        Fields
      </Typography>
      <List>
        {fieldTypes.map((field) => (
          <ListItem
            key={field.type}
            draggable
            onDragStart={(e) => onDragStart(e, field.type)}
            sx={{
              cursor: 'grab',
              border: '1px solid #ddd',
              borderRadius: 1,
              mb: 1,
              '&:hover': {
                bgcolor: 'action.hover',
              },
            }}
          >
            <ListItemText primary={field.label} />
          </ListItem>
        ))}
      </List>
    </Paper>
  );
};
```

### ✅ Validation Checkpoint 4
```bash
# Manual testing checklist:
# 1. Designer canvas loads
# 2. Can drag fields from palette
# 3. Fields render as nodes
# 4. Can connect nodes
# 5. Zoom and pan work
# 6. Minimap shows canvas
```

---

## Phase 5: Testing & Validation

### Step 5.1: Unit Tests - Backend

**File: `packages/backend/src/crf/crf.service.spec.ts`**
```typescript
import { Test, TestingModule } from '@nestjs/testing';
import { CRFService } from './crf.service';
import { PrismaClient } from '@prisma/client';

describe('CRFService', () => {
  let service: CRFService;
  let prisma: PrismaClient;

  beforeEach(async () => {
    prisma = new PrismaClient();
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CRFService,
        { provide: PrismaClient, useValue: prisma },
      ],
    }).compile();

    service = module.get<CRFService>(CRFService);
  });

  afterEach(async () => {
    await prisma.$disconnect();
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  it('should create a CRF', async () => {
    const dto = { name: 'Test CRF', description: 'Test' };
    const result = await service.create(dto);
    
    expect(result).toHaveProperty('id');
    expect(result.name).toBe('Test CRF');
  });

  it('should find all CRFs', async () => {
    const result = await service.findAll();
    expect(Array.isArray(result)).toBe(true);
  });
});
```

```bash
# Run tests
npm run test
```

### Step 5.2: Unit Tests - Frontend

**File: `packages/frontend/src/features/library/CRFList.test.tsx`**
```typescript
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CRFList } from './CRFList';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

describe('CRFList', () => {
  it('renders loading state', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <CRFList />
      </QueryClientProvider>
    );
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders CRF cards', async () => {
    // Mock API response
    const mockCRFs = [
      { id: '1', name: 'Test CRF', description: 'Test', createdAt: new Date() },
    ];
    
    // ... test implementation
  });
});
```

```bash
# Run tests
npm run test
```

### Step 5.3: E2E Tests with Playwright

```bash
cd packages/frontend
npm install -D @playwright/test
npx playwright install
```

**File: `packages/frontend/e2e/crf-designer.spec.ts`**
```typescript
import { test, expect } from '@playwright/test';

test.describe('CRF Designer', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:5173');
  });

  test('should display CRF list', async ({ page }) => {
    await expect(page.getByText('CRF Design Studio')).toBeVisible();
    await expect(page.getByText('Create New CRF')).toBeVisible();
  });

  test('should create new CRF', async ({ page }) => {
    await page.getByText('Create New CRF').click();
    
    await page.fill('input[name="name"]', 'Test CRF');
    await page.fill('textarea[name="description"]', 'Test Description');
    await page.getByText('Save').click();
    
    await expect(page.getByText('Test CRF')).toBeVisible();
  });

  test('should drag field to canvas', async ({ page }) => {
    await page.goto('http://localhost:5173/designer');
    
    const textField = page.getByText('Text Field');
    const canvas = page.locator('.react-flow');
    
    await textField.dragTo(canvas);
    await expect(page.locator('.react-flow-node')).toBeVisible();
  });
});
```

```bash
# Run E2E tests
npx playwright test
```

### Step 5.4: Integration Testing

**Test API with real database:**
```typescript
// test/integration/crf.integration.spec.ts
describe('CRF Integration Tests', () => {
  beforeAll(async () => {
    // Set up test database
    await setupTestDatabase();
  });

  afterAll(async () => {
    // Clean up test database
    await cleanupTestDatabase();
  });

  it('should create CRF and retrieve it', async () => {
    // Create
    const created = await request(app.getHttpServer())
      .post('/api/crfs')
      .send({ name: 'Integration Test CRF' })
      .expect(201);

    // Retrieve
    const response = await request(app.getHttpServer())
      .get(`/api/crfs/${created.body.id}`)
      .expect(200);

    expect(response.body.name).toBe('Integration Test CRF');
  });
});
```

### ✅ Final Validation Checklist

```bash
# Backend
[ ] All unit tests pass
[ ] API endpoints work
[ ] Database operations succeed
[ ] Error handling works
[ ] Validation works

# Frontend
[ ] Components render correctly
[ ] API calls work
[ ] State management works
[ ] Forms submit correctly
[ ] Navigation works

# Integration
[ ] End-to-end flows work
[ ] Create → Read → Update → Delete
[ ] Designer saves and loads
[ ] Export functions work

# Code Quality
[ ] ESLint passes
[ ] Prettier formats code
[ ] TypeScript compiles
[ ] No console errors
[ ] Good test coverage (>70%)
```

---

## Next Steps

1. **Continue to Phase 2**: Add more features (validation, export, etc.)
2. **Review**: [Architecture Patterns](../development/ARCHITECTURE-PATTERNS.md)
3. **Deploy**: Follow [Deployment Guide](DEPLOYMENT.md)
4. **Monitor**: Set up [Monitoring](MONITORING.md)

---

## Troubleshooting

### Common Issues

**Issue**: Cannot connect to PostgreSQL
**Solution**: Ensure Docker container is running
```bash
docker ps
docker-compose up -d
```

**Issue**: Prisma migration fails
**Solution**: Reset database
```bash
npx prisma migrate reset
npx prisma migrate dev
```

**Issue**: Frontend can't reach backend
**Solution**: Check CORS configuration
```typescript
// main.ts
app.enableCors({
  origin: 'http://localhost:5173',
});
```

---

**This guide provides a solid foundation. Continue building features incrementally, testing at each step.**
