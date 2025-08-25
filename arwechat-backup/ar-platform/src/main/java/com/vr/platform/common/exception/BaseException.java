package com.vr.platform.common.exception;


import com.vr.platform.common.bean.response.IReturnCode;

public interface BaseException {

    IReturnCode getReturnCode();

    String getMessage();

    String getMsg();
}
