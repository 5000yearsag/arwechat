package com.vr.platform.modules.ar.entity.request;

import com.vr.platform.common.bean.request.BasePageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GetAllSceneRequest extends BasePageRequest {
    @ApiModelProperty(value = "合集uuid")
    @NotBlank
    private String collectionUuid;
}
