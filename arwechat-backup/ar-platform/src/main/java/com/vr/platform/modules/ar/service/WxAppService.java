package com.vr.platform.modules.ar.service;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.service.CommonService;
import com.vr.platform.common.utils.CommonUtils;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.WxAppInfo;
import com.vr.platform.modules.ar.entity.request.*;
import com.vr.platform.modules.ar.mapper.CollectionInfoMapper;
import com.vr.platform.modules.ar.mapper.SceneInfoMapper;
import com.vr.platform.modules.ar.mapper.WxAppMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.List;

@Service
public class WxAppService {

    @Resource
    private WxAppMapper wxAppMapper;

    public List<WxAppInfo> getAllWxAppList() {
        return wxAppMapper.getAllWxApp();
    }

    public WxAppInfo getAppByAppId(GetWxAppRequest request) {
        return wxAppMapper.getAppByAppId(request.getAppId());
    }

    public void addWxApp(AddWxAppRequest appRequest) {
        WxAppInfo appByAppId = wxAppMapper.getAppByAppId(appRequest.getAppId());
        if(ObjectUtil.isNotEmpty(appByAppId)){
            throw new BizException(BizReturnCode.BIZ_WX_APP_EXIST);
        }
        wxAppMapper.addNewWxApp(appRequest);
    }

    public void updateApp(UpdateWxAppRequest appRequest) {
        WxAppInfo appByAppId = wxAppMapper.findById(appRequest.getId());
        if(ObjectUtil.isEmpty(appByAppId)){
            throw new BizException(BizReturnCode.BIZ_WX_APP_NOT_EXIST);
        }
        wxAppMapper.updateApp(appRequest);
    }

    public void deleteApp(Long id) {
        wxAppMapper.deleteApp(id);
    }

    public void deleteByAppId(String appId) {
        wxAppMapper.deleteByAppId(appId);
    }
}
