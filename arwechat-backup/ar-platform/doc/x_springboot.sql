CREATE DATABASE IF NOT EXISTS `yaoculture`

drop table if exists `t_user`;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `name` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户表';

drop table if exists `collection_info`;
CREATE TABLE `collection_info` (
     `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
     `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
     `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
     `collection_name` varchar(64) NOT NULL COMMENT '合集名称`',
     `cover_img_url` varchar(200) DEFAULT NULL COMMENT '封面图',
     `description` varchar(200) DEFAULT NULL COMMENT '合集描述',
     PRIMARY KEY (`id`) USING BTREE,
     UNIQUE KEY `collection_uuid` (`collection_uuid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='合集信息表';

drop table if exists `scene_info`;
CREATE TABLE `scene_info` (
     `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
     `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-正常',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
     `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
     `scene_uuid` varchar(64) NOT NULL COMMENT '场景uuid',
     `scene_name` varchar(64) NOT NULL COMMENT '场景名称`',
     `scene_img_url` varchar(200) NOT NULL COMMENT '场景识别图',
     `ar_resource_url` varchar(200) NOT NULL COMMENT 'ar资源地址',
     `ar_resource_dimension` varchar(20) DEFAULT NULL COMMENT 'ar资源尺寸',
     `video_effect` varchar(10) DEFAULT NULL COMMENT 'ar视频类型',
     `space_param` varchar(200) DEFAULT NULL COMMENT '空间参数',
     `description` varchar(200) DEFAULT NULL COMMENT '合集描述',
     PRIMARY KEY (`id`) USING BTREE,
     UNIQUE KEY `scene_uuid` (`scene_uuid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='场景信息表';

drop table if exists `wx_app_info`;
CREATE TABLE `wx_app_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',

  `app_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'app 名称',
  `app_id` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'app id',
  `app_secret` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'app secret',
  `wx_jump_param` varchar(100) DEFAULT NULL COMMENT '小程序跳转参数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='微信小程序信息表';

drop table if exists `collection_app`;
CREATE TABLE `collection_app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `collection_uuid` varchar(64) NOT NULL COMMENT '合集uuid',
  `app_id` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'app id',
  `wx_img_url` varchar(500) DEFAULT NULL COMMENT '小程序码',
  `wx_jump_param` varchar(100) DEFAULT NULL COMMENT '小程序跳转参数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='合集与微信小程序关联表';


INSERT INTO `yaoculture`.`t_user` (`id`, `username`, `password`, `email`, `phone`, `status`) VALUES ('1', 'user1', '$2a$10$oRUL6Z7B86L4mDfVaQzEEe9XxmfnnBOCcBmOUSVV/0XP1jIcosohe', '111124444', '13978786565', '0');
INSERT INTO `yaoculture`.`t_user` (`id`, `username`, `password`, `email`, `phone`, `status`) VALUES ('2', 'user2', '$2a$10$oRUL6Z7B86L4mDfVaQzEEe9XxmfnnBOCcBmOUSVV/0XP1jIcosohe', '111124444', '13978786565', '0');
INSERT INTO `yaoculture`.`t_user` (`id`, `username`, `password`, `email`, `phone`, `status`) VALUES ('3', 'user3', '$2a$10$oRUL6Z7B86L4mDfVaQzEEe9XxmfnnBOCcBmOUSVV/0XP1jIcosohe', '111124444', '13978786565', '0');
