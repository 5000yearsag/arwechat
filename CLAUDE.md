# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

This is a full-stack AR application with three main components:

1. **WeChat Mini Program** (root directory) - AR-enabled mini program using WeChat XR framework
2. **Web Frontend** (`arweb/`) - React/Ant Design Pro admin dashboard 
3. **Backend API** (`ar-platform/`) - Spring Boot REST API with MySQL database

## Development Commands

### Frontend (Web Admin)
```bash
cd arweb/
npm run start:dev          # Start development server (localhost:8000)
npm run build              # Production build
npm run lint               # Run linting
npm run test               # Run tests
npm run prettier           # Format code
```

### Backend (Spring Boot)
```bash
cd ar-platform/
mvn spring-boot:run        # Start development server (localhost:9091)
mvn clean package          # Build JAR
mvn clean install         # Build and install dependencies
```

### Full Stack Development
```bash
# From root directory (requires Node.js 18.x)
npm run start:dev:fullstack  # Start both frontend and backend
export PATH="/opt/homebrew/opt/node@18/bin:$PATH"  # Switch to Node 18 on macOS
```

## Database Configuration

- **Development**: Remote MySQL at 123.57.231.35:3306/ardb
- **Production**: Local MySQL at 127.0.0.1:3306/yaoculture
- **Credentials**: Username `ardb`, Password `LtHenC6MFTRt6GPe`

## API Proxy Configuration

Frontend proxy settings in `arweb/config/proxy.ts`:
- **Development**: Proxies to 123.57.231.35:8081
- **Test**: Proxies to https://testar.shenyuantek.com/

## File Upload Limits

All components configured for 100MB file uploads:
- Frontend validation in beforeUpload functions
- Backend Spring Boot maxFileSize: 104857600 bytes
- Applies to GLB models, audio files, and images

## Key Technologies

- **Mini Program**: WeChat XR framework, AR tracking components
- **Frontend**: React 18, Ant Design Pro, UmiJS Max
- **Backend**: Spring Boot 2.7.14, MyBatis Plus, MySQL 8
- **File Storage**: Aliyun OSS integration
- **Authentication**: Spring Security with custom providers

## Mini Program Structure

- `components/ar-*`: AR-related components (tracker, plane detection)
- `pages/scene-ar-*`: AR scene pages with marker tracking
- `components/xr-*`: XR framework components
- WeChat AppID: `wxa7aaf8df3c07a824`

## Recent Changes

- Multi-resource functionality enabled with audio upload support
- Removed GLB model dependency requirements
- Unified 100MB file upload limits across all components
- Local full-stack development environment configured