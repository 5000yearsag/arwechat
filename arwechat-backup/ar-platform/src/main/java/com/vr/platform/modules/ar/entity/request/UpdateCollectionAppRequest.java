package com.vr.platform.modules.ar.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class UpdateCollectionAppRequest {
    @ApiModelProperty(value = "合集uuid")
    @NotBlank
    private String collectionUuid;

    @ApiModelProperty(value = "小程序信息列表")
    private List<AddCollectionAppRequest> wxAppInfoList;
}
