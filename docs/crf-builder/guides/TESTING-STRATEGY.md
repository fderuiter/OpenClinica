# Testing Strategy

## Overview

Comprehensive testing strategy for the CRF Design Studio covering all layers from unit tests to E2E tests.

---

## Testing Pyramid

```
        /\
       /E2E\         ← Few, slow, expensive
      /______\
     /        \
    /Integration\   ← Some, medium speed
   /____________\
  /              \
 /   Unit Tests   \  ← Many, fast, cheap
/__________________\
```

**Distribution**:
- **70%** Unit Tests
- **20%** Integration Tests
- **10%** E2E Tests

---

## Unit Testing

### Frontend Unit Tests

**Tools**: Vitest + React Testing Library

**Setup**: `packages/frontend/vitest.config.ts`
```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: ['node_modules/', 'src/test/'],
    },
  },
});
```

**Example Component Test**:
```typescript
// CRFCard.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { CRFCard } from './CRFCard';

describe('CRFCard', () => {
  const mockCRF = {
    id: '1',
    name: 'Demographics',
    description: 'Patient demographics',
    createdAt: '2026-02-03',
  };

  it('renders CRF information', () => {
    render(<CRFCard crf={mockCRF} />);
    
    expect(screen.getByText('Demographics')).toBeInTheDocument();
    expect(screen.getByText('Patient demographics')).toBeInTheDocument();
  });

  it('calls onEdit when edit button clicked', () => {
    const onEdit = vi.fn();
    render(<CRFCard crf={mockCRF} onEdit={onEdit} />);
    
    fireEvent.click(screen.getByText('Edit'));
    expect(onEdit).toHaveBeenCalledWith(mockCRF);
  });

  it('displays formatted date', () => {
    render(<CRFCard crf={mockCRF} />);
    expect(screen.getByText(/Feb 3, 2026/)).toBeInTheDocument();
  });
});
```

**Custom Hook Test**:
```typescript
// useCRFs.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCRFs } from './useCRFs';
import { api } from '@/lib/api';

vi.mock('@/lib/api');

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('useCRFs', () => {
  it('fetches CRFs successfully', async () => {
    const mockCRFs = [{ id: '1', name: 'Test CRF' }];
    vi.mocked(api.getCRFs).mockResolvedValue(mockCRFs);

    const { result } = renderHook(() => useCRFs(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockCRFs);
  });

  it('handles errors', async () => {
    vi.mocked(api.getCRFs).mockRejectedValue(new Error('Failed'));

    const { result } = renderHook(() => useCRFs(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toBeDefined();
  });
});
```

### Backend Unit Tests

**Tools**: Jest + NestJS Testing

**Example Service Test**:
```typescript
// crf.service.spec.ts
import { Test } from '@nestjs/testing';
import { CRFService } from './crf.service';
import { PrismaClient } from '@prisma/client';
import { CreateCRFDto } from './dto';

describe('CRFService', () => {
  let service: CRFService;
  let prisma: PrismaClient;

  beforeEach(async () => {
    const module = await Test.createTestingModule({
      providers: [
        CRFService,
        {
          provide: PrismaClient,
          useValue: {
            cRF: {
              create: jest.fn(),
              findMany: jest.fn(),
              findUnique: jest.fn(),
              update: jest.fn(),
              delete: jest.fn(),
            },
          },
        },
      ],
    }).compile();

    service = module.get<CRFService>(CRFService);
    prisma = module.get<PrismaClient>(PrismaClient);
  });

  describe('create', () => {
    it('should create a CRF', async () => {
      const dto: CreateCRFDto = {
        name: 'Test CRF',
        description: 'Test',
      };

      const expected = { id: '1', ...dto, createdAt: new Date() };
      jest.spyOn(prisma.cRF, 'create').mockResolvedValue(expected);

      const result = await service.create(dto);

      expect(result).toEqual(expected);
      expect(prisma.cRF.create).toHaveBeenCalledWith({
        data: expect.objectContaining({
          name: dto.name,
          description: dto.description,
        }),
      });
    });

    it('should throw error for invalid data', async () => {
      const dto: CreateCRFDto = { name: '', description: '' };

      await expect(service.create(dto)).rejects.toThrow();
    });
  });

  describe('findAll', () => {
    it('should return array of CRFs', async () => {
      const expected = [
        { id: '1', name: 'CRF 1' },
        { id: '2', name: 'CRF 2' },
      ];
      jest.spyOn(prisma.cRF, 'findMany').mockResolvedValue(expected);

      const result = await service.findAll();

      expect(result).toEqual(expected);
      expect(prisma.cRF.findMany).toHaveBeenCalled();
    });
  });
});
```

**Example Controller Test**:
```typescript
// crf.controller.spec.ts
import { Test } from '@nestjs/testing';
import { CRFController } from './crf.controller';
import { CRFService } from './crf.service';

describe('CRFController', () => {
  let controller: CRFController;
  let service: CRFService;

  beforeEach(async () => {
    const module = await Test.createTestingModule({
      controllers: [CRFController],
      providers: [
        {
          provide: CRFService,
          useValue: {
            findAll: jest.fn(),
            findOne: jest.fn(),
            create: jest.fn(),
            update: jest.fn(),
            delete: jest.fn(),
          },
        },
      ],
    }).compile();

    controller = module.get<CRFController>(CRFController);
    service = module.get<CRFService>(CRFService);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });

  describe('findAll', () => {
    it('should return array of CRFs', async () => {
      const expected = [{ id: '1', name: 'CRF 1' }];
      jest.spyOn(service, 'findAll').mockResolvedValue(expected);

      const result = await controller.findAll();

      expect(result).toEqual(expected);
    });
  });
});
```

---

## Integration Testing

### API Integration Tests

**Tools**: Supertest + Test Database

**Setup**:
```typescript
// test/integration/setup.ts
import { INestApplication } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { AppModule } from '@/app.module';
import { PrismaClient } from '@prisma/client';

export async function createTestApp(): Promise<INestApplication> {
  const moduleRef = await Test.createTestingModule({
    imports: [AppModule],
  }).compile();

  const app = moduleRef.createNestApplication();
  await app.init();
  return app;
}

export async function cleanDatabase() {
  const prisma = new PrismaClient();
  await prisma.cRFVersion.deleteMany();
  await prisma.cRF.deleteMany();
  await prisma.$disconnect();
}
```

**Example Integration Test**:
```typescript
// crf.integration.spec.ts
import request from 'supertest';
import { INestApplication } from '@nestjs/common';
import { createTestApp, cleanDatabase } from './setup';

describe('CRF API Integration', () => {
  let app: INestApplication;

  beforeAll(async () => {
    app = await createTestApp();
  });

  afterAll(async () => {
    await cleanDatabase();
    await app.close();
  });

  describe('POST /api/crfs', () => {
    it('should create a new CRF', async () => {
      const dto = {
        name: 'Integration Test CRF',
        description: 'Test description',
      };

      const response = await request(app.getHttpServer())
        .post('/api/crfs')
        .send(dto)
        .expect(201);

      expect(response.body).toMatchObject({
        id: expect.any(String),
        name: dto.name,
        description: dto.description,
        createdAt: expect.any(String),
      });
    });

    it('should return 400 for invalid data', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/crfs')
        .send({ name: '' })
        .expect(400);

      expect(response.body.message).toBeDefined();
    });
  });

  describe('GET /api/crfs', () => {
    it('should return list of CRFs', async () => {
      // Create test data
      await request(app.getHttpServer())
        .post('/api/crfs')
        .send({ name: 'Test CRF 1' });

      const response = await request(app.getHttpServer())
        .get('/api/crfs')
        .expect(200);

      expect(Array.isArray(response.body)).toBe(true);
      expect(response.body.length).toBeGreaterThan(0);
    });
  });

  describe('Full CRUD flow', () => {
    it('should create, read, update, and delete', async () => {
      // Create
      const createRes = await request(app.getHttpServer())
        .post('/api/crfs')
        .send({ name: 'CRUD Test CRF' })
        .expect(201);

      const crfId = createRes.body.id;

      // Read
      const readRes = await request(app.getHttpServer())
        .get(`/api/crfs/${crfId}`)
        .expect(200);

      expect(readRes.body.id).toBe(crfId);

      // Update
      const updateRes = await request(app.getHttpServer())
        .put(`/api/crfs/${crfId}`)
        .send({ name: 'Updated CRF' })
        .expect(200);

      expect(updateRes.body.name).toBe('Updated CRF');

      // Delete
      await request(app.getHttpServer())
        .delete(`/api/crfs/${crfId}`)
        .expect(200);

      // Verify deleted
      await request(app.getHttpServer())
        .get(`/api/crfs/${crfId}`)
        .expect(404);
    });
  });
});
```

---

## End-to-End Testing

### E2E Tests with Playwright

**Setup**: `packages/frontend/playwright.config.ts`
```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
});
```

**Example E2E Tests**:
```typescript
// e2e/crf-designer.spec.ts
import { test, expect } from '@playwright/test';

test.describe('CRF Designer Workflow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('complete CRF creation workflow', async ({ page }) => {
    // Step 1: Navigate to create CRF
    await page.getByRole('button', { name: 'Create New CRF' }).click();
    await expect(page).toHaveURL(/.*\/crfs\/new/);

    // Step 2: Fill in CRF details
    await page.getByLabel('Name').fill('E2E Test CRF');
    await page.getByLabel('Description').fill('Created via E2E test');
    await page.getByRole('button', { name: 'Next' }).click();

    // Step 3: Design form - add fields
    await page.getByText('Text Field').dragTo(page.locator('.react-flow'));
    await expect(page.locator('.react-flow-node')).toBeVisible();

    // Step 4: Configure field
    await page.locator('.react-flow-node').first().click();
    await page.getByLabel('Field Label').fill('Patient Name');
    await page.getByLabel('Required').check();

    // Step 5: Save CRF
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('CRF saved successfully')).toBeVisible();

    // Step 6: Verify in library
    await page.goto('/');
    await expect(page.getByText('E2E Test CRF')).toBeVisible();
  });

  test('drag and drop fields', async ({ page }) => {
    await page.goto('/designer');

    const palette = page.locator('.field-palette');
    const canvas = page.locator('.react-flow');

    // Drag text field
    await palette.getByText('Text Field').dragTo(canvas, {
      targetPosition: { x: 100, y: 100 },
    });

    // Verify node added
    const nodes = page.locator('.react-flow-node');
    await expect(nodes).toHaveCount(1);

    // Drag number field
    await palette.getByText('Number Field').dragTo(canvas, {
      targetPosition: { x: 100, y: 200 },
    });

    await expect(nodes).toHaveCount(2);
  });

  test('field configuration', async ({ page }) => {
    await page.goto('/designer');

    // Add field
    await page.getByText('Number Field').dragTo(page.locator('.react-flow'));

    // Select field
    await page.locator('.react-flow-node').click();

    // Configure properties
    await page.getByLabel('Label').fill('Age');
    await page.getByLabel('Min Value').fill('0');
    await page.getByLabel('Max Value').fill('120');
    await page.getByLabel('Required').check();

    // Verify configuration saved
    await expect(page.getByText('Age')).toBeVisible();
  });

  test('export to Excel', async ({ page }) => {
    await page.goto('/crfs/1');

    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('button', { name: 'Export to Excel' }).click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toMatch(/\.xls$/);
  });
});
```

---

## Visual Regression Testing

**Tool**: Playwright + Percy

```typescript
// e2e/visual.spec.ts
import { test } from '@playwright/test';
import percySnapshot from '@percy/playwright';

test.describe('Visual Regression', () => {
  test('CRF library page', async ({ page }) => {
    await page.goto('/');
    await percySnapshot(page, 'CRF Library');
  });

  test('CRF designer canvas', async ({ page }) => {
    await page.goto('/designer');
    await percySnapshot(page, 'Designer Canvas');
  });

  test('field palette', async ({ page }) => {
    await page.goto('/designer');
    await percySnapshot(page, 'Field Palette');
  });
});
```

---

## Performance Testing

**Tool**: Lighthouse CI

```yaml
# .lighthouserc.js
module.exports = {
  ci: {
    collect: {
      startServerCommand: 'npm run preview',
      url: ['http://localhost:4173/'],
      numberOfRuns: 3,
    },
    assert: {
      assertions: {
        'categories:performance': ['error', { minScore: 0.9 }],
        'categories:accessibility': ['error', { minScore: 0.9 }],
        'categories:best-practices': ['error', { minScore: 0.9 }],
      },
    },
  },
};
```

---

## Test Coverage

### Coverage Goals

| Layer | Target | Critical |
|-------|--------|----------|
| Unit Tests | 80% | 90% |
| Integration | 60% | 80% |
| E2E | 40% | 60% |

### Generate Coverage Reports

```bash
# Frontend
npm run test:coverage

# Backend
npm run test:cov

# View reports
open coverage/index.html
```

---

## Continuous Integration

### GitHub Actions Workflow

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run linters
        run: npm run lint

      - name: Run unit tests
        run: npm run test:coverage

      - name: Run E2E tests
        run: npx playwright install --with-deps && npm run test:e2e

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/coverage-final.json
```

---

## Testing Best Practices

### 1. Arrange-Act-Assert Pattern

```typescript
test('should create CRF', async () => {
  // Arrange
  const dto = { name: 'Test CRF' };
  
  // Act
  const result = await service.create(dto);
  
  // Assert
  expect(result).toHaveProperty('id');
  expect(result.name).toBe('Test CRF');
});
```

### 2. Test Naming Convention

```typescript
// Good
test('should create CRF when given valid data');
test('should throw error when name is empty');

// Bad
test('test1');
test('create');
```

### 3. Mock External Dependencies

```typescript
// Mock API calls
vi.mock('@/lib/api', () => ({
  api: {
    getCRFs: vi.fn(),
    createCRF: vi.fn(),
  },
}));
```

### 4. Clean Up After Tests

```typescript
afterEach(async () => {
  await cleanDatabase();
  vi.clearAllMocks();
});
```

### 5. Test Data Factories

```typescript
// test/factories/crf.factory.ts
export const createMockCRF = (overrides = {}) => ({
  id: '1',
  name: 'Test CRF',
  description: 'Test',
  createdAt: new Date(),
  ...overrides,
});
```

---

## Running Tests

```bash
# Run all tests
npm run test

# Watch mode
npm run test:watch

# Coverage
npm run test:coverage

# E2E tests
npm run test:e2e

# Specific test file
npm run test -- CRFCard.test.tsx

# Update snapshots
npm run test -- -u
```

---

## Conclusion

Comprehensive testing ensures:
- ✅ Code quality
- ✅ Confidence in changes
- ✅ Documentation via tests
- ✅ Regression prevention
- ✅ Faster debugging

**Next Steps**:
1. Set up test infrastructure
2. Write tests alongside features
3. Maintain coverage > 80%
4. Review test results in CI
