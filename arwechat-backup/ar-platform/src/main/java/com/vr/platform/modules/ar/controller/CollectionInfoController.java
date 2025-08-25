package com.vr.platform.modules.ar.controller;

import com.github.pagehelper.PageInfo;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.modules.ar.entity.CollectionInfo;
import com.vr.platform.modules.ar.entity.request.*;
import com.vr.platform.modules.ar.entity.response.GetCollectionRes;
import com.vr.platform.modules.ar.service.CollectionInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

@Slf4j
@RestController
@RequestMapping("/api/collection")
@AllArgsConstructor
@Api(value = "CollectionInfo", tags = "合集管理")
public class CollectionInfoController {

    @Autowired
    private CollectionInfoService collectionInfoService;

    @ApiOperation(value = "根据合集uuid获取合集信息", notes = "根据合集uuid获取合集信息")
    @PostMapping("/getCollectionByUuid")
    public ResponseFormat<GetCollectionRes> getCollectionByUuid(@RequestBody GetCollectionRequest request) {
        log.info("getCollectionByUuid:{}", request.getCollectionUuid());
        return ResponseFormat.success(collectionInfoService.getCollection(request.getCollectionUuid()));
    }

    @ApiOperation(value = "获取所有合集信息", notes = "获取所有合集信息")
    @PostMapping("/getAllCollection")
    public ResponseFormat<PageInfo<GetCollectionRes>> getAllCollection(@RequestBody GetAllCollectionRequest request) {
        log.info("getAllCollection:{}", request.toString());
        return ResponseFormat.success(collectionInfoService.getAllCollection(request));
    }

    @ApiOperation(value = "新增合集", notes = "新增合集")
    @PostMapping("/addCollection")
    public ResponseFormat<String> addCollection(@RequestBody AddCollectionRequest addCollectionRequest, HttpServletRequest request) throws WxErrorException, IOException {
        log.info("addCollection:{}", addCollectionRequest.toString());
        return collectionInfoService.addCollection(addCollectionRequest);
    }

    @ApiOperation(value = "更新合集基本信息", notes = "更新合集基本信息")
    @PostMapping(value = "/updateCollectionBaseInfo")
    public ResponseFormat updateCollectionBaseInfo(@RequestBody CollectionInfo collectionInfo) {
        log.info("updateCollectionBaseInfo:{}", collectionInfo.toString());
        collectionInfoService.updateCollectionBaseInfo(collectionInfo);
        return ResponseFormat.success();
    }

    @ApiOperation(value = "删除合集", notes = "删除合集")
    @PostMapping(value = "/deleteCollection")
    public ResponseFormat deleteCollection(@RequestBody GetCollectionRequest request) {
        log.info("deleteCollection:{}", request.toString());
        collectionInfoService.deleteCollection(request);
        return ResponseFormat.success();
    }

    @ApiOperation(value = "删除合集小程序信息", notes = "删除合集小程序信息")
    @PostMapping(value = "/deleteCollectionWxApp")
    public ResponseFormat deleteCollectionWxApp(@RequestBody DelCollectionAppRequest request) {
        log.info("deleteCollectionWxApp:{}", request.toString());
        collectionInfoService.deleteCollectionWxApp(request);
        return ResponseFormat.success();
    }

    @ApiOperation(value = "更新合集小程序信息", notes = "更新合集小程序信息")
    @PostMapping(value = "/updateCollectionWxApp")
    public ResponseFormat updateCollectionWxApp(@RequestBody UpdateCollectionAppRequest updateCollectionAppRequest) throws MalformedURLException, WxErrorException, UnsupportedEncodingException {
        log.info("updateCollectionWxApp:{}", updateCollectionAppRequest.toString());
        collectionInfoService.updateCollectionWxApp(updateCollectionAppRequest);
        return ResponseFormat.success();
    }

    @ApiOperation(value = "修改合集", notes = "修改合集")
    @PostMapping("/updateCollection")
    public ResponseFormat updateCollection(@RequestBody UpdateCollectionRequest addCollectionRequest, HttpServletRequest request) throws WxErrorException, IOException {
        log.info("updateCollection:{}", addCollectionRequest.toString());
        collectionInfoService.updateCollection(addCollectionRequest);
        return ResponseFormat.success();
    }
}
