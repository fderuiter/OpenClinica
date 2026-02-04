# Development Setup Guide

## Quick Start

Get the CRF Design Studio running locally in under 30 minutes.

---

## Prerequisites

### Required Software

```bash
# 1. Node.js 20+ (LTS)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# Verify
node --version  # v20.x.x
npm --version   # 10.x.x

# 2. Git
sudo apt-get install git

# 3. Docker & Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 4. VS Code (recommended)
sudo snap install code --classic
```

### Recommended VS Code Extensions

```bash
code --install-extension dbaeumer.vscode-eslint
code --install-extension esbenp.prettier-vscode
code --install-extension prisma.prisma
code --install-extension ms-playwright.playwright
code --install-extension bradlc.vscode-tailwindcss
```

---

## Step 1: Clone Repository

```bash
# Clone the repository
git clone https://github.com/your-org/crf-design-studio.git
cd crf-design-studio

# Create your feature branch
git checkout -b feature/my-feature
```

---

## Step 2: Install Dependencies

```bash
# Install root dependencies
npm install

# Install workspace dependencies (monorepo)
npm install --workspaces

# Or manually install each package
cd packages/backend && npm install
cd ../frontend && npm install
cd ../..
```

---

## Step 3: Database Setup

### Option A: Docker (Recommended)

```bash
# Start PostgreSQL
docker-compose up -d

# Verify
docker ps
# Should show crf-postgres running on port 5432

# Test connection
docker exec -it crf-postgres psql -U postgres -d crf_design_studio -c "SELECT version();"
```

### Option B: Local PostgreSQL

```bash
# Install PostgreSQL
sudo apt-get install postgresql postgresql-contrib

# Create database
sudo -u postgres psql
CREATE DATABASE crf_design_studio;
CREATE USER crf_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE crf_design_studio TO crf_user;
\q
```

---

## Step 4: Environment Configuration

### Backend Environment

```bash
cd packages/backend

# Create .env file
cat > .env << 'EOF'
# Database
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/crf_design_studio"

# Application
PORT=3000
NODE_ENV=development

# JWT Secret
JWT_SECRET=your-secret-key-change-this-in-production

# CORS
CORS_ORIGIN=http://localhost:5173

# Git Repository (for CRF versioning)
CRF_REPO_PATH=/path/to/crf-repository
EOF
```

### Frontend Environment

```bash
cd packages/frontend

# Create .env file
cat > .env << 'EOF'
VITE_API_URL=http://localhost:3000/api
VITE_APP_NAME=CRF Design Studio
EOF
```

---

## Step 5: Database Migration

```bash
cd packages/backend

# Generate Prisma client
npx prisma generate

# Run migrations
npx prisma migrate dev --name init

# Seed database (optional)
npx prisma db seed
```

---

## Step 6: Start Development Servers

### Option A: Start All (Recommended)

```bash
# From project root
npm run dev

# This starts:
# - Backend on http://localhost:3000
# - Frontend on http://localhost:5173
```

### Option B: Start Individually

**Terminal 1 - Backend:**
```bash
cd packages/backend
npm run start:dev

# Server running at http://localhost:3000
# Swagger docs at http://localhost:3000/api-docs
```

**Terminal 2 - Frontend:**
```bash
cd packages/frontend
npm run dev

# App running at http://localhost:5173
```

---

## Step 7: Verify Installation

### Test Backend

```bash
# Health check
curl http://localhost:3000/health

# List CRFs (should be empty)
curl http://localhost:3000/api/crfs

# Create test CRF
curl -X POST http://localhost:3000/api/crfs \
  -H "Content-Type: application/json" \
  -d '{"name":"Test CRF","description":"Test"}'
```

### Test Frontend

1. Open browser to `http://localhost:5173`
2. Should see "CRF Design Studio" title
3. Should see "Create New CRF" button
4. Should see empty CRF list or test CRF created above

---

## IDE Configuration

### VS Code Settings

**File: `.vscode/settings.json`**
```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.tsdk": "node_modules/typescript/lib",
  "typescript.enablePromptUseWorkspaceTsdk": true,
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescriptreact]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "eslint.workingDirectories": [
    "./packages/backend",
    "./packages/frontend"
  ]
}
```

### VS Code Debug Configuration

**File: `.vscode/launch.json`**
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Debug Backend",
      "type": "node",
      "request": "launch",
      "runtimeExecutable": "npm",
      "runtimeArgs": ["run", "start:debug"],
      "cwd": "${workspaceFolder}/packages/backend",
      "console": "integratedTerminal"
    },
    {
      "name": "Debug Frontend",
      "type": "chrome",
      "request": "launch",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/packages/frontend/src"
    }
  ]
}
```

---

## Common Tasks

### Database Management

```bash
# Reset database
cd packages/backend
npx prisma migrate reset

# Create new migration
npx prisma migrate dev --name add_new_field

# Open Prisma Studio (DB GUI)
npx prisma studio
```

### Code Quality

```bash
# Lint all packages
npm run lint

# Fix lint errors
npm run lint:fix

# Format code
npm run format

# Type check
npm run type-check
```

### Testing

```bash
# Run all tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run E2E tests
npm run test:e2e

# Generate coverage
npm run test:coverage
```

### Building

```bash
# Build all packages
npm run build

# Build specific package
npm run build -w backend
npm run build -w frontend

# Preview production build
cd packages/frontend
npm run preview
```

---

## Git Workflow

### Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Feature
git commit -m "feat(designer): add drag-and-drop for fields"

# Bug fix
git commit -m "fix(api): correct validation error handling"

# Documentation
git commit -m "docs: update setup guide"

# Refactor
git commit -m "refactor(backend): extract CRF service logic"
```

### Pre-commit Hooks

**File: `.husky/pre-commit`**
```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

npm run lint
npm run type-check
npm run test
```

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 3000
lsof -i :3000

# Kill process
kill -9 <PID>

# Or change port in .env
PORT=3001
```

### Database Connection Failed

```bash
# Check Docker container
docker ps
docker logs crf-postgres

# Restart container
docker-compose restart postgres

# Check connection string in .env
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/crf_design_studio"
```

### Prisma Client Not Generated

```bash
cd packages/backend
npx prisma generate
npm run build
```

### Module Not Found

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
rm -rf packages/*/node_modules
npm install
```

### TypeScript Errors

```bash
# Restart TS server in VS Code
# Cmd/Ctrl + Shift + P → "TypeScript: Restart TS Server"

# Check tsconfig.json paths
# Verify imports use correct paths
```

---

## Development Workflow

### Daily Workflow

```bash
# 1. Pull latest changes
git checkout main
git pull origin main

# 2. Create feature branch
git checkout -b feature/my-feature

# 3. Start servers
npm run dev

# 4. Make changes
# ... edit code ...

# 5. Test changes
npm run test
npm run lint

# 6. Commit
git add .
git commit -m "feat: add new feature"

# 7. Push
git push origin feature/my-feature

# 8. Create Pull Request on GitHub
```

### Code Review Checklist

Before submitting PR:
- [ ] Tests pass
- [ ] Linter passes
- [ ] Type checks pass
- [ ] Code is formatted
- [ ] Documentation updated
- [ ] No console.log statements
- [ ] Commit messages follow convention

---

## Useful Commands

```bash
# View all available scripts
npm run

# Clean build artifacts
npm run clean

# Reset everything
npm run clean && npm install && npm run build

# Check for outdated dependencies
npm outdated

# Update dependencies
npm update

# Audit security
npm audit
npm audit fix
```

---

## Performance Tips

### Speed up npm install

```bash
# Use npm ci for clean installs
npm ci

# Use --prefer-offline
npm install --prefer-offline
```

### Speed up TypeScript

**File: `tsconfig.json`**
```json
{
  "compilerOptions": {
    "incremental": true,
    "skipLibCheck": true
  }
}
```

### Use npm workspaces efficiently

```bash
# Run command in specific workspace
npm run dev -w backend

# Run command in all workspaces
npm run test --workspaces

# Install dependency in specific workspace
npm install react -w frontend
```

---

## Next Steps

1. ✅ Setup complete
2. Read [Step-by-Step Guide](STEP-BY-STEP-GUIDE.md)
3. Review [Architecture Patterns](../development/ARCHITECTURE-PATTERNS.md)
4. Check [Testing Strategy](TESTING-STRATEGY.md)
5. Start building features!

---

## Getting Help

- **Documentation**: Browse `/docs` folder
- **Issues**: GitHub Issues
- **Questions**: Team chat
- **Code Review**: Submit PR

---

**Setup complete! Start building amazing CRFs! 🚀**
