package com.vr.platform.common.bean.response;

import com.vr.platform.common.bean.response.IReturnCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author zhangchenyang
 * @date 2022/1/15 0015
 */
@AllArgsConstructor
@Getter
public enum BizReturnCode implements IReturnCode {


    /**
     * 通用错误码
     */
    BIZ_RETURN_CODE_UNDEFINE(20009, "未定义错误码"),
    BIZ_REQUEST_PARAM_ERROR(20010, "请求参数错误"),
    BIZ_SQL_INTEGRITY_CONSTRAINT_VIOLATION(20011, "违反SQL约束"),
    BIZ_FILE_UPLOAD_SIZE_EXCEEDS_LIMIT(20012, "文件大小超出限制"),
    BIZ_FILE_IS_EMPTY(20013, "文件不存在"),
    BIZ_MENU_NO_EXIST(20014, "权限不足"),
    BIZ_USER_LOGOUT(20080, "用户已登出"),
    BIZ_USER_DECRYPT_FAIL(20090, "用户信息解析失败"),
    BIZ_WX_RESULT_DECRYPT_FAIL(20091, "微信反参信息解析失败"),
    BIZ_NACOS_CONFIG_ERROR(20101, "配置文件错误"),
    BIZ_ADMIN_PERMISSION_CAN_OPERATE(20102, "非管理员不能操作"),
    BIZ_DATE_CHANGE_FAIL(20103, "时间类型转换失败"),
    BIZ_SCHEDULE_FAIL(20104,"定时任务失败"),
    BIZ_REQUEST_PARAM_IS_NULL(20015, "请求参数为空"),

    BIZ_OLD_PASSWORD_BLANK(20101,"原密码为空"),
    BIZ_NEW_PASSWORD_BLANK(20102,"原密码为空"),
    BIZ_OLD_PASSWORD_ERROR(20103,"原密码错误"),
    BIZ_CAPTCHA_EXPIRE(20201,"验证码过期"),
    BIZ_CAPTCHA_ERROR(20202,"验证码错误"),

    BIZ_FILE_TYPE_ERROR(34001,"文件类型不支持"),
    BIZ_FILE_UPLOAD_FAIL(34101,"上传文件失败"),
    BIZ_FILE_UPLOAD_OSS_FAIL(34102,"上传阿里云失败"),
    BIZ_FILE_UPLOAD_CLOUD_FAIL(34103,"上传阿里云失败"),
    BIZ_FILE_GENERATE_FAIL(34201,"文件生成失败"),

    BIZ_COLLECTION_NAME_BLANK(40101,"合集名称为空"),
    BIZ_COLLECTION_NAME_EXIST(40102,"合集名称已存在"),
    BIZ_COLLECTION_NOT_EXIST(40103,"合集不存在"),
    BIZ_COLLECTION_UUID_BLANK(40104,"合集uuid为空"),
    BIZ_COLLECTION_AR_RESOURCE_EMPTY(40106,"合集资源URL不能为空"),
    BIZ_COLLECTION_UUID_EXIST(40105,"合集uuid已存在"),

    BIZ_COLLECTION_APP_ADD_ERROR(40201,"合集小程序添加失败"),

    BIZ_SCENE_NOT_EXIST(40301,"场景不存在"),
    BIZ_COLLECTION_AR_RESOURCE_DIMENSION_BLANK(40302,"场景AR资源尺寸不能为空"),

    BIZ_WX_APP_EXIST(50101,"微信小程序已存在"),
    BIZ_WX_APP_NOT_EXIST(50102,"微信小程序已存在"),


    ;
    private int value;
    private String desc;

    public static IReturnCode getInstanceByValue(final int value){
        return Arrays.asList(values()).parallelStream().filter(t -> t.getValue()==value).findFirst().orElse(BIZ_RETURN_CODE_UNDEFINE);
    }
}















