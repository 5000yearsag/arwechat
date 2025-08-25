package com.vr.platform.modules.ar.entity;

import com.vr.platform.common.bean.entity.BaseInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WxAppInfo extends BaseInfo {

    @ApiModelProperty(value = "小程序名")
    private String appName;

    @ApiModelProperty(value = "app id")
    private String appId;

    @ApiModelProperty(value = "app secret")
    private String appSecret;

    @ApiModelProperty(value = "微信小程序跳转参数")
    private String wxJumpParam;

}