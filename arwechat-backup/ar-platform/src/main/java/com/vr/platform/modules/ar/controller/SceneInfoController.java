package com.vr.platform.modules.ar.controller;

import com.github.pagehelper.PageInfo;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.modules.ar.entity.SceneInfo;
import com.vr.platform.modules.ar.entity.request.*;
import com.vr.platform.modules.ar.service.SceneInfoService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/scene")
@AllArgsConstructor
@Api(value = "CollectionInfo", tags = "场景管理")
public class SceneInfoController {

    @Autowired
    private SceneInfoService sceneInfoService;

    @PostMapping("/getScene")
    public ResponseFormat<SceneInfo> getScene(@RequestBody GetSceneRequest request) {
        log.info("findById:{}", request.getSceneUuid());
        return ResponseFormat.success(sceneInfoService.getScene(request.getSceneUuid()));
    }

    @PostMapping(value = "/getAllSceneByCollection")
    public ResponseFormat<PageInfo<SceneInfo>> getAllSceneByCollection(@RequestBody GetAllSceneRequest request) {
        log.info("find All sceneInfo by collection");
        return ResponseFormat.success(sceneInfoService.getAllScene(request));
    }

    @PostMapping(value = "/addScene")
    public ResponseFormat<String> addScene(@RequestBody AddSceneRequest addSceneRequest) throws WxErrorException, IOException {
        log.info("add sceneInfo");
        return ResponseFormat.success(sceneInfoService.addScene(addSceneRequest));
    }

    @PostMapping(value = "/updateScene")
    public ResponseFormat updateScene(@RequestBody SceneInfo sceneInfo) {
        log.info("update sceneInfo");
        sceneInfoService.updateScene(sceneInfo);
        return ResponseFormat.success();
    }

    @PostMapping(value = "/changeSceneStatus")
    public ResponseFormat changeSceneStatus(@RequestBody UpdateSceneStatusRequest updateRequest) {
        log.info("update sceneInfo");
        sceneInfoService.changeSceneStatus(updateRequest);
        return ResponseFormat.success();
    }

    @PostMapping("/deleteScene")
    public ResponseFormat deleteScene(@RequestBody GetSceneRequest request) {
        log.info("delete sceneInfo");
        sceneInfoService.delete(request.getSceneUuid());
        return ResponseFormat.success();
    }
}
