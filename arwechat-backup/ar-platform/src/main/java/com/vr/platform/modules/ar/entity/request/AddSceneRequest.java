package com.vr.platform.modules.ar.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class AddSceneRequest {
    @ApiModelProperty(value = "合集uuid")
    private String collectionUuid;

    @ApiModelProperty(value = "场景名称")
    private String sceneName;

    @ApiModelProperty(value = "场景图片")
    private String sceneImgUrl;

    @ApiModelProperty(value = "场景AR资源")
    private String arResourceUrl;

    @ApiModelProperty(value = "场景AR资源尺寸")
    private String arResourceDimension;

    @ApiModelProperty(value = "场景AR视频类型 tsbs-'透明视频'")
    private String videoEffect;

    @ApiModelProperty(value = "场景AR参数")
    private String spaceParam;

    @ApiModelProperty(value = "场景描述")

    private String description;

    @ApiModelProperty(value = "场景uuid",hidden = true)
    private String sceneUuid;
}
