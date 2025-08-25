package com.vr.platform.modules.ar.controller;

import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.modules.ar.entity.WxAppInfo;
import com.vr.platform.modules.ar.entity.request.AddWxAppRequest;
import com.vr.platform.modules.ar.entity.request.GetWxAppRequest;
import com.vr.platform.modules.ar.entity.request.UpdateWxAppRequest;
import com.vr.platform.modules.ar.service.WxAppService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wxapp")
@RequiredArgsConstructor
@Api(value = "wxQrcode", tags = "小程序码")
public class WxAppController {

    @Resource
    private WxAppService wxAppService;


    @GetMapping(value = "/getAllWxAppList")
    public ResponseFormat<List<WxAppInfo>> getAllWxAppList() {
        log.info("find wx app");
        return ResponseFormat.success(wxAppService.getAllWxAppList());
    }

    @PostMapping(value = "/getAppByAppId")
    public ResponseFormat<WxAppInfo> getAppByAppId(@RequestBody GetWxAppRequest request) {
        log.info("find wx app");
        return ResponseFormat.success(wxAppService.getAppByAppId(request));
    }

    @PostMapping(value = "/addApp")
    public ResponseFormat addScene(@RequestBody AddWxAppRequest appRequest) {
        log.info("add wx app");
        wxAppService.addWxApp(appRequest);
        return ResponseFormat.success();
    }

    @PostMapping(value = "/updateApp")
    public ResponseFormat updateApp(@RequestBody UpdateWxAppRequest appRequest) {
        log.info("update wx app");
        wxAppService.updateApp(appRequest);
        return ResponseFormat.success();
    }

    @PostMapping("/deleteApp")
    public ResponseFormat deleteApp(@RequestBody GetWxAppRequest request) {
        log.info("delete wx app");
        wxAppService.deleteApp(request.getId());
        return ResponseFormat.success();
    }

}
