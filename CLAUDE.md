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
# 设置环境变量（首次运行前）
source ../setup-env.sh    # 或手动设置环境变量
mvn spring-boot:run        # Start development server (localhost:9091)
mvn clean package          # Build JAR
mvn clean install         # Build and install dependencies
```

### Production Deployment
```bash
# 设置生产环境变量（部署前必需）
source ./setup-env.sh                 # 或手动设置环境变量

# Automated backend deployment (recommended)
./deploy-backend.sh                    # Deploy with timestamp version
./deploy-backend.sh v1.4-loadtype     # Deploy with custom version tag

# Automated frontend deployment (recommended)
./deploy-frontend.sh                   # Build and deploy with timestamp version
./deploy-frontend.sh v1.5-ui           # Deploy with custom version tag
./deploy-frontend.sh --use-existing    # Use existing dist build

# Full stack deployment
./deploy-backend.sh v1.5-fullstack && ./deploy-frontend.sh v1.5-fullstack

# Manual deployment commands
cd ar-platform/
mvn clean package -DskipTests         # Build JAR
scp target/vr-platform.jar ali_lanyu:/home/back/
ssh ali_lanyu "cd /home/back && ./start.sh"  # Deploy and restart
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

## Environment Variables Configuration

### Required Environment Variables

Before running the backend service, set the following environment variables:

```bash
# 阿里云OSS配置（必需）
export ALIYUN_ACCESS_KEY_ID='你的AccessKeyId'
export ALIYUN_ACCESS_KEY_SECRET='你的AccessKeySecret'

# 可选配置（有默认值）
export ALIYUN_OSS_ENDPOINT='oss-cn-beijing-internal.aliyuncs.com'
export ALIYUN_OSS_BUCKET_NAME='beijingxr'
export ALIYUN_OSS_URL_PREFIX='https://beijingxr.oss-cn-beijing.aliyuncs.com/'
```

### Environment Setup Methods

1. **Using .env file** (recommended):
   ```bash
   cp arwechat-backup/ar-platform/.env.example arwechat-backup/ar-platform/.env
   # Edit .env file with actual values
   source ./setup-env.sh
   ```

2. **Manual export**:
   ```bash
   export ALIYUN_ACCESS_KEY_ID='LTAI5tGryWYdwStSk6StoQPu'
   export ALIYUN_ACCESS_KEY_SECRET='TFQEbHHuce10Rr1XMXZI5h7bwsr89R'
   ```

3. **System environment** (production):
   Add to `/etc/environment` or container configuration

⚠️ **Security Note**: Never commit actual credentials to version control. See `SECURITY.md` for best practices.

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
- WeChat AppID: `wx360d6d845e60562e`

## Recent Changes

- **LoadType Functionality**: Complete support for 普通加载 (loadType: 0) vs 分段加载 (loadType: 1)
- **Automated Deployment**: Created `deploy-backend.sh` script for one-click backend deployment
- **Multi-resource functionality**: Audio upload support with AR scene integration
- **Smart Backup System**: Automatic JAR file backup with timestamp and version tagging
- **Unified file upload limits**: 100MB across all components
- **Local full-stack development**: Complete development environment setup
- **CI/CD Configuration**: miniprogram-ci for automated WeChat Mini Program uploads

## LoadType Feature Support

The application now supports two loading modes for AR resources:

### Loading Types
- **普通加载 (loadType: 0)**: All AR resources load immediately when scene starts
- **分段加载 (loadType: 1)**: AR resources load with `defer="true"` attribute for staged loading

### Implementation Stack
- **Frontend (arweb)**: LoadType selection UI in collection management
- **Backend (ar-platform)**: Complete API support with database persistence
- **Mini Program**: XR framework integration with defer attribute support
- **Database**: `collection_info.load_type` field with proper indexing

## WeChat Mini Program CI Configuration

### Environment Setup
- **CI Tool**: miniprogram-ci v2.1.26
- **Upload Script**: `upload.js` with version control and progress tracking
- **Package Scripts**: `npm run upload`, `npm run preview`

### Usage Commands
```bash
# Upload to WeChat backend
npm run upload                           # Default version upload
node upload.js 1.0.1 "Version description"  # Custom version upload

# Generate preview QR code
npm run preview                          # Outputs preview.jpg
```

### Requirements
- **Private Key**: Download from WeChat Developer Platform → Development Settings
- **File Location**: `private.key` in project root directory
- **IP Whitelist**: Add server IP to WeChat platform whitelist
- **Project Config**: AppID `wx360d6d845e60562e` configured

### CI Features
- Automatic file compression and optimization
- ES6/ES7 compilation support
- Progress tracking and error handling
- Ignored files: node_modules, .git, upload scripts
- Support for development/trial/release environments

## Automated Deployment

### Complete Deployment Script System

The project includes comprehensive deployment automation for both frontend and backend:

### Backend Deployment Script (`deploy-backend.sh`)

#### Features
- **Automated Build**: Maven build with skip tests
- **Smart Backup**: Automatic JAR backup with timestamp/version naming
- **Service Management**: Graceful stop and restart of backend service
- **Health Validation**: Process, port, and API response verification
- **Error Handling**: Fail-fast with detailed error messages

#### Usage Examples
```bash
./deploy-backend.sh                    # Timestamp-based deployment
./deploy-backend.sh v1.4-loadtype     # Version-tagged deployment
./deploy-backend.sh hotfix-20250825   # Custom version deployment
```

#### Deployment Validation
- Java process status and resource usage
- Port 8081 listening verification
- API endpoint response testing (HTTP 200)
- Application startup log monitoring

### Frontend Deployment Script (`deploy-frontend.sh`)

#### Features
- **Environment Management**: Automatic Node.js 18 switching with npm config handling
- **Smart Build**: Automatic build or fallback to existing dist files
- **Smart Backup**: Automatic web directory backup with version naming
- **Safe Deployment**: Clean old files, extract new version, set correct permissions
- **Multi-Validation**: HTTPS/HTTP access testing, file integrity checks
- **Cleanup**: Automatic cleanup of temporary files

#### Usage Examples
```bash
./deploy-frontend.sh                   # Build and deploy with timestamp
./deploy-frontend.sh v1.5-ui           # Deploy with custom version tag
./deploy-frontend.sh --use-existing    # Use existing dist build
./deploy-frontend.sh v1.5 --use-existing  # Custom version with existing build
```

#### Deployment Validation
- File count and key file existence verification
- HTTPS production environment testing (port 443)
- HTTP test environment testing (port 7891)
- File permissions and ownership verification

### Full Stack Deployment
```bash
# Sequential deployment with same version tag
./deploy-backend.sh v1.5-release && ./deploy-frontend.sh v1.5-release

# Parallel deployment for independent components
./deploy-backend.sh v1.5-api &
./deploy-frontend.sh v1.5-ui &
wait
```