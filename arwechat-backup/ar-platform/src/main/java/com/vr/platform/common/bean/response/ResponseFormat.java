package com.vr.platform.common.bean.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangchenyang
 * @date 2022/1/14 0014
 */
public class ResponseFormat<T> extends BaseResponse<T> {
    private static final Logger log = LoggerFactory.getLogger(ResponseFormat.class);

    public ResponseFormat() {
    }

    public ResponseFormat(IReturnCode returnCode, T data){
        this.setReturnCode(returnCode.getValue());
        this.setReturnDesc(returnCode.getDesc());
        this.setData(data);
    }

    public static <T> ResponseFormat<T> of(IReturnCode returnCode){
        return of(returnCode, null);
    }

    public static <T> ResponseFormat<T> of(IReturnCode returnCode, T data){
        return new ResponseFormat(returnCode, data);
    }

    public static <T> ResponseFormat<T> success(IReturnCode returnCode, T data){
        return of(returnCode, data);
    }

    public static <T> ResponseFormat<T> success( T data){
        return success(ReturnCode.UNI_REQUEST_SUCCESSDED, data);
    }

    public static <T> ResponseFormat<T> success(){
        return success(null);
    }

    public static <T> ResponseFormat<T> fail(IReturnCode returnCode, T data){
        return of(returnCode, data);
    }

    public static <T> ResponseFormat<T> fail(IReturnCode returnCode){
        return fail(returnCode, null);
    }

    public static <T> ResponseFormat<T> fail(T data){
        return fail(ReturnCode.UNI_REQUEST_FAILED, data);
    }

    public static <T> ResponseFormat<T> fail(){
        return fail(ReturnCode.UNI_REQUEST_FAILED);
    }

    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if(!(o instanceof ResponseFormat)){
            return false;
        }else {
            ResponseFormat<?> other = (ResponseFormat)o;
            return other.canEqual(this);
        }
    }

    protected boolean canEqual(Object other){
        return other instanceof ResponseFormat;
    }

    @Override
    public String toString() {
        return "ResponseFormat(super=" + super.toString() +" )";
    }
}
