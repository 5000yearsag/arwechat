package com.vr.platform.modules.sys.task;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.modules.ar.entity.ZipFileData;
import com.vr.platform.modules.ar.entity.ZipFileItem;
import com.vr.platform.modules.oss.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class CollectionTask {
    @Resource
    private FileStorageService fileStorageService;

    @Async("taskExecutor_order")
    @Scheduled(cron = "0 0/1 * * * ?")
    public void autoCancelUnpaidOrder() throws Exception {
        log.info("开始执行定时任务 taskExecutor_order");
        String zipFilePath = "C:\\Users\\Admin\\Pictures\\vr\\collection\\234.zip"; // 请替换为实际的文件路径
        String savePath = "C:\\Users\\Admin\\Pictures\\vr\\collection\\temp\\";

        FileUtils.decompressZip(zipFilePath, savePath);
        List<String> fileNameFromDirectory = FileUtils.getFileNameFromDirectory(savePath);
        System.out.println(fileNameFromDirectory);
        File saveFile = new File(savePath);
        if (saveFile.isDirectory()) {
            File[] saveFileList = saveFile.listFiles();
            for (File file : saveFileList) {
                if (file.isDirectory()) {
                    log.error("目录下不允许有文件夹:"+file.getPath());
                }
                if (Objects.equals(FileUtils.getExtension(file.getPath()), "dat")) {
                    String content = Files.readString(Paths.get(file.getPath()), StandardCharsets.UTF_8);
                    System.out.println(content.trim());
                    ZipFileData zipFileData = jsonToConfig(content);
                    if(ObjectUtil.isNotEmpty(zipFileData)
                            && ObjectUtil.isNotEmpty(zipFileData.getLsData())
                            && !zipFileData.getLsData().isEmpty()){
                        for(ZipFileItem zipFileItem : zipFileData.getLsData()){
                            if(ObjectUtil.equals(zipFileItem.getType(),"image")){
                                String zipFileName = FileService.getFileNameFromUrl(zipFileItem.getResPath());
                                fileStorageService.of(savePath+zipFileName)
                                        .setOriginalFilename(zipFileName)
                                        .upload();
                            }
                        }
                    }
                }
            }
        }

    }
    public static ZipFileData jsonToConfig(String jsonData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonData, ZipFileData.class);
        } catch (Exception e) {
            log.info(jsonData);
            log.error("配置文件json转换失败:"+e.getMessage());
        }
        return null;
    }
}
