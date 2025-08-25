package com.vr.platform.modules.ar.service;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vr.platform.common.bean.ennum.AllowFileTypeEnums;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.service.CommonService;
import com.vr.platform.common.utils.CommonUtils;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.request.AddSceneRequest;
import com.vr.platform.modules.ar.entity.request.GetAllSceneRequest;
import com.vr.platform.modules.ar.entity.request.UpdateSceneStatusRequest;
import com.vr.platform.modules.ar.entity.response.WxGetAllSceneRes;
import com.vr.platform.modules.ar.mapper.CollectionInfoMapper;
import com.vr.platform.modules.ar.mapper.SceneInfoMapper;
import com.vr.platform.modules.oss.service.FileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.List;

@Service
public class SceneInfoService {

    @Resource
    private CollectionInfoMapper collectionInfoMapper;

    @Resource
    private SceneInfoMapper sceneInfoMapper;

    @Resource
    private CommonService commonService;

    @Resource
    private FileService fileService;

    public SceneInfo getScene(String sceneUuid) {
        SceneInfo sceneInfo = sceneInfoMapper.getSceneByUuid(sceneUuid);
        if(ObjectUtil.isEmpty(sceneInfo)){
            throw new BizException(BizReturnCode.BIZ_SCENE_NOT_EXIST);
        }
        try {
            sceneInfo.setArResourceFileName(getArResourceFileName(sceneInfo.getArResourceUrl()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return sceneInfo;
    }

    public PageInfo<SceneInfo> getAllScene(GetAllSceneRequest request) {
        CollectionInfo collection = collectionInfoMapper.getCollectionByUuid(request.getCollectionUuid());
        if(ObjectUtil.isNull(collection)){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_NOT_EXIST);
        }
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<SceneInfo> allSceneByCollection = sceneInfoMapper.getAllSceneByCollection(request.getCollectionUuid());
        allSceneByCollection.forEach(sceneInfo -> {
            if(ObjectUtil.isNotNull(sceneInfo.getArResourceUrl())){
                if(AllowFileTypeEnums.getVideoTypeList().contains(FileUtils.getExtension(sceneInfo.getArResourceUrl()))){
                    sceneInfo.setArResourceType("video");
                }
                if(AllowFileTypeEnums.getModelTypeList().contains(FileUtils.getExtension(sceneInfo.getArResourceUrl()))){
                    sceneInfo.setArResourceType("model");
                    try {
                        sceneInfo.setArResourceFileName(getArResourceFileName(sceneInfo.getArResourceUrl()));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return new PageInfo<>(allSceneByCollection);
    }
    public WxGetAllSceneRes getAllSceneByCollectionForGuest(String collectionUuid) {
        WxGetAllSceneRes result = new WxGetAllSceneRes();
        CollectionInfo collection = collectionInfoMapper.getCollectionByUuid(collectionUuid);
        if(ObjectUtil.isNull(collection)){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_NOT_EXIST);
        }
        result.setSceneInfo(collection);
        List<SceneInfo> allSceneByCollection = sceneInfoMapper.getAllSceneByCollectionForGuest(collectionUuid);
        allSceneByCollection.forEach(sceneInfo -> {
            if(ObjectUtil.isNotNull(sceneInfo.getArResourceUrl())){
                if(AllowFileTypeEnums.getVideoTypeList().contains(FileUtils.getExtension(sceneInfo.getArResourceUrl()))){
                    sceneInfo.setArResourceType("video");
                }
                if(AllowFileTypeEnums.getModelTypeList().contains(FileUtils.getExtension(sceneInfo.getArResourceUrl()))){
                    sceneInfo.setArResourceType("model");
                    try {
                        sceneInfo.setArResourceFileName(getArResourceFileName(sceneInfo.getArResourceUrl()));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        result.setSceneList(allSceneByCollection);
        return result;
    }
    private String getArResourceFileName(String arResourceUrl) throws MalformedURLException {
        if(ObjectUtil.isEmpty(arResourceUrl)){
            return "";
        }
        try {
            String fileName = FileUtils.getFileNameFromUrl(arResourceUrl);
            return fileName.substring(7);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String addScene(AddSceneRequest addSceneRequest) throws MalformedURLException {
        if(ObjectUtils.isEmpty(addSceneRequest.getCollectionUuid())){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_UUID_BLANK);
        }
        if(ObjectUtils.isEmpty(addSceneRequest.getSceneName())){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_NAME_BLANK);
        }
        if(ObjectUtils.isEmpty(addSceneRequest.getArResourceUrl())){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_AR_RESOURCE_EMPTY);
        }
        CollectionInfo collection = collectionInfoMapper.getCollectionByUuid(addSceneRequest.getCollectionUuid());
        if(ObjectUtils.isEmpty(collection)){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_NOT_EXIST);
        }

        addSceneRequest.setSceneUuid(CommonUtils.getCurrentTimeStamp());
        //处理透明视频的尺寸
        if(!ObjectUtils.isEmpty(addSceneRequest.getVideoEffect())
                && StringUtils.equals(addSceneRequest.getVideoEffect(), "tsbs")){
            String arResourceDimension = addSceneRequest.getArResourceDimension();
            if(ObjectUtils.isEmpty(arResourceDimension)){
                throw new BizException(BizReturnCode.BIZ_COLLECTION_AR_RESOURCE_DIMENSION_BLANK);
            }
            addSceneRequest.setArResourceDimension(fileService.processDimensions(arResourceDimension));
        }

        sceneInfoMapper.insertSceneInfo(addSceneRequest);
        return addSceneRequest.getSceneUuid();
    }


    public void updateScene(SceneInfo request) {
        SceneInfo oldSceneInfo = sceneInfoMapper.getSceneByUuid(request.getSceneUuid());
        if(ObjectUtil.isNull(oldSceneInfo)){
            throw new BizException(BizReturnCode.BIZ_SCENE_NOT_EXIST);
        }
        //如果从非透明视频改为透明视频，则缩小宽度为以前的1/2
        if(ObjectUtils.isEmpty(oldSceneInfo.getVideoEffect())
                && !ObjectUtils.isEmpty(request.getVideoEffect())
                && StringUtils.equals(request.getVideoEffect(), "tsbs")){
            String arResourceDimension = request.getArResourceDimension();
            if(ObjectUtils.isEmpty(arResourceDimension)){
                throw new BizException(BizReturnCode.BIZ_COLLECTION_AR_RESOURCE_DIMENSION_BLANK);
            }
            request.setArResourceDimension(fileService.processDimensions(arResourceDimension));
        }
        // 如果从透明视频改为非透明视频，则使用之前的尺寸
        if(!ObjectUtils.isEmpty(oldSceneInfo.getVideoEffect())
                && ObjectUtils.isEmpty(request.getVideoEffect())){
            if(ObjectUtils.isEmpty(oldSceneInfo.getArResourceDimension())){
                throw new BizException(BizReturnCode.BIZ_COLLECTION_AR_RESOURCE_DIMENSION_BLANK);
            }
            request.setArResourceDimension(fileService.processDimensions(oldSceneInfo.getArResourceDimension(),"upscale"));
        }
        sceneInfoMapper.updateSceneInfo(request);
    }

    public void changeSceneStatus(UpdateSceneStatusRequest sceneInfo) {
        getScene(sceneInfo.getSceneUuid());
        sceneInfoMapper.changeSceneStatus(sceneInfo);
    }

    public void delete(String sceneUuid) {
        SceneInfo sceneInfo = sceneInfoMapper.getSceneByUuid(sceneUuid);
        if(ObjectUtil.isNotEmpty(sceneInfo)){
            fileService.delete(sceneInfo.getSceneImgUrl());
            fileService.delete(sceneInfo.getArResourceUrl());
            sceneInfoMapper.deleteByUuid(sceneUuid);
        }
    }
}
