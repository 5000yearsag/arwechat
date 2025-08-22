# AR Platform 部署指南

## 服务器信息

### 基本配置
- **服务器**: ali_lanyu
- **连接方式**: `ssh ali_lanyu`
- **操作系统**: CentOS Linux
- **Java版本**: JDK 11.0.22 (路径: `/home/jdk/jdk-11.0.22/bin/java`)

### 应用部署路径
- **部署目录**: `/home/back/`
- **应用JAR**: `vr-platform.jar`
- **配置文件**: `application-prod.yml`
- **启动脚本**: `start.sh`
- **日志文件**: `platform.log`

## 数据库配置

### 连接信息
- **数据库**: MySQL
- **数据库名**: `ardb`
- **用户名**: `ardb`
- **密码**: `LtHenC6MFTRt6GPe`
- **连接URL**: `jdbc:mysql://127.0.0.1:3306/ardb`

### 统计相关表结构
```sql
-- 访问统计表
CREATE TABLE `access_statistics` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
  `open_id` varchar(64) DEFAULT NULL COMMENT '用户openId',
  `statistic_type` varchar(20) NOT NULL COMMENT '统计类型',
  `user_ip` varchar(45) DEFAULT NULL COMMENT '用户IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_collection_type_time` (`collection_uuid`, `statistic_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问统计表';

-- 用户访问历史表
CREATE TABLE `user_history` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `open_id` varchar(64) NOT NULL COMMENT '用户openId',
  `app_id` varchar(64) NOT NULL COMMENT '小程序appId',
  `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
  `collection_name` varchar(100) DEFAULT NULL COMMENT '合集名称',
  `access_count` int(11) DEFAULT 1 COMMENT '访问次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_collection` (`open_id`, `collection_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户访问历史表';
```

## 应用配置

### 生产环境配置文件 (application-prod.yml)
```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ardb
    password: LtHenC6MFTRt6GPe
    url: jdbc:mysql://127.0.0.1:3306/ardb?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true

file:
  env: release
  server:
    ip: https://app.lanyuxr.com/
    path: /usr/local/www/yaoculture/file/

wx:
  request:
    url: https://app.lanyuxr.com/api/guest/getAllSceneByCollection

dromara:
  x-file-storage:
    default-platform: aliyun-oss-1
    aliyun-oss:
      - platform: aliyun-oss-1
        enable-storage: true
        access-key: YOUR_ALIYUN_ACCESS_KEY
        secret-key: YOUR_ALIYUN_SECRET_KEY
        end-point: oss-cn-shanghai-internal.aliyuncs.com
        bucket-name: lanyuar
        domain: https://lanyuar.oss-cn-shanghai.aliyuncs.com/
        base-path: gao/
```

### 微信小程序配置 (内置在JAR中)
```yaml
wx:
  miniapp:
    configs:
      - appid: 'wxe7b4faed5d067dd6'
        secret: '49168bb05bf2a42f382c59191e0d9b74'
        token: 'default_token'
        aesKey: 'abcdefghijklmnopqrstuvwxyz1234567890123'
        msgDataFormat: 'JSON'
```

## 部署流程

### 1. 构建项目
```bash
# 在本地项目目录
cd /Users/luche/Documents/Miniprogram/arwechat/ar-platform
mvn clean package -DskipTests
```

### 2. 上传JAR包
```bash
scp target/vr-platform.jar ali_lanyu:/home/back/vr-platform-new.jar
```

### 3. 备份和替换
```bash
ssh ali_lanyu "cd /home/back && cp vr-platform.jar vr-platform-backup-$(date +%Y%m%d_%H%M%S).jar"
ssh ali_lanyu "cd /home/back && mv vr-platform-new.jar vr-platform.jar"
```

### 4. 停止旧进程
```bash
ssh ali_lanyu "pkill -f vr-platform.jar"
```

### 5. 启动应用
```bash
ssh ali_lanyu "cd /home/back && nohup /home/jdk/jdk-11.0.22/bin/java -jar vr-platform.jar -Xmx1500M -Xms1024M --spring.profiles.active=prod --spring.config.location=file:./application-prod.yml --server.port=8081 > platform.log 2>&1 &"
```

### 6. 验证启动
```bash
# 检查进程
ssh ali_lanyu "ps aux | grep vr-platform.jar | grep -v grep"

# 检查日志
ssh ali_lanyu "cd /home/back && tail -f platform.log"

# 验证启动成功的标志
grep "vr-platform start ok" platform.log
```

## API接口

### 统计相关接口

1. **访问统计记录**
   - URL: `GET /api/guest/statistic`
   - 参数: `collectionUuid`, `type`, `openId`(可选)
   - 支持的type: `pvCount`, `click1Count`, `click2Count`, `click3Count`, `click4Count`, `click5Count`

2. **获取微信openId**
   - URL: `GET /api/guest/openId`
   - 参数: `code`, `appId`
   - 说明: 当微信服务不可用时返回模拟openId

3. **记录访问历史**
   - URL: `GET /api/guest/historyRecord`
   - 参数: `openId`, `appId`, `collectionUuid`

4. **获取访问历史**
   - URL: `GET /api/guest/history`
   - 参数: `openId`

### 测试命令
```bash
# 在服务器上测试
curl -X GET 'http://localhost:8081/api/guest/statistic?collectionUuid=test&type=pvCount&openId=user001'
curl -X GET 'http://localhost:8081/api/guest/openId?code=test_code&appId=test_app'
curl -X GET 'http://localhost:8081/api/guest/historyRecord?openId=user001&appId=test_app&collectionUuid=test'
curl -X GET 'http://localhost:8081/api/guest/history?openId=user001'
```

## 数据库操作

### 连接数据库
```bash
mysql -u ardb -pLtHenC6MFTRt6GPe ardb
```

### 查看统计数据
```sql
-- 查看访问统计
SELECT * FROM access_statistics ORDER BY create_time DESC LIMIT 10;

-- 查看用户历史
SELECT * FROM user_history ORDER BY update_time DESC LIMIT 10;

-- 统计各类型访问量
SELECT statistic_type, COUNT(*) as count FROM access_statistics GROUP BY statistic_type;
```

## 故障排查

### 常见问题

1. **启动失败 - 微信配置问题**
   - 症状: `WxRuntimeException: 添加相关配置`
   - 解决: 确保application.yml中有有效的微信配置，或使用模拟配置

2. **Lombok编译错误**
   - 症状: `找不到符号 方法 setXXX`
   - 解决: 升级Lombok到1.18.38以支持JDK 24

3. **数据库连接失败**
   - 检查数据库服务: `systemctl status mysqld`
   - 验证连接信息和密码

4. **端口占用**
   - 检查端口: `netstat -tlnp | grep 8081`
   - 查找占用进程: `lsof -i :8081`

### 日志查看
```bash
# 实时查看日志
tail -f /home/back/platform.log

# 查看启动日志
grep -A 10 -B 10 "Started Application" /home/back/platform.log

# 查看错误日志
grep -i error /home/back/platform.log
```

## 备份策略

### 应用备份
- 每次部署前自动备份当前JAR包
- 备份命名格式: `vr-platform-backup-YYYYMMDD_HHMMSS.jar`

### 数据库备份
```bash
# 备份统计相关表
mysqldump -u ardb -pLtHenC6MFTRt6GPe ardb access_statistics user_history > stats_backup_$(date +%Y%m%d).sql

# 恢复数据
mysql -u ardb -pLtHenC6MFTRt6GPe ardb < stats_backup_20250822.sql
```

## 监控要点

1. **应用状态**: 进程是否正常运行
2. **内存使用**: JVM堆内存配置 -Xmx1500M -Xms1024M
3. **数据库连接**: 连接池状态
4. **API响应**: 统计接口是否正常响应
5. **数据增长**: 统计表数据增长情况

## 版本信息

- **当前版本**: 5.1
- **Spring Boot**: 2.7.14
- **Java**: 11
- **Lombok**: 1.18.38
- **部署日期**: 2025-08-22
- **部署人员**: Claude Code Assistant