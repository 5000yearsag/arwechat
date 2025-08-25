package com.vr.platform.modules.ar.entity.response;

import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.SceneInfo;
import lombok.Data;

import java.util.List;

@Data
public class GetAllSceneRes {
    CollectionInfo collectionInfo;
    List<SceneInfo> sceneInfoList;
}
