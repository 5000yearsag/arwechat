package com.vr.platform.modules.ar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.WxAppInfo;
import com.vr.platform.modules.ar.entity.request.AddSceneRequest;
import com.vr.platform.modules.ar.entity.request.AddWxAppRequest;
import com.vr.platform.modules.ar.entity.request.UpdateWxAppRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WxAppMapper extends BaseMapper<WxAppInfo> {

    @Select("SELECT * FROM wx_app_info WHERE id = #{id}")
    WxAppInfo findById(@Param("id") Long id);

    @Select("SELECT * FROM wx_app_info")
    List<WxAppInfo> getAllWxApp();

    @Select("SELECT * FROM wx_app_info where app_id = #{appId} ")
    WxAppInfo getAppByAppId(@Param("appId") String appId);

    @Insert("INSERT INTO wx_app_info(app_name, app_id, app_secret,wx_jump_param) " +
            "VALUES(#{appName}, #{appId}, #{appSecret}, #{wxJumpParam})")
    void addNewWxApp(AddWxAppRequest wxAppRequest);

    @Update("UPDATE wx_app_info SET app_name = #{appName}, app_id = #{appId}, app_secret=#{appSecret},wx_jump_param=#{wxJumpParam}" +
            " WHERE id = #{id}")
    void updateApp(UpdateWxAppRequest wxAppRequest);

    @Delete("DELETE FROM wx_app_info WHERE id = #{id}")
    void deleteApp(@Param("id") Long id);

    @Delete("DELETE FROM wx_app_info where app_id = #{appId}")
    void deleteByAppId(@Param("appId") String appId);

}
