package com.vr.platform.modules.ar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.request.AddCollectionRequest;
import com.vr.platform.modules.ar.entity.response.GetCollectionRes;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CollectionInfoMapper extends BaseMapper<CollectionInfo> {

    @Select("SELECT * FROM collection_info WHERE id = #{id}")
    CollectionInfo getCollection(@Param("id") Long id);

    @Select("SELECT * FROM collection_info WHERE collection_uuid = #{collectionUuid}")
    CollectionInfo getCollectionByUuid(@Param("collectionUuid") String collectionUuid);

    @Select("SELECT * FROM collection_info as ci " +
            " left join collection_app as ca " +
            " on ci.collection_uuid = ca.collection_uuid ")
    List<GetCollectionRes> getAllCollectionList();

    @Select("SELECT * FROM collection_info order by update_time desc")
    List<GetCollectionRes> getAllCollection();


    @Update("UPDATE collection_info SET collection_name = #{collectionName}, " +
            "cover_img_url = #{coverImgUrl},  description = #{description} " +
            "WHERE collection_uuid = #{collectionUuid}")
    void updateCollection(CollectionInfo collectionInfo);

    @Insert("INSERT INTO collection_info(collection_uuid, collection_name, cover_img_url, description) " +
            "VALUES(#{collectionUuid}, #{collectionName}, #{coverImgUrl}, #{description})")
    int addCollection(AddCollectionRequest collectionInfo);

    @Delete("DELETE FROM collection_info WHERE collection_uuid = #{collectionUuid}")
    void deleteByUuid(@Param("collectionUuid") String collectionUuid);
}