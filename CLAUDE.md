# Claude 项目记忆

## SSH连接信息
- **服务器别名**: ali_lanyu
- **连接命令**: `ssh ali_lanyu`
- **服务器IP**: 123.57.231.35
- **用途**: 阿里云服务器，运行MySQL数据库和生产环境

## 数据库配置
- **开发环境数据库**: 123.57.231.35:3306/ardb
- **用户名**: ardb
- **密码**: LtHenC6MFTRt6GPe
- **生产环境数据库**: 127.0.0.1:3306/yaoculture (服务器本地)

## 项目结构
- **前端项目**: /Users/luche/Documents/Miniprogram/arwechat/arweb
- **后端项目**: /Users/luche/Documents/Miniprogram/arwechat/ar-platform
- **Node.js版本**: 18.x (通过 `/opt/homebrew/opt/node@18/bin` 使用)

## 开发环境命令
- **启动前端**: `npm run start:dev`
- **启动全栈**: `npm run start:dev:fullstack` (同时启动前后端)
- **切换Node.js**: `export PATH="/opt/homebrew/opt/node@18/bin:$PATH"`

## API配置
- **开发环境代理**: http://localhost:9091/ (本地后端)
- **生产环境代理**: https://app.lanyuxr.com/
- **前端访问**: http://localhost:8000

## 文件上传限制
- **前端验证**: 100MB beforeUpload函数
- **后端限制**: 100MB (FileService.java + application.yml)
- **Spring Boot配置**: maxFileSize: 104857600 (100MB)

## 最近修改
1. 移除GLB模型依赖，音频上传组件始终显示
2. 启用多资源功能，每个资源都支持音频上传
3. 统一所有文件上传限制为100MB
4. 配置本地全栈开发环境