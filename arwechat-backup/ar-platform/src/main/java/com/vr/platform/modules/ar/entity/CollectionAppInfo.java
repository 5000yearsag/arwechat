package com.vr.platform.modules.ar.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vr.platform.common.bean.entity.BaseInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("collection_app")
public class CollectionAppInfo{

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "合集uuid")
    private String collectionUuid;

    @ApiModelProperty(value = "小程序id")
    private String appId;

    @ApiModelProperty(value = "小程序名称")
    private String appName;

    @ApiModelProperty(value = "小程序码")
    private String wxImgUrl;

    @ApiModelProperty(value = "微信小程序跳转参数")
    private String wxJumpParam;
}
