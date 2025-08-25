package com.vr.platform.modules.ar.mapper;

import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.request.AddSceneRequest;
import com.vr.platform.modules.ar.entity.request.UpdateSceneStatusRequest;
import com.vr.platform.modules.ar.entity.request.UpdateWxAppRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SceneInfoMapper {

    @Select("SELECT * FROM scene_info WHERE id = #{id}")
    SceneInfo getSceneInfo(@Param("id") Long id);

    @Select("SELECT * FROM scene_info WHERE scene_uuid = #{sceneUuid}")
    SceneInfo getSceneByUuid(@Param("sceneUuid") String sceneUuid);

    @Select("SELECT * FROM scene_info")
    List<SceneInfo> findAllScene();

    @Select("SELECT * FROM scene_info WHERE collection_uuid = #{collectionUuid} ")
    List<SceneInfo> getAllSceneByCollection(@Param("collectionUuid") String collectionUuid);

    @Select("SELECT count(*) FROM scene_info WHERE collection_uuid = #{collectionUuid} ")
    int getSceneCountByCollection(@Param("collectionUuid") String collectionUuid);

    @Select("SELECT * FROM scene_info WHERE collection_uuid = #{collectionUuid} and status ='1'")
    List<SceneInfo> getAllSceneByCollectionForGuest(@Param("collectionUuid") String collectionUuid);

    @Insert("INSERT INTO scene_info(collection_uuid, scene_uuid, scene_name, scene_img_url, " +
            "ar_resource_url, space_param, ar_resource_dimension, video_effect) " +
            "VALUES(#{collectionUuid}, #{sceneUuid}, #{sceneName}, #{sceneImgUrl}, " +
            " #{arResourceUrl}, #{spaceParam}, #{arResourceDimension}, #{videoEffect} )")
    void insertSceneInfo(AddSceneRequest sceneInfo);

    @Update("UPDATE scene_info SET  scene_name = #{sceneName}, " +
            "scene_img_url = #{sceneImgUrl}, ar_resource_url = #{arResourceUrl}, space_param = #{spaceParam}, " +
            " ar_resource_dimension = #{arResourceDimension},video_effect = #{videoEffect} " +
            "WHERE id = #{id}")
    void updateSceneInfo(SceneInfo sceneInfo);

    @Update("UPDATE scene_info SET status = #{status} " +
            " WHERE scene_uuid = #{sceneUuid}")
    void changeSceneStatus(UpdateSceneStatusRequest sceneInfo);

    @Delete("DELETE FROM scene_info WHERE id = #{id}")
    void deleteSceneInfo(@Param("id") Long id);

    @Delete("DELETE FROM scene_info WHERE scene_uuid = #{sceneUuid}")
    void deleteByUuid(@Param("sceneUuid") String sceneUuid);

    @Delete("DELETE FROM scene_info WHERE collection_uuid = #{collectionUuid}")
    void deleteByCollectionUuid(@Param("collectionUuid") String collectionUuid);
}
