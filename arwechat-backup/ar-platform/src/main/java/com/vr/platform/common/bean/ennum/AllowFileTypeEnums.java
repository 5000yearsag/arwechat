package com.vr.platform.common.bean.ennum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author zhangchenyang
 * @date 2023/4/24 0024
 */
@Getter
@AllArgsConstructor
public class AllowFileTypeEnums {

    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";
    public static final String PNG = "png";
    public static final String GIF = "gif";
    public static final String MP4 = "mp4";
    public static final String AVI = "avi";
    public static final String MOV = "mov";
    public static final String GLB = "glb";

    public static List<String> getAllTypeList() {
        return java.util.Arrays.asList(
                JPG, JPEG, PNG, GIF, MP4, AVI, MOV, GLB
        );
    }
    public static List<String> getPictureTypeList() {
        return java.util.Arrays.asList(
                JPG, JPEG, PNG, GIF
        );
    }

    public static List<String> getVideoTypeList() {
        return java.util.Arrays.asList(
                MP4,AVI, MOV
        );
    }

    public static List<String> getModelTypeList() {
        return java.util.Arrays.asList(
                GLB
        );
    }
}
