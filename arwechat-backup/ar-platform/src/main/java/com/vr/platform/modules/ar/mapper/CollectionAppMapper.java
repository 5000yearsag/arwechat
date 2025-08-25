package com.vr.platform.modules.ar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vr.platform.modules.ar.entity.CollectionAppInfo;
import com.vr.platform.modules.ar.entity.request.AddCollectionAppRequest;
import com.vr.platform.modules.ar.entity.request.DelCollectionAppRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CollectionAppMapper extends BaseMapper<CollectionAppInfo> {

    @Select("SELECT * FROM collection_app WHERE id = #{id}")
    CollectionAppInfo getCollectionAppById(@Param("id") Long id);

    @Select("SELECT ca.*, wai.app_name as app_name FROM collection_app as ca, wx_app_info as wai" +
            " where ca.collection_uuid = #{collectionUuid} " +
            " and ca.app_id = wai.app_id ")
    List<CollectionAppInfo> getAppByCollectionUuid(@Param("collectionUuid") String collectionUuid);

    @Select("SELECT * FROM collection_app " +
            " where app_id = #{appId} " +
            " and collection_uuid = #{collectionUuid}")
    CollectionAppInfo getAppByCollectionAppId(@Param("appId") String appId,
                                              @Param("collectionUuid") String collectionUuid);

    @Select("SELECT * FROM collection_app where app_id = #{appId} ")
    List<CollectionAppInfo> getAppByAppId(@Param("appId") String appId);

    @Insert("INSERT INTO collection_app(collection_uuid, app_id, wx_img_url, wx_jump_param) " +
            "VALUES(#{collectionUuid}, #{appId}, #{wxImgUrl}, #{wxJumpParam})")
    void addCollectionApp(AddCollectionAppRequest appRequest);

    @Update("UPDATE collection_app SET wx_jump_param = #{wxJumpParam}" +
            " where app_id = #{appId} " +
            " and collection_uuid = #{collectionUuid}")
    void updateJumpParam(AddCollectionAppRequest appRequest);

    @Delete("DELETE FROM collection_app " +
            " where app_id = #{appId} " +
            " and collection_uuid = #{collectionUuid}")
    void deleteByCollectionAppId(DelCollectionAppRequest request);

    @Delete("DELETE FROM collection_app where collection_uuid = #{collectionUuid}")
    void deleteByCollectionUuid(@Param("collectionUuid") String collectionUuid);

    @Delete("DELETE FROM collection_app " +
            " where app_id = #{appId} " +
            " and collection_uuid = #{collectionUuid}")
    void deleteByCollectionUuidAndAppId(@Param("appId") String appId,
                                        @Param("collectionUuid") String collectionUuid);

}
