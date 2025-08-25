package com.vr.platform.modules.ar.entity.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DelCollectionAppRequest {
    @NotBlank
    private String collectionUuid;
    @NotBlank
    private String appId;
}
