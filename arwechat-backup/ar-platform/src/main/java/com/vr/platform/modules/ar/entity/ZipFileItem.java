package com.vr.platform.modules.ar.entity;

import lombok.Data;

@Data
public class ZipFileItem {
    private String type;
    private String id;
    private String refImage;
    private String resPath;
    private String resName;
    private String title;
    private String initScale;
    private String needLoad;
    private String offsetX;
    private String offsetY;
    private String offsetZ;
    private String setScale;
    private String rotateUp;
    private String rotateRight;
    private String rotateForward;
}
