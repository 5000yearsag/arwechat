package com.vr.platform.modules.ar.service;

import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.service.CommonService;
import com.vr.platform.common.utils.CommonUtils;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.modules.ar.entity.CollectionAppInfo;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.WxAppInfo;
import com.vr.platform.modules.ar.entity.request.*;
import com.vr.platform.modules.ar.entity.response.GetCollectionRes;
import com.vr.platform.modules.ar.mapper.CollectionAppMapper;
import com.vr.platform.modules.ar.mapper.CollectionInfoMapper;
import com.vr.platform.modules.ar.mapper.SceneInfoMapper;
import com.vr.platform.modules.ar.mapper.WxAppMapper;
import com.vr.platform.modules.oss.service.FileService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CollectionInfoService {

    @Resource
    private CollectionInfoMapper collectionInfoMapper;
    @Resource
    private CollectionAppMapper collectionAppMapper;
    @Resource
    private SceneInfoMapper sceneInfoMapper;
    @Resource
    private WxAppMapper wxAppMapper;

    @Resource
    private CommonService commonService;

    @Resource
    private WxMaService wxMaService;

    @Resource
    private FileService fileService;
    @Resource
    private FileStorageService fileStorageService;

    @Value("${file.env}")
    public String fileEnv;

    @Value("${file.server.ip}")
    public String fileServerIp;

    @Value("${file.server.path}")
    private String fileServerPath;

    @Value("${wx.request.url}")
    private String wxRequestUrl;

    public CollectionInfo findById(Long id) {
        return collectionInfoMapper.getCollection(id);
    }

    public GetCollectionRes getCollection(String collectionUuid) {
        GetCollectionRes res = new GetCollectionRes();
        CollectionInfo collectionInfo = collectionInfoMapper.getCollectionByUuid(collectionUuid);
        BeanUtils.copyProperties(collectionInfo, res);
        List<CollectionAppInfo> collectionAppInfoList = collectionAppMapper.getAppByCollectionUuid(collectionUuid);
        res.setCollectionAppList(collectionAppInfoList);
        return res;
    }

    public PageInfo<GetCollectionRes> getAllCollection(GetAllCollectionRequest request) {

        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<GetCollectionRes> allCollection = collectionInfoMapper.getAllCollection();
        allCollection.forEach(collectionRes -> {
            collectionRes.setSceneCount(sceneInfoMapper.getSceneCountByCollection(collectionRes.getCollectionUuid()));
            collectionRes.setCollectionAppList(collectionAppMapper.getAppByCollectionUuid(collectionRes.getCollectionUuid()));
        });

        return new PageInfo<>(allCollection);
    }

    public ResponseFormat addCollection(AddCollectionRequest addCollectionRequest) throws IOException {
        ResponseFormat responseFormat ;
        addCollectionRequest.setCollectionUuid(CommonUtils.getCurrentTimeStamp());

        if(ObjectUtil.isNotEmpty(addCollectionRequest.getWxAppInfoList())){
            try {
                addNewAppList(addCollectionRequest.getWxAppInfoList(), addCollectionRequest.getCollectionUuid());
            }catch (WxErrorException e){
                log.error("更新小程序信息失败");
                responseFormat = ResponseFormat.fail(BizReturnCode.BIZ_COLLECTION_APP_ADD_ERROR);
            }
        }

        collectionInfoMapper.addCollection(addCollectionRequest);
        responseFormat = ResponseFormat.success(addCollectionRequest.getCollectionUuid());
        return  responseFormat;
    }

    public void updateCollection(UpdateCollectionRequest updateCollectionRequest) throws MalformedURLException, WxErrorException, UnsupportedEncodingException {
        CollectionInfo collectionInfo = new CollectionInfo();
        BeanUtils.copyProperties(updateCollectionRequest, collectionInfo);
        collectionInfoMapper.updateCollection(collectionInfo);

        if(ObjectUtil.isNotEmpty(updateCollectionRequest.getWxAppInfoList())){
            UpdateCollectionAppRequest updateCollectionAppRequest = new UpdateCollectionAppRequest();
            updateCollectionAppRequest.setCollectionUuid(updateCollectionRequest.getCollectionUuid());
            updateCollectionAppRequest.setWxAppInfoList(updateCollectionRequest.getWxAppInfoList());
            updateCollectionWxApp(updateCollectionAppRequest);
        }

    }

    public void updateCollectionBaseInfo(CollectionInfo collectionInfo) {
        collectionInfoMapper.updateCollection(collectionInfo);
    }

    public void updateCollectionWxApp(UpdateCollectionAppRequest updateCollectionAppRequest) throws MalformedURLException, WxErrorException, UnsupportedEncodingException {
        CollectionInfo collectionInfo = collectionInfoMapper.getCollectionByUuid(updateCollectionAppRequest.getCollectionUuid());
        if(ObjectUtil.isEmpty(collectionInfo)){
            throw new BizException(BizReturnCode.BIZ_COLLECTION_NOT_EXIST);
        }

        List<AddCollectionAppRequest> newAppInfoList = updateCollectionAppRequest.getWxAppInfoList();
        if(ObjectUtil.isNotEmpty(newAppInfoList)){
            List<CollectionAppInfo> oldAppInfoList = collectionAppMapper.getAppByCollectionUuid(updateCollectionAppRequest.getCollectionUuid());
            // 找出newAppInfoList中有值，但是oldAppInfoList中没有的
            List<AddCollectionAppRequest> addAppInfoList = newAppInfoList.stream().filter(addCollectionAppRequest -> {
                return oldAppInfoList.stream().noneMatch(oldAppInfo -> oldAppInfo.getAppId().equals(addCollectionAppRequest.getAppId()));
            }).collect(Collectors.toList());
            if(ObjectUtil.isNotEmpty(addAppInfoList)){
                addNewAppList(addAppInfoList, updateCollectionAppRequest.getCollectionUuid());
            }
            // 找出oldAppInfoList中有值，但是newAppInfoList中没有的
            List<CollectionAppInfo> deleteAppInfoList = oldAppInfoList.stream().filter(oldAppInfo -> {
                return newAppInfoList.stream().noneMatch(newAppInfo -> newAppInfo.getAppId().equals(oldAppInfo.getAppId()));
            }).collect(Collectors.toList());
            for (CollectionAppInfo deleteAppInfo : deleteAppInfoList) {
                collectionAppMapper.deleteByCollectionUuidAndAppId(deleteAppInfo.getAppId(),updateCollectionAppRequest.getCollectionUuid());
            }
        }
    }

    public void deleteCollectionWxApp(DelCollectionAppRequest request) {
        // 删除小程序信息
        collectionAppMapper.deleteByCollectionAppId(request);
    }

    public void deleteCollection(GetCollectionRequest request) {
        // 删除小程序信息
        List<CollectionAppInfo> collectionAppInfoList = collectionAppMapper.getAppByCollectionUuid(request.getCollectionUuid());
        if(ObjectUtil.isNotEmpty(collectionAppInfoList)){
            collectionAppMapper.deleteByCollectionUuid(request.getCollectionUuid());
            for(CollectionAppInfo collectionAppInfo : collectionAppInfoList){
                fileService.delete(collectionAppInfo.getWxImgUrl());
            }
        }
        // 删除场景信息
        List<SceneInfo> allSceneByCollection = sceneInfoMapper.getAllSceneByCollection(request.getCollectionUuid());
        if(ObjectUtil.isNotEmpty(allSceneByCollection)){
            sceneInfoMapper.deleteByCollectionUuid(request.getCollectionUuid());
            allSceneByCollection.forEach(sceneInfo -> {
                fileService.delete(sceneInfo.getSceneImgUrl());
                fileService.delete(sceneInfo.getArResourceUrl());
            });
        }
        // 删除合集信息
        CollectionInfo collectionInfo = collectionInfoMapper.getCollectionByUuid(request.getCollectionUuid());
        if(ObjectUtil.isNotEmpty(collectionInfo)){
            collectionInfoMapper.deleteByUuid(request.getCollectionUuid());
            fileService.delete(collectionInfo.getCoverImgUrl());
        }
    }
    private void addNewAppList(List<AddCollectionAppRequest> wxAppInfoList,String collectionUuid) throws WxErrorException, MalformedURLException, UnsupportedEncodingException {
        for (AddCollectionAppRequest appInfo : wxAppInfoList) {
            WxAppInfo wxAppInfo = wxAppMapper.getAppByAppId(appInfo.getAppId());
            if(ObjectUtil.isEmpty(wxAppInfo)){
                throw new BizException(BizReturnCode.BIZ_WX_APP_NOT_EXIST);
            }

            CollectionAppInfo collectionAppInfo = new CollectionAppInfo();
            collectionAppInfo.setCollectionUuid(collectionUuid);
            collectionAppInfo.setWxJumpParam(wxAppInfo.getWxJumpParam()+ "?collectionUuid="+collectionUuid);
            collectionAppInfo.setAppId(appInfo.getAppId());
            collectionAppInfo.setWxImgUrl(genWxImgUrl(collectionUuid, wxAppInfo.getAppId(), wxAppInfo.getAppSecret(),wxAppInfo.getWxJumpParam()));

            collectionAppMapper.insert(collectionAppInfo);
        }
    }
    private String genWxImgUrl(String collectionUuid, String appId, String appSecret,String wxScenePage) throws WxErrorException, UnsupportedEncodingException {
        // 生成小程序码
        Map<String, WxMaConfig> configs = new HashMap<>();
        configs.put(appId, new WxMaDefaultConfigImpl() {{
            setAppid(appId);
            setSecret(appSecret);
        }});
        wxMaService.setMultiConfigs(configs);
        WxMaQrcodeService qrcodeService = wxMaService.getQrcodeService();

        String encodedUrl = URLEncoder.encode(wxRequestUrl+"?collectionUuid="+collectionUuid, "UTF-8");
        String sceneParam = wxScenePage+"?url="+encodedUrl;
        WxMaCodeLineColor wxMaCodeLineColor = new WxMaCodeLineColor("0","0","0");
        byte[] qrcodeFileBytes = qrcodeService.createWxaCodeBytes(sceneParam, fileEnv,  1280, false, wxMaCodeLineColor, false);

        String filePath = fileServerPath+"qrcode"+ File.separator;
        String fileName= CommonUtils.getSixRandomNum()+"_"+collectionUuid+"_"+appId+ ".jpg";
        log.info(" wxScenePage:{},sceneParam:{}",wxScenePage,sceneParam);
        FileUtils.createFolder(filePath);
        try (OutputStream os = Files.newOutputStream(Paths.get(filePath+fileName))) {
            os.write(qrcodeFileBytes);
        }catch (FileNotFoundException e){
            throw new BizException(BizReturnCode.BIZ_FILE_IS_EMPTY);
        } catch (IOException e) {
            log.error(String.valueOf(e));
            throw new BizException(BizReturnCode.BIZ_FILE_GENERATE_FAIL);
        }
        String wxImgUrl = fileServerIp+"file"+ File.separator+"qrcode"+ File.separator+fileName;

        FileInfo upload = fileStorageService.of(wxImgUrl)
                .setPath(fileService.getFilePath("qrcode"))
                .setOriginalFilename(fileName)
                .upload();
        if(ObjectUtil.isNotEmpty(upload)){
            log.info("上传云端地址:{}",upload.getUrl());
            wxImgUrl = upload.getUrl();
        }

        log.info("小程序码生成url:{}",wxImgUrl);
        return wxImgUrl;
    }
}