package com.vr.platform.modules.ar.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class
UpdateWxAppRequest {
    @ApiModelProperty(value = "id")
    @NotBlank
    private Long id;

    @ApiModelProperty(value = "小程序名")
    private String appName;

    @ApiModelProperty(value = "app id")
    private String appId;

    @ApiModelProperty(value = "app secret")
    private String appSecret;

    @ApiModelProperty(value = "微信小程序跳转参数")
    private String wxJumpParam;

}
