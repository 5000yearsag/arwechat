package com.vr.platform.modules.ar.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AddCollectionAppRequest {
    @ApiModelProperty(value = "合集uuid",hidden = true)
    private String collectionUuid;

    @ApiModelProperty(value = "小程序id")
    private String appId;

    @ApiModelProperty(value = "小程序码",hidden = true)
    private String wxImgUrl;

    @ApiModelProperty(value = "微信小程序跳转参数")
    private String wxJumpParam;

}
