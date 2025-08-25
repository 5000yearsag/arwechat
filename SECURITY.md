# 安全配置指南

## 环境变量配置

为了保护敏感信息，本项目使用环境变量来管理密钥和配置。

### 后端配置 (ar-platform)

1. **复制环境变量模板**:
   ```bash
   cd arwechat-backup/ar-platform
   cp .env.example .env
   ```

2. **设置必需的环境变量**:
   ```bash
   # 阿里云OSS配置
   export ALIYUN_OSS_ENDPOINT=oss-cn-beijing-internal.aliyuncs.com
   export ALIYUN_ACCESS_KEY_ID=你的AccessKeyId
   export ALIYUN_ACCESS_KEY_SECRET=你的AccessKeySecret
   export ALIYUN_OSS_BUCKET_NAME=beijingxr
   export ALIYUN_OSS_URL_PREFIX=https://beijingxr.oss-cn-beijing.aliyuncs.com/
   ```

3. **或者在 .env 文件中设置**:
   ```properties
   ALIYUN_OSS_ENDPOINT=oss-cn-beijing-internal.aliyuncs.com
   ALIYUN_ACCESS_KEY_ID=你的AccessKeyId
   ALIYUN_ACCESS_KEY_SECRET=你的AccessKeySecret
   ALIYUN_OSS_BUCKET_NAME=beijingxr
   ALIYUN_OSS_URL_PREFIX=https://beijingxr.oss-cn-beijing.aliyuncs.com/
   ```

### 生产环境部署

在生产环境中，建议通过以下方式设置环境变量：

1. **Docker 容器**:
   ```bash
   docker run -e ALIYUN_ACCESS_KEY_ID=你的密钥 -e ALIYUN_ACCESS_KEY_SECRET=你的密钥 ...
   ```

2. **Kubernetes**:
   使用 ConfigMap 和 Secret 来管理配置和敏感信息

3. **系统环境变量**:
   在 `/etc/environment` 或服务器配置中设置

### 安全注意事项

- ❌ **永远不要**将密钥提交到版本控制系统
- ✅ **始终使用**环境变量或专门的密钥管理服务
- ✅ **定期轮换**访问密钥
- ✅ **使用最小权限原则**为密钥分配权限
- ✅ **监控**密钥的使用情况

### .gitignore 配置

确保以下文件被忽略：
```
.env
.env.local
.env.production
*.key
*.pem
```