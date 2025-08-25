package com.vr.platform.common.bean.response;

import lombok.Setter;

public enum ReturnCode implements IReturnCode{

    UNAUTHORIZED(401, "token 已失效，请重新登录"),

    UNI_LOGIN_FAILED(11030, " Login failed 登陆失败"),
    UNI_PARAMTER_FAILED(12010, " Paramter verification failed 参数校验失败"),
    UNI_REQUEST_SUCCESSDED(17000, " Request succeeded 请求成功"),
    UNI_REQUEST_FAILED(17010, " Request failed 请求失败"),

    UNI_SYSTEM_USER_NOT_EXIST(11001, " User does not exist 用户不存在"),
    UNI_SYSTEM_USER_PWD_ERROR(11002, "用户登录失败，用户名或密码错误"),
    UNI_SYSTEM_USER_NOT_LOGIN(11003, "用户未登录，请先登录"),



    ;

    private int value;
    private String desc;

    private ReturnCode(int value, String desc){
        this.value = value;
        this.desc = desc;
    }
    public int getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }

    public ReturnCode setDesc(String desc) {
        this.desc = desc;
        return this;
    }
}
