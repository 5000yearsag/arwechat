package com.vr.platform.common.service;

import cn.hutool.core.util.ObjectUtil;
import com.vr.platform.common.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;

@Service
public class CommonService {

    @Value("${file.server.path}")
    private String fileServerPath;

    public void oldBackupFile(String url,String collectionUuid,String type) throws MalformedURLException {
        String fileName = FileUtils.getFileNameFromUrl(url);
        String targetFilePath =fileServerPath+File.separator+"backup"+File.separator;
        String sourceFilePath = fileServerPath+ File.separator;
        if(ObjectUtil.isNotEmpty(type)){
            targetFilePath = targetFilePath+collectionUuid+File.separator+type;
            sourceFilePath = sourceFilePath+type+File.separator+fileName;
        }else {
            targetFilePath = targetFilePath+collectionUuid;
            sourceFilePath = sourceFilePath+"collection"+File.separator+fileName;
        }
        FileUtils.createFolder(targetFilePath);
        targetFilePath = targetFilePath + File.separator+fileName;
        FileUtils.copyFile(sourceFilePath,targetFilePath);
    }
}
