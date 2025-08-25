package com.vr.platform.common.exception;


import com.vr.platform.common.bean.response.IReturnCode;

public class BizException extends RuntimeException implements BaseException {
    private IReturnCode returnCode;

    private String msg;

    public IReturnCode getReturnCode() {
        return this.returnCode;
    }

    public String getMsg() {
        return this.msg;
    }

    public BizException(IReturnCode returnCode){
        super(returnCode.getDesc());
        this.returnCode = returnCode;
    }

    public BizException(IReturnCode returnCode, Throwable cause){
        super(returnCode.getDesc(), cause);
        this.returnCode = returnCode;
    }

    public BizException(IReturnCode returnCode, String msg){
        super(msg);
        this.returnCode = returnCode;
        this.msg = msg;
    }
}
