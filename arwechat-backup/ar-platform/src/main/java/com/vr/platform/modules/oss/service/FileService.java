package com.vr.platform.modules.oss.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.internal.Mimetypes;
import com.aliyun.oss.model.*;
import com.aliyuncs.exceptions.ClientException;
import com.vr.platform.common.bean.ennum.AllowFileTypeEnums;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.utils.CommonUtils;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.config.AliyunConfig;
import com.vr.platform.modules.oss.entity.UploadFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileService {
    @Autowired
    private FileStorageService fileStorageService;//注入实列

    @Value("${file.server.ip}")
    public String fileServerIp;
    @Value("${file.server.path}")
    private String fileServerPath;


    public UploadFileInfo uploadFile(MultipartFile uploadFile,String type) throws Exception {
        //开始时间
        long startTime = System.currentTimeMillis();
        UploadFileInfo uploadFileInfo = new UploadFileInfo();
        if(uploadFile == null || uploadFile.isEmpty()){
            throw new BizException(BizReturnCode.BIZ_FILE_IS_EMPTY);
        }
        //当前文件名称
        String sourceFileName = CommonUtils.getSixRandomNum()+"_"+uploadFile.getOriginalFilename();
        //取得文件后缀
        String suffix = FileUtils.getExtension(sourceFileName).toLowerCase();
        //允许上传图片格式
        List<String> formatList = AllowFileTypeEnums.getAllTypeList();
        if(!formatList.contains(suffix)){
            log.error("文件格式错误："+suffix);
            throw new BizException(BizReturnCode.BIZ_FILE_TYPE_ERROR);
        }
        //文件大小
        long size = uploadFile.getSize();
        long imageSize = 100000L; //100MB
        if(size > imageSize*1024L){
            log.error("文件大小超过限制："+size/1024L+"MB");
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_SIZE_EXCEEDS_LIMIT,"单个上传文件不能超过100M");
        }
        String savePath = getFilePath(type);
        //保存本地时间
        long saveLocalTime = System.currentTimeMillis();
        log.info("保存开始前耗时："+(System.currentTimeMillis()-startTime));
        UploadFileInfo localInfo = saveLocal(uploadFile, savePath, sourceFileName);
        uploadFileInfo.setDimensions(localInfo.getDimensions());

        long saveAliOssTime = System.currentTimeMillis();
        log.info("文件上传服务器耗时："+(System.currentTimeMillis()-saveLocalTime));
        uploadFileInfo.setUrl(saveCloud(uploadFile,savePath,sourceFileName));
        log.info("文件上传阿里耗时："+(System.currentTimeMillis()-saveAliOssTime));

        return uploadFileInfo;
    }

    public UploadFileInfo saveLocal(MultipartFile uploadFile,String saveFilePath,String sourceFileName) throws Exception {
        UploadFileInfo uploadFileInfo = new UploadFileInfo();
        String filePath = fileServerPath+saveFilePath;
//        //生成文件保存目录
        FileUtils.createFolder(filePath);
        FileUtils.writeFile(filePath, sourceFileName,uploadFile);
        uploadFileInfo.setUrl(fileServerIp+sourceFileName);
        uploadFileInfo.setDimensions(FileUtils.getFileDimension(filePath+sourceFileName));
        return uploadFileInfo;
    }

    public String saveCloud(MultipartFile uploadFile,String saveFilePath,String sourceFileName) throws Exception {
        log.info("文件上传阿里开始 saveFilePath is {},sourceFileName is {}",saveFilePath,sourceFileName);
        FileInfo fileInfo = fileStorageService.of(uploadFile)
                .setPath(saveFilePath) //保存到相对路径下，为了方便管理，不需要可以不写
                .setSaveFilename(sourceFileName)
                .upload();  //将文件上传到对应地方
        if(ObjectUtil.isEmpty(fileInfo)){
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_CLOUD_FAIL);
        }
        return fileInfo.getUrl();
    }

    public void delete(String url) {
        if(ObjectUtil.isEmpty(url)) return;
        FileStorage fileStorage = fileStorageService.getFileStorage();
        FileInfo fileInfo = new FileInfo()
                .setPlatform(fileStorage.getPlatform())
                .setBasePath(getBasePathFromUrl(url))
                .setPath(getFilePathFromUrl(url))
                .setFilename(getFileNameFromUrl(url));
        fileStorageService.delete(fileInfo);
    }

    public String getFilePath(String type) {
        DateTime dateTime = new DateTime();
        return type + "/"
                + dateTime.toString("yyMMdd")+"/";
    }
    private static String getFilePathFromUrl(String url){
        String path = FileUtils.getPathFromUrl(url);
        String filename = getFileNameFromUrl(url);
        path = path.replace(filename,"");
        return path.substring(path.indexOf("/",2)+1);
    }
    private static String getBasePathFromUrl(String url){
        String path = FileUtils.getPathFromUrl(url);
        System.out.println(path.indexOf("/",2));
        return path.substring(1,path.indexOf("/",1))+"/";
    }
    public static String getFileNameFromUrl(String url){
        return url.substring(url.lastIndexOf("/")+1);
    }

    /**
     * 处理输入的维度字符串，将宽度除以2
     * @param dimensions 原始的宽*高字符串
     * @return 修改后的宽*高字符串
     */
    public String processDimensions(String dimensions,String... type) {
        // 以 '*' 分隔字符串
        String[] parts = dimensions.split("\\*");

        // 确保字符串有正确的格式
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid dimensions format. Expected 'width*height'.");
        }

        // 解析宽度和高度
        int width = Integer.parseInt(parts[0].trim());
        int height = Integer.parseInt(parts[1].trim());

        // 计算新的宽度
        int newWidth;
        if(ObjectUtil.isNotEmpty(type)){
            newWidth = width * 2;
        }else {
            newWidth = width / 2;
        }
        // 生成新的维度字符串
        return newWidth + "*" + height;
    }

}
