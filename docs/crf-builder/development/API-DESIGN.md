# API Design & Conventions

## Overview

RESTful API design principles and conventions for the CRF Design Studio.

---

## API Structure

### Base URL

```
Development: http://localhost:3000/api
Production:  https://api.crf-studio.com/api
```

### Versioning

```
/api/v1/crfs
/api/v1/templates
```

Start with v1, increment for breaking changes.

---

## RESTful Endpoints

### CRF Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/crfs` | List all CRFs |
| `POST` | `/api/v1/crfs` | Create new CRF |
| `GET` | `/api/v1/crfs/:id` | Get CRF by ID |
| `PUT` | `/api/v1/crfs/:id` | Update CRF |
| `PATCH` | `/api/v1/crfs/:id` | Partial update |
| `DELETE` | `/api/v1/crfs/:id` | Delete CRF |

### CRF Versions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/crfs/:id/versions` | List versions |
| `POST` | `/api/v1/crfs/:id/versions` | Create version |
| `GET` | `/api/v1/crfs/:id/versions/:versionId` | Get version |
| `PUT` | `/api/v1/crfs/:id/versions/:versionId` | Update version |

### Export

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/crfs/:id/export/excel` | Export to Excel |
| `GET` | `/api/v1/crfs/:id/export/odm` | Export to ODM XML |
| `GET` | `/api/v1/crfs/:id/export/json` | Export to JSON |

### Visit Grid

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/visit-configs` | List visit configs |
| `POST` | `/api/v1/visit-configs` | Create config |
| `GET` | `/api/v1/visit-configs/:id` | Get config |
| `PUT` | `/api/v1/visit-configs/:id` | Update config |

---

## Request/Response Format

### Standard Response Envelope

```typescript
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
  meta: {
    timestamp: string;
    version: string;
  };
}
```

### Success Response

```json
{
  "success": true,
  "data": {
    "id": "crf_123",
    "name": "Demographics",
    "description": "Patient demographics form"
  },
  "meta": {
    "timestamp": "2026-02-03T12:00:00Z",
    "version": "1.0"
  }
}
```

### Error Response

```json
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

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Invalid input data |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource already exists |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Pagination

### Query Parameters

```
GET /api/v1/crfs?page=1&limit=20&sort=name&order=asc
```

### Response

```json
{
  "success": true,
  "data": [ /* items */ ],
  "meta": {
    "pagination": {
      "page": 1,
      "limit": 20,
      "totalPages": 5,
      "totalItems": 100,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

## Filtering & Sorting

### Filtering

```
GET /api/v1/crfs?status=published&name=demographics
```

### Sorting

```
GET /api/v1/crfs?sort=createdAt&order=desc
```

### Advanced Filtering

```
GET /api/v1/crfs?filter[status]=published&filter[createdAt][gte]=2026-01-01
```

---

## OpenAPI/Swagger Specification

### Example

```yaml
# swagger.yaml
openapi: 3.0.0
info:
  title: CRF Design Studio API
  version: 1.0.0
  description: API for CRF design and management

paths:
  /api/v1/crfs:
    get:
      summary: List CRFs
      tags: [CRFs]
      parameters:
        - in: query
          name: page
          schema:
            type: integer
            default: 1
        - in: query
          name: limit
          schema:
            type: integer
            default: 20
      responses:
        200:
          description: List of CRFs
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CRFList'
    
    post:
      summary: Create CRF
      tags: [CRFs]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateCRFDto'
      responses:
        201:
          description: CRF created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CRF'

components:
  schemas:
    CRF:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        description:
          type: string
        createdAt:
          type: string
          format: date-time
    
    CreateCRFDto:
      type: object
      required:
        - name
      properties:
        name:
          type: string
        description:
          type: string
```

---

## NestJS Implementation

### DTO (Data Transfer Object)

```typescript
// dto/create-crf.dto.ts
import { IsString, IsNotEmpty, IsOptional, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateCRFDto {
  @ApiProperty({
    description: 'CRF name',
    example: 'Demographics',
    maxLength: 255,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  name: string;

  @ApiProperty({
    description: 'CRF description',
    example: 'Patient demographics form',
    required: false,
  })
  @IsString()
  @IsOptional()
  description?: string;
}
```

### Controller

```typescript
// crf.controller.ts
import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { CRFService } from './crf.service';
import { CreateCRFDto, UpdateCRFDto, PaginationDto } from './dto';

@ApiTags('CRFs')
@Controller('api/v1/crfs')
export class CRFController {
  constructor(private crfService: CRFService) {}

  @Get()
  @ApiOperation({ summary: 'List all CRFs' })
  @ApiResponse({ status: 200, description: 'List of CRFs' })
  async findAll(@Query() pagination: PaginationDto) {
    const crfs = await this.crfService.findAll(pagination);
    return {
      success: true,
      data: crfs,
      meta: {
        timestamp: new Date().toISOString(),
      },
    };
  }

  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create new CRF' })
  @ApiResponse({ status: 201, description: 'CRF created' })
  @ApiResponse({ status: 400, description: 'Validation error' })
  async create(@Body() dto: CreateCRFDto) {
    const crf = await this.crfService.create(dto);
    return {
      success: true,
      data: crf,
      meta: {
        timestamp: new Date().toISOString(),
      },
    };
  }
}
```

---

## Authentication & Authorization

### JWT Token

```typescript
// auth.guard.ts
@Injectable()
export class AuthGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const token = request.headers.authorization?.split(' ')[1];
    
    if (!token) {
      throw new UnauthorizedException('No token provided');
    }
    
    try {
      const payload = jwt.verify(token, process.env.JWT_SECRET);
      request.user = payload;
      return true;
    } catch {
      throw new UnauthorizedException('Invalid token');
    }
  }
}

// Usage
@UseGuards(AuthGuard)
@Get()
findAll() {
  // ...
}
```

---

## Rate Limiting

```typescript
// main.ts
import rateLimit from 'express-rate-limit';

app.use(
  rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // limit each IP to 100 requests per windowMs
  })
);
```

---

## CORS Configuration

```typescript
// main.ts
app.enableCors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:5173',
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
});
```

---

## Best Practices

### 1. Use Proper HTTP Methods

- `GET` - Read
- `POST` - Create
- `PUT` - Full update
- `PATCH` - Partial update
- `DELETE` - Delete

### 2. Return Appropriate Status Codes

```typescript
@Get(':id')
async findOne(@Param('id') id: string) {
  const crf = await this.service.findOne(id);
  if (!crf) {
    throw new NotFoundException(`CRF ${id} not found`);
  }
  return crf;
}
```

### 3. Validate Input

```typescript
@Post()
async create(@Body(ValidationPipe) dto: CreateCRFDto) {
  return this.service.create(dto);
}
```

### 4. Handle Errors Gracefully

```typescript
@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  catch(exception: any, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse();
    
    const status = exception.getStatus?.() || 500;
    
    response.status(status).json({
      success: false,
      error: {
        code: exception.name,
        message: exception.message,
      },
      meta: {
        timestamp: new Date().toISOString(),
      },
    });
  }
}
```

### 5. Document Everything

```typescript
@ApiOperation({ summary: 'Create new CRF' })
@ApiResponse({ status: 201, description: 'CRF created successfully' })
@ApiResponse({ status: 400, description: 'Invalid input data' })
@ApiBody({ type: CreateCRFDto })
```

---

## Testing APIs

### With cURL

```bash
# GET
curl http://localhost:3000/api/v1/crfs

# POST
curl -X POST http://localhost:3000/api/v1/crfs \
  -H "Content-Type: application/json" \
  -d '{"name":"Test CRF"}'

# PUT
curl -X PUT http://localhost:3000/api/v1/crfs/123 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated CRF"}'

# DELETE
curl -X DELETE http://localhost:3000/api/v1/crfs/123
```

### With Postman

Import OpenAPI spec:
```
http://localhost:3000/api-docs-json
```

---

## Conclusion

Following these API conventions ensures:
- ✅ Consistent API design
- ✅ Easy to understand and use
- ✅ Self-documenting with Swagger
- ✅ Proper error handling
- ✅ Security best practices

**Next**: Review [Step-by-Step Guide](../guides/STEP-BY-STEP-GUIDE.md)
