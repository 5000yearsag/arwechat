package com.vr.platform.modules.oss.controller;

import com.vr.platform.common.base.AbstractController;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.modules.oss.entity.UploadFileInfo;
import com.vr.platform.modules.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/sys/file")
@AllArgsConstructor
@Api(value = "FileController", tags = "文件上传")
public class FileController extends AbstractController {
    @Resource
    private FileService fileService;

    @ApiOperation(value = "上传场景文件", notes = "上传场景文件")
    @PostMapping(value = "/uploadSceneFile")
    public ResponseFormat<UploadFileInfo> uploadSceneFile(@RequestPart("file") MultipartFile file) throws Exception {
        log.info("上传图片");
        return ResponseFormat.success(fileService.uploadFile(file,"scene"));
    }

    @ApiOperation(value = "上传合集图片", notes = "上传合集图片")
    @PostMapping(value = "/uploadCollectionFile")
    public ResponseFormat<UploadFileInfo> uploadCollectionFile(@RequestPart("file") MultipartFile file) throws Exception {
        log.info("上传图片");
        return ResponseFormat.success(fileService.uploadFile(file,"collection"));
    }

}
