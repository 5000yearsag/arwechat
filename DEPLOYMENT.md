# ARWeb 部署文档

## 服务器信息
- 服务器别名: `ali_lanyu`
- 域名: `app.lanyuxr.com`
- SSL证书: 自签名证书

## 部署架构

### 1. 前端部署 (ARWeb)
- **部署目录**: `/home/web/`
- **访问方式**: 
  - HTTPS (生产): `https://app.lanyuxr.com` (端口443)
  - HTTP测试: `http://app.lanyuxr.com:7891` (端口7891，开发用)
- **构建文件**: `/home/arweb-dist.zip` (最新构建包)

### 2. 后端部署 (Spring Boot API)
- **部署目录**: `/home/back/`
- **JAR文件**: `vr-platform.jar`
- **运行端口**: 8081 (内部端口，通过nginx代理)
- **Java路径**: `/home/jdk/jdk-11.0.22/bin/java`
- **配置文件**: `/home/back/application-prod.yml`
- **日志文件**: `/home/back/platform.log`

### 3. Web服务器 (Nginx)
- **安装目录**: `/usr/local/nginx/`
- **配置文件**: `/usr/local/nginx/conf/nginx.conf`
- **SSL证书目录**: `/usr/local/nginx/cert/`

## Nginx配置详情

### 生产环境配置 (HTTPS - 端口443)
```nginx
server {
    listen       443 ssl;
    server_name  app.lanyuxr.com;
    
    ssl_certificate      /usr/local/nginx/cert/app.lanyuxr.com_selfsigned.pem;
    ssl_certificate_key  /usr/local/nginx/cert/app.lanyuxr.com_selfsigned.key;
    
    location / {
        root /home/web/;
        index index.html index.htm;
    }
    
    location ^~ /api/ {
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_buffering off;
        proxy_pass http://localhost:8081;
    }
    
    location /file/ {
        alias /usr/local/www/yaoculture/file/;
    }
}
```

### 开发/测试环境配置 (HTTP - 端口7891)
```nginx
server {
    listen 7891;
    client_max_body_size 300M;
    
    location / {
        root /root/web/;  # 注意：目前不存在，应使用 /home/web/
        index index.html index.htm;
    }
    
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 200m;
    }
}
```

## 构建环境要求

### Node.js版本要求
- **必需版本**: Node.js 18.x
- **推荐版本**: v18.20.8 (npm v10.8.2)
- **不兼容版本**: Node.js 24.x (存在`http_parser`模块兼容性问题)

### 环境设置
```bash
# 安装nvm (如果没有)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.4/install.sh | bash

# 加载nvm
source ~/.nvm/nvm.sh

# 安装并使用Node.js 18
nvm install 18
nvm use 18

# 验证版本
node --version  # 应显示 v18.20.8
npm --version   # 应显示 10.8.2
```

### 依赖安装
```bash
# 使用legacy-peer-deps解决依赖兼容性问题
npm install --legacy-peer-deps --ignore-scripts
```

## 已知问题和解决方案

### 1. 前后端参数不匹配问题 (已修复)
**问题**: 新建合集时出现"request failed"错误

**原因**: 前端发送的参数包含后端不支持的字段：
- `templateId` - 后端AddCollectionRequest不支持
- `collectionType` - 后端AddCollectionRequest不支持  
- `loadType` - 后端AddCollectionRequest不支持

**解决方案**: 
- 暂时注释掉前端UI中的相关字段选择
- 移除API调用中的不支持参数
- 保留所有代码以备将来恢复

**修复时间**: 2025-08-23 03:20

### 2. 模板功能临时隐藏
**状态**: 已隐藏合集模板相关UI和逻辑
**原因**: 简化功能，专注核心合集管理
**位置**: `AddOrEditCollectionModal.tsx` 第317-330行

## 部署流程

### 前端部署 (ARWeb)

1. **构建前端项目**
   ```bash
   # 确保使用Node.js 18
   source ~/.nvm/nvm.sh && nvm use 18
   
   # 在arweb目录中
   cd arweb/
   
   # 安装依赖 (如果需要)
   npm install --legacy-peer-deps
   
   # 构建项目
   npm run build
   
   # 打包dist目录
   cd dist/
   tar -czf ../arweb-dist.tar.gz *
   # 或使用zip (如果可用): zip -r ../arweb-dist.zip *
   ```

2. **上传到服务器**
   ```bash
   # 上传构建包到服务器 (支持tar.gz和zip格式)
   scp arweb-dist.tar.gz ali_lanyu:/home/arweb-dist.tar.gz
   # 或 scp arweb-dist.zip ali_lanyu:/home/arweb-dist.zip
   ```

3. **部署到生产目录**
   ```bash
   # SSH到服务器并一键部署
   ssh ali_lanyu "cd /home && \
     echo 'Creating backup...' && \
     cp -r web web_backup_\$(date +%Y%m%d_%H%M%S) && \
     echo 'Deploying new build...' && \
     rm -rf web/* && \
     tar -xzf arweb-dist.tar.gz -C web/ && \
     echo 'Setting permissions...' && \
     chown -R root:root web/ && \
     chmod -R 644 web/* && \
     find web/ -type d -exec chmod 755 {} \; && \
     echo 'Deployment completed!'"
   
   # 如果使用zip格式:
   # unzip arweb-dist.zip -d web/
   ```

4. **重载Nginx配置** (如有修改)
   ```bash
   /usr/local/nginx/sbin/nginx -t  # 测试配置
   /usr/local/nginx/sbin/nginx -s reload  # 重载配置
   ```

### 后端部署 (Spring Boot)

1. **构建后端项目**
   ```bash
   # 在本地开发环境
   cd ar-platform/
   mvn clean package -DskipTests
   ```

2. **上传JAR文件**
   ```bash
   # 上传到服务器
   scp target/vr-platform.jar ali_lanyu:/home/back/
   ```

3. **部署新版本**
   ```bash
   # SSH到服务器
   ssh ali_lanyu
   cd /home/back/
   
   # 备份当前版本
   cp vr-platform.jar vr-platform-backup-$(date +%Y%m%d_%H%M%S).jar
   
   # 运行部署脚本
   ./start.sh
   ```

### start.sh 脚本说明
```bash
#!/bin/bash
JAR_FILE="vr-platform.jar"

# 停止现有进程
if pgrep -f "$JAR_FILE" > /dev/null; then
    echo "Killing existing $JAR_FILE process..."
    pkill -f "$JAR_FILE"
fi

sleep 2

# 启动新进程
echo "Starting $JAR_FILE with external config..."
nohup /home/jdk/jdk-11.0.22/bin/java -jar "$JAR_FILE" \
    -Xmx1500M -Xms1024M \
    --spring.profiles.active=prod \
    --spring.config.location=classpath:/application.yml,classpath:/application-prod.yml,file:./application-prod.yml \
    --server.port=8081 > platform.log 2>&1 &

echo "Application started. PID: $!"
```

## 服务管理

### 检查服务状态
```bash
# 检查nginx状态
systemctl status nginx

# 检查Java应用进程
ps aux | grep java

# 查看应用日志
tail -f /home/back/platform.log

# 查看nginx日志
tail -f /usr/local/nginx/logs/access.log
tail -f /usr/local/nginx/logs/error.log
```

### 重启服务
```bash
# 重启nginx
systemctl restart nginx

# 重启Java应用
cd /home/back/
./start.sh
```

## 重要目录和文件

### 目录结构
```
/home/
├── web/                    # 前端部署目录 (生产)
├── back/                   # 后端部署目录
│   ├── vr-platform.jar    # 后端JAR文件
│   ├── application-prod.yml # 生产配置
│   ├── start.sh           # 启动脚本
│   └── platform.log       # 应用日志
├── jdk/                   # Java环境
└── arweb-dist.zip         # 前端构建包

/usr/local/nginx/
├── conf/nginx.conf        # Nginx主配置
├── cert/                  # SSL证书目录
├── logs/                  # Nginx日志
└── sbin/nginx            # Nginx执行文件

/usr/local/www/yaoculture/file/  # 文件存储目录
```

### 配置文件
- Nginx配置: `/usr/local/nginx/conf/nginx.conf`
- 后端配置: `/home/back/application-prod.yml`
- SSL证书: `/usr/local/nginx/cert/app.lanyuxr.com_selfsigned.pem`
- SSL私钥: `/usr/local/nginx/cert/app.lanyuxr.com_selfsigned.key`

## 注意事项

1. **端口配置**
   - 443: HTTPS生产环境
   - 7891: HTTP测试环境  
   - 8081: 后端API内部端口
   - 8011: 文件服务端口

2. **文件大小限制**
   - Nginx: `client_max_body_size 300M`
   - 后端: 100MB (在application配置中)

3. **SSL证书**
   - 当前使用自签名证书
   - 生产环境建议使用正式SSL证书

4. **权限管理**
   - 所有服务以root用户运行
   - 文件权限: 644 (文件) / 755 (目录)

5. **备份策略**
   - 部署前自动备份当前版本
   - 备份文件命名格式: `*-backup-YYYYMMDD_HHMMSS.*`

## 部署历史

### 最近部署记录
| 时间 | 版本 | 关键文件 | 变更说明 | 备份位置 |
|------|------|----------|----------|----------|
| 2025-08-23 03:20 | v1.2 | `p__Collection__index.26623d4a.async.js` | 修复新建合集参数不匹配问题 | `web_backup_20250823_032032_before_fix` |
| 2025-08-23 02:34 | v1.1 | `p__Collection__index.2b753385.async.js` | 隐藏合集模板功能，使用Node.js 18构建 | `web_backup_20250822_172211` |
| 2025-07-30 16:18 | v1.0 | `p__Collection__index.69203f70.async.js` | 初始版本 | - |

### 重要变更记录
- **2025-08-23**: 解决前后端API参数不匹配问题，新建合集功能恢复正常
- **2025-08-23**: 统一Node.js 18构建环境，解决构建兼容性问题
- **2025-08-22**: 暂时隐藏合集模板选择功能

## 故障排除

### 常见问题
1. **前端页面404**: 检查 `/home/web/` 目录是否存在文件
2. **API请求失败**: 检查后端服务是否运行 (`ps aux | grep java`)
3. **SSL证书错误**: 检查证书文件路径和权限
4. **文件上传失败**: 检查文件大小限制配置

### 日志查看
```bash
# 应用日志
tail -f /home/back/platform.log

# Nginx访问日志
tail -f /usr/local/nginx/logs/access.log

# Nginx错误日志
tail -f /usr/local/nginx/logs/error.log
```

## 完整部署流程

### 后端部署 (Spring Boot)

#### 1. 编译打包
```bash
cd ar-platform
mvn clean package -DskipTests
```

#### 2. 备份和部署
```bash
# SSH到服务器
ssh ali_lanyu

# 备份当前JAR文件
cd /home/back
cp vr-platform.jar vr-platform-backup-$(date +%Y%m%d_%H%M%S).jar

# 上传新JAR文件 (在本地执行)
scp ar-platform/target/vr-platform.jar ali_lanyu:/home/back/vr-platform-new.jar

# 替换JAR文件
cd /home/back
mv vr-platform-new.jar vr-platform.jar
```

#### 3. 配置文件检查
```bash
# 确保配置文件存在且命名正确
cd /home/back
ls -la application-pro.yml

# 如果配置文件名为 application-prod.yml，需要重命名
mv application-prod.yml application-pro.yml
```

#### 4. 启动服务
```bash
# 方法1: 使用启动脚本
./start.sh

# 方法2: 直接启动 (推荐用于调试)
pkill -f vr-platform.jar
nohup /home/jdk/jdk-11.0.22/bin/java -jar vr-platform.jar \
    --spring.profiles.active=pro \
    --server.port=8081 > platform.log 2>&1 &

# 验证启动
ps aux | grep vr-platform
tail -20 platform.log
```

### 前端部署 (React)

#### 1. 环境准备
```bash
# 使用Node.js 18 (必需)
export NVM_DIR="$HOME/.nvm"
source "$NVM_DIR/nvm.sh"
nvm use 18
```

#### 2. 编译构建
```bash
cd arweb
npm run build
```

#### 3. 备份和部署
```bash
# 打包构建文件
tar -czf dist-new.tar.gz -C dist .

# 上传到服务器
scp dist-new.tar.gz ali_lanyu:/tmp/

# SSH到服务器并部署
ssh ali_lanyu
cd /home/web

# 备份当前文件
mkdir -p backup-$(date +%Y%m%d_%H%M%S)
cp -r * backup-$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || true

# 清理旧文件并部署新版本
rm -f *.js *.css *.html
tar -xzf /tmp/dist-new.tar.gz
```

### 部署验证

#### 1. 服务状态检查
```bash
# 检查后端进程
ps aux | grep vr-platform

# 检查端口监听
netstat -tlnp | grep 8081

# 检查前端文件
ls -la /home/web/
```

#### 2. 功能测试
```bash
# 测试前端访问
curl -I https://app.lanyuxr.com

# 测试后端API
curl -I "http://app.lanyuxr.com:8081/api/guest/statistic?collectionUuid=test&type=pvCount"

# 查看应用日志
tail -20 /home/back/platform.log
```

## 重要注意事项

### 配置文件匹配
- **JAR内部配置**: `application-pro.yml`
- **外部配置文件**: 必须命名为 `application-pro.yml`
- **启动脚本参数**: `--spring.profiles.active=pro`

### Node.js版本限制
- **必须使用**: Node.js 18.x
- **不能使用**: Node.js 24.x (会导致构建失败)

### 文件权限
```bash
# 确保JAR文件有执行权限
chmod +x /home/back/vr-platform.jar

# 确保启动脚本有执行权限
chmod +x /home/back/start.sh
```

### 服务启动验证
```bash
# 等待服务完全启动 (约30-45秒)
tail -f /home/back/platform.log | grep "vr-platform start ok"

# 确认端口监听
ss -tlnp | grep 8081
```