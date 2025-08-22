# 访问统计功能后端实现计划

## 项目概述
本文档详细说明了在AR小程序项目中添加访问统计功能的后端实现方案。目前小程序和arweb前端已经实现了统计功能的调用，但后端ar-platform服务中尚未实现相应的API接口。

## 现有前端统计功能分析

### 小程序统计埋点

小程序中已实现6种类型的统计埋点：

1. **页面访问统计 (pvCount)** 
   - 位置: `pages/index/index.js:159`
   - 触发时机: 用户首次访问页面时

2. **扫描入口点击统计 (click1Count)**
   - 位置: `pages/index/index.js:79`
   - 触发时机: 用户点击"AR体验"按钮时

3. **识别成功统计 (click2Count)**
   - 位置: `components/ar-tracker/index.js:75`
   - 触发时机: AR识别成功，用户与AR内容交互时

4. **分享统计 (click3Count)**
   - 位置: `pages/plane-ar-preview/index.js:18`, `pages/preview/scan.js:43`
   - 触发时机: 用户点击分享按钮时

5. **视频分享统计 (click4Count)**
   - 位置: `pages/preview/scan.js:143`
   - 触发时机: 用户分享录制的AR视频时

6. **AR相机准备统计 (click5Count)**
   - 位置: `components/ar-tracker/index.js:26`
   - 触发时机: AR相机初始化完成时

### API调用方式

所有统计都通过统一的API接口调用：
```javascript
wx.request({
  url: `${appInstance.globalData.domainWithProtocol}${appInstance.globalData.statisticApi}?collectionUuid=${collectionUuid}&type=${statisticType}`,
  method: "GET",
  header: { "content-type": "application/json" },
  success: (res) => {}
})
```

### 相关API接口

小程序中还调用了以下相关API：
- `/api/guest/openId` - 获取用户openId
- `/api/guest/historyRecord` - 记录用户访问历史  
- `/api/guest/history` - 获取用户访问历史

### 前端配置 (app.js)
```javascript
globalData: {
  domainWithProtocol: 'https://yaoculture.shenyuantek.com',
  openIdApi: '/api/guest/openId',
  statisticApi: '/api/guest/statistic',
  historyRecordApi: '/api/guest/historyRecord', 
  historyApi: '/api/guest/history',
  // ...其他配置
}
```

### arweb前端分析
arweb子模块主要是后台管理界面，没有发现访问统计相关的代码。

## 后端实现方案

### 1. 数据库设计

#### 1.1 访问统计表 (access_statistics)

```sql
CREATE TABLE `access_statistics` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
  `open_id` varchar(64) DEFAULT NULL COMMENT '用户openId',  
  `statistic_type` varchar(20) NOT NULL COMMENT '统计类型：pvCount/click1Count/click2Count/click3Count/click4Count/click5Count',
  `user_ip` varchar(45) DEFAULT NULL COMMENT '用户IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理信息',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_collection_type_time` (`collection_uuid`, `statistic_type`, `create_time`),
  KEY `idx_openid_time` (`open_id`, `create_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问统计表';
```

#### 1.2 用户访问历史表 (user_history)

```sql  
CREATE TABLE `user_history` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `open_id` varchar(64) NOT NULL COMMENT '用户openId',
  `app_id` varchar(64) NOT NULL COMMENT '小程序appId', 
  `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
  `collection_name` varchar(100) DEFAULT NULL COMMENT '合集名称',
  `last_access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
  `access_count` int(11) DEFAULT 1 COMMENT '访问次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_collection` (`open_id`, `collection_uuid`),
  KEY `idx_openid_time` (`open_id`, `last_access_time`),
  KEY `idx_collection_time` (`collection_uuid`, `last_access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户访问历史表';
```

### 2. Java实体类设计

#### 2.1 AccessStatistics.java
```java
package com.vr.platform.modules.ar.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class AccessStatistics {
    private Long id;
    private String collectionUuid;
    private String openId;
    private String statisticType;
    private String userIp;
    private String userAgent;
    private String sessionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
```

#### 2.2 UserHistory.java
```java
package com.vr.platform.modules.ar.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class UserHistory {
    private Long id;
    private String openId;
    private String appId;
    private String collectionUuid;
    private String collectionName;
    private Integer accessCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastAccessTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
```

### 3. API接口实现

#### 3.1 在GuestController.java中添加接口

```java
@Resource
private StatisticsService statisticsService;

/**
 * 访问统计记录
 */
@ApiModelProperty(value = "访问统计")
@GetMapping("/statistic")
public ResponseFormat<Void> recordStatistic(
    @RequestParam(name = "collectionUuid") String collectionUuid,
    @RequestParam(name = "type") String type,
    @RequestParam(name = "openId", required = false) String openId,
    HttpServletRequest request) {
    
    statisticsService.recordAccess(collectionUuid, type, openId, request);
    return ResponseFormat.success();
}

/**
 * 获取微信openId
 */
@ApiModelProperty(value = "获取微信openId")
@GetMapping("/openId")
public ResponseFormat<String> getOpenId(
    @RequestParam(name = "code") String code,
    @RequestParam(name = "appId") String appId) {
    
    String openId = wxAppService.getOpenId(code, appId);
    return ResponseFormat.success(openId);
}

/**
 * 记录用户访问历史
 */
@ApiModelProperty(value = "记录访问历史")
@GetMapping("/historyRecord")
public ResponseFormat<Void> recordHistory(
    @RequestParam(name = "openId") String openId,
    @RequestParam(name = "appId") String appId,
    @RequestParam(name = "collectionUuid") String collectionUuid) {
    
    statisticsService.recordUserHistory(openId, appId, collectionUuid);
    return ResponseFormat.success();
}

/**
 * 获取用户访问历史
 */
@ApiModelProperty(value = "获取访问历史")
@GetMapping("/history")
public ResponseFormat<List<UserHistory>> getUserHistory(
    @RequestParam(name = "openId") String openId) {
    
    List<UserHistory> history = statisticsService.getUserHistory(openId);
    return ResponseFormat.success(history);
}
```

### 4. 服务层实现

#### 4.1 StatisticsService.java
```java
package com.vr.platform.modules.ar.service;

import com.vr.platform.common.utils.IPUtils;
import com.vr.platform.modules.ar.entity.AccessStatistics;
import com.vr.platform.modules.ar.entity.UserHistory;
import com.vr.platform.modules.ar.mapper.AccessStatisticsMapper;
import com.vr.platform.modules.ar.mapper.UserHistoryMapper;
import com.vr.platform.modules.ar.mapper.CollectionInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class StatisticsService {

    @Resource
    private AccessStatisticsMapper accessStatisticsMapper;
    
    @Resource 
    private UserHistoryMapper userHistoryMapper;
    
    @Resource
    private CollectionInfoMapper collectionInfoMapper;

    /**
     * 记录访问统计
     */
    public void recordAccess(String collectionUuid, String type, String openId, HttpServletRequest request) {
        try {
            AccessStatistics statistics = new AccessStatistics();
            statistics.setCollectionUuid(collectionUuid);
            statistics.setStatisticType(type);
            statistics.setOpenId(openId);
            statistics.setUserIp(IPUtils.getIpAddr(request));
            statistics.setUserAgent(request.getHeader("User-Agent"));
            statistics.setSessionId(generateSessionId(request));
            
            accessStatisticsMapper.insert(statistics);
            log.info("记录访问统计成功: collection={}, type={}, openId={}", collectionUuid, type, openId);
        } catch (Exception e) {
            log.error("记录访问统计失败: collection={}, type={}, error={}", collectionUuid, type, e.getMessage(), e);
        }
    }

    /**
     * 记录用户访问历史
     */
    public void recordUserHistory(String openId, String appId, String collectionUuid) {
        try {
            // 获取合集名称
            String collectionName = collectionInfoMapper.getCollectionNameByUuid(collectionUuid);
            
            UserHistory existingHistory = userHistoryMapper.findByOpenIdAndCollection(openId, collectionUuid);
            if (existingHistory != null) {
                // 更新访问记录
                userHistoryMapper.updateAccessTime(openId, collectionUuid);
            } else {
                // 创建新记录
                UserHistory history = new UserHistory();
                history.setOpenId(openId);
                history.setAppId(appId);
                history.setCollectionUuid(collectionUuid);
                history.setCollectionName(collectionName);
                history.setAccessCount(1);
                
                userHistoryMapper.insert(history);
            }
            log.info("记录用户访问历史成功: openId={}, collection={}", openId, collectionUuid);
        } catch (Exception e) {
            log.error("记录用户访问历史失败: openId={}, collection={}, error={}", openId, collectionUuid, e.getMessage(), e);
        }
    }

    /**
     * 获取用户访问历史
     */
    public List<UserHistory> getUserHistory(String openId) {
        return userHistoryMapper.findByOpenIdOrderByTime(openId);
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        if (!StringUtils.hasText(sessionId)) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }
        return sessionId;
    }
}
```

### 5. Mapper接口实现

#### 5.1 AccessStatisticsMapper.java
```java
package com.vr.platform.modules.ar.mapper;

import com.vr.platform.modules.ar.entity.AccessStatistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface AccessStatisticsMapper {

    @Insert("INSERT INTO access_statistics(collection_uuid, open_id, statistic_type, user_ip, user_agent, session_id) " +
            "VALUES(#{collectionUuid}, #{openId}, #{statisticType}, #{userIp}, #{userAgent}, #{sessionId})")
    void insert(AccessStatistics statistics);

    @Select("SELECT * FROM access_statistics WHERE collection_uuid = #{collectionUuid} AND statistic_type = #{type} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<AccessStatistics> findByCollectionAndTypeAndTime(@Param("collectionUuid") String collectionUuid, 
                                                         @Param("type") String type,
                                                         @Param("startTime") Date startTime, 
                                                         @Param("endTime") Date endTime);

    @Select("SELECT COUNT(*) FROM access_statistics WHERE collection_uuid = #{collectionUuid} AND statistic_type = #{type}")
    Long countByCollectionAndType(@Param("collectionUuid") String collectionUuid, @Param("type") String type);
}
```

#### 5.2 UserHistoryMapper.java  
```java
package com.vr.platform.modules.ar.mapper;

import com.vr.platform.modules.ar.entity.UserHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserHistoryMapper {

    @Insert("INSERT INTO user_history(open_id, app_id, collection_uuid, collection_name, access_count) " +
            "VALUES(#{openId}, #{appId}, #{collectionUuid}, #{collectionName}, #{accessCount})")
    void insert(UserHistory history);

    @Select("SELECT * FROM user_history WHERE open_id = #{openId} AND collection_uuid = #{collectionUuid}")
    UserHistory findByOpenIdAndCollection(@Param("openId") String openId, @Param("collectionUuid") String collectionUuid);

    @Update("UPDATE user_history SET access_count = access_count + 1, last_access_time = NOW() " +
            "WHERE open_id = #{openId} AND collection_uuid = #{collectionUuid}")
    void updateAccessTime(@Param("openId") String openId, @Param("collectionUuid") String collectionUuid);

    @Select("SELECT * FROM user_history WHERE open_id = #{openId} ORDER BY last_access_time DESC LIMIT 50")
    List<UserHistory> findByOpenIdOrderByTime(@Param("openId") String openId);
}
```

### 6. 微信小程序服务扩展

需要在现有的WxAppService中添加获取openId的方法：

```java
/**
 * 通过code获取openId
 */
public String getOpenId(String code, String appId) {
    try {
        WxMaJscode2SessionResult session = wxMaService.switchoverTo(appId).getUserService().getSessionInfo(code);
        return session.getOpenid();
    } catch (WxErrorException e) {
        log.error("获取openId失败: code={}, appId={}, error={}", code, appId, e.getMessage());
        throw new BizException(BizReturnCode.WX_GET_OPENID_FAIL);
    }
}
```

### 7. 部署实施步骤

#### 7.1 数据库更新
1. 连接服务器: `ssh ali_lanyu`
2. 连接MySQL数据库
3. 执行数据库脚本创建新表

#### 7.2 代码部署
1. 将新增的Java代码提交到项目
2. 使用Maven构建项目: `mvn clean package`
3. 上传jar包到服务器
4. 重启ar-platform服务

#### 7.3 测试验证
1. 测试各个统计接口的调用
2. 验证数据库记录是否正确写入
3. 测试小程序端的统计功能

### 8. 监控和优化建议

#### 8.1 性能优化
- 对access_statistics表的大批量插入考虑异步处理
- 定期清理过期的统计数据
- 为高频查询添加合适的索引

#### 8.2 数据分析
- 可以基于统计数据生成用户行为分析报告
- 在arweb后台添加数据可视化图表
- 设置关键指标的监控告警

#### 8.3 扩展功能
- 支持按时间维度的统计查询
- 添加用户留存率分析
- 支持A/B测试数据收集

## 总结

本实现方案完整覆盖了小程序端所需的统计功能，包括：
- 6种类型的访问统计记录
- 用户历史访问记录管理  
- 微信openId获取
- 完整的数据库设计和Java后端实现

实施后将为产品运营提供完整的用户行为数据支持。