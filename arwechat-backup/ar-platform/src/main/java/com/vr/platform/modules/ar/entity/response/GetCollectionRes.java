package com.vr.platform.modules.ar.entity.response;

import com.vr.platform.modules.ar.entity.CollectionAppInfo;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import lombok.Data;

import java.util.List;

@Data
public class GetCollectionRes extends CollectionInfo{
    private int sceneCount;
    private List<CollectionAppInfo> collectionAppList;
}
