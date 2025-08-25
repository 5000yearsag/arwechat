package com.vr.platform.modules.ar.entity;

import com.vr.platform.common.bean.entity.BaseInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CollectionInfo extends BaseInfo {

    @ApiModelProperty(value = "合集uuid")
    private String collectionUuid;

    @ApiModelProperty(value = "合集名称")
    private String collectionName;

    @ApiModelProperty(value = "合集封面")
    private String coverImgUrl;

    @ApiModelProperty(value = "合集描述")
    private String description;
}
