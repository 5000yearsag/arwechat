package com.vr.platform.modules.ar.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateSceneStatusRequest {
    private String sceneUuid;
    @ApiModelProperty(value = "状态  0：禁用   1：正常")
    private Integer status;
}
