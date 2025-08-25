package com.vr.platform.modules.ar.entity.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GetCollectionRequest {
    @NotBlank
    private String collectionUuid;
}
