# 阿里云服务器 ali_lanyu 完整备份

## 📦 备份概览
- **备份时间**: 2025年8月7日 14:52
- **服务器**: ali_lanyu (47.116.172.28)
- **系统**: CentOS 7 (Core)
- **备份大小**: 22MB (压缩后)

## 📁 备份文件结构

```
ali_lanyu_server_backup_final/
├── backup_ali_lanyu_complete_20250807.tar.gz (22MB) - 完整备份压缩包
├── backup_data/                                      - 备份数据目录
│   ├── mysql/           - 数据库备份
│   │   ├── all_databases.sql (1.2MB) - 完整数据库导出
│   │   ├── database_list.txt          - 数据库列表
│   │   └── mysql_users.txt            - 用户权限
│   ├── nginx/           - Nginx配置
│   │   └── nginx_configs.tar.gz (9.7KB) - 配置和SSL证书
│   ├── system/          - 系统配置
│   │   └── system_configs.tar.gz (30KB) - 系统配置文件
│   ├── java_apps/       - Java应用
│   │   └── java_app_core.tar.gz (22MB) - 应用文件
│   ├── scripts/         - 重要脚本
│   │   ├── checkpy.sh         - 服务监控脚本
│   │   ├── ip.sh              - IP记录脚本
│   │   ├── start.sh           - 启动脚本
│   │   ├── restart.sh         - 重启脚本
│   │   ├── recover.sh         - 恢复脚本
│   │   ├── docker-compose.yml - Docker配置
│   │   └── auto-del-7-days-ago-log.sh - 日志清理
│   ├── backup_summary.txt     - 备份摘要
│   └── system_info.txt        - 系统信息
├── backup_server_ali_lanyu.sh   - 完整备份脚本
└── backup_server_quick.sh       - 快速备份脚本
```

## 🎯 备份内容详情

### 1. MySQL数据库 ✅
- **完整数据库导出**: 包含所有数据库和表结构
- **数据库**: ardb, information_schema, mysql, performance_schema, sys
- **用户账户**: root, ardb 等用户权限信息
- **文件大小**: 1.2MB

### 2. 系统配置 ✅
- **用户管理**: /etc/passwd, /etc/group, /etc/shadow
- **网络配置**: /etc/hosts
- **MySQL配置**: /etc/my.cnf
- **SSH配置**: /etc/ssh/ 完整配置
- **定时任务**: crontab配置
- **文件大小**: 30KB

### 3. Nginx服务 ✅
- **主配置**: nginx.conf (3个虚拟主机)
  - 端口443: HTTPS (app.lanyuxr.com)
  - 端口7891: HTTP网站
  - 端口8011: 文件服务
- **SSL证书**: app.lanyuxr.com.pem, app.lanyuxr.com.key
- **所有配置文件**: mime.types, fastcgi等
- **文件大小**: 9.7KB

### 4. Java应用 ✅
- **主应用**: vr-platform.jar (106MB)
- **备份应用**: bak1vr-platform.jar, bakvr-platform.jar
- **启动脚本**: start.sh
- **压缩后大小**: 22MB

### 5. 运维脚本 ✅
- **服务监控**: checkpy.sh (每分钟检查58080端口)
- **IP记录**: ip.sh (记录服务器IP到ehcache)
- **日志清理**: auto-del-7-days-ago-log.sh (清理7天前日志)
- **服务控制**: start.sh, restart.sh, recover.sh
- **Docker配置**: docker-compose.yml

## 🔧 服务架构

### 端口配置
- **443**: HTTPS网站 (app.lanyuxr.com) + API代理 → 8081
- **7891**: HTTP网站 + API代理 → 8081
- **8011**: 文件服务器 (/usr/local/www/yaoculture/file/)
- **8081**: Java后端应用 (vr-platform.jar)
- **3306**: MySQL数据库
- **58080**: Python服务 (监控检查)

### 关键进程
```bash
# Java应用
/home/jdk/jdk-11.0.22/bin/java -jar vr-platform.jar -Xmx1500M -Xms1024M --spring.profiles.active=prod --server.port=8081

# MySQL数据库
/www/server/mysql/bin/mysqld --datadir=/www/server/data --port=3306

# Nginx Web服务器
nginx: master process nginx
```

### 定时任务
```bash
* * * * * /bin/sh /data/checkpy.sh      # 每分钟检查Python服务
* * * * * /bin/sh /data/ip.sh           # 每分钟记录IP
10 0 * * * /data/auto-del-7-days-ago-log.sh  # 每天清理日志
```

## 🚀 迁移指南

### 1. 环境准备
新服务器需要安装：
- CentOS 7
- Java 11 (OpenJDK 11.0.22)
- MySQL 8.0.24
- Nginx (编译版本)
- Docker (可选)

### 2. 恢复步骤
1. **解压备份**: `tar -xzf backup_ali_lanyu_complete_20250807.tar.gz`
2. **恢复系统配置**: 解压 system_configs.tar.gz
3. **恢复数据库**: `mysql -u root -p < mysql/all_databases.sql`
4. **恢复Nginx**: 解压 nginx_configs.tar.gz 到 /usr/local/nginx/
5. **部署Java应用**: 解压 java_app_core.tar.gz 到 /home/back/
6. **恢复脚本**: 复制所有脚本到 /data/ 目录
7. **设置定时任务**: 导入 crontab 配置
8. **启动服务**: MySQL → Nginx → Java应用

### 3. 验证检查
- [ ] 检查端口监听：443, 7891, 8011, 8081, 3306
- [ ] 验证网站访问：https://app.lanyuxr.com
- [ ] 测试API接口：/api/ 路由是否正常
- [ ] 确认数据库连接和数据完整性
- [ ] 检查定时任务是否正常运行

## ⚠️ 重要注意事项

1. **数据库密码**: root密码为 `4305865740bf490f`
2. **SSL证书**: 已包含app.lanyuxr.com的有效证书
3. **文件权限**: 注意恢复后设置正确的文件权限
4. **网络配置**: 新服务器IP地址需要更新DNS解析
5. **防火墙**: 确保开放必要端口
6. **备份验证**: 建议在测试环境先验证备份完整性

## 📞 备份信息
- **备份脚本**: 可重复运行进行增量备份
- **备份类型**: 完整配置 + 数据库 + 核心应用
- **遗漏内容**: 大型网站文件、JDK安装包、历史日志
- **备份质量**: ✅ 生产就绪

---
*备份生成时间: 2025-08-07 14:52:54 CST*  
*备份工具: 自定义Shell脚本*  
*服务器: ali_lanyu (47.116.172.28)*