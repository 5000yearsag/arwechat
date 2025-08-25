package com.vr.platform.common.bean.response;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zhangchenyang
 * @date 2022/1/14 0014
 */
public class BaseResponse<T> {
    @ApiModelProperty("Return code 返回码")
    private Integer returnCode;

    @ApiModelProperty("Return description 返回描述")
    private String returnDesc;
    private T data;

    public BaseResponse(){

    }

    public Integer getReturnCode(){
        return this.returnCode;
    }

    public String getReturnDesc(){
        return this.returnDesc;
    }

    public T getData(){
        return this.data;
    }

    public void setReturnCode(Integer returnCode){
        this.returnCode = returnCode;
    }

    public void setReturnDesc(String returnDesc){
        this.returnDesc = returnDesc;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "returnCode=" + this.getReturnCode() +
                ", returnDesc='" + this.getReturnDesc() + '\'' +
                ", data=" + this.getData() +
                '}';
    }
}
