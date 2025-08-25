package com.vr.platform.modules.sys.controller;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.common.utils.MemCacheUtils;
import com.vr.platform.modules.ar.entity.ZipFileData;
import com.vr.platform.modules.ar.entity.ZipFileItem;
import com.vr.platform.modules.oss.service.FileService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.vr.platform.common.utils.MemCacheUtils.printSize;

@Slf4j
public class Test {
//    public static void main(String[] args) {
//        String a = new String("1");
//        String b = new String("1");
//        System.out.println(a.equals(b));
//        System.out.println(a==b);
//    }
//    public static void main(String[] args) {
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String password = passwordEncoder.encode("81dc9bdb52d04dc20036dbd8313ed055");
//        System.out.println(password);
//    }
//public static void main(String[] args) throws InterruptedException {
//    MemCacheUtils.set("aaa","bb", TimeUnit.SECONDS, 5);
//    log.info(MemCacheUtils.get("aaa"));
//    MemCacheUtils.set("bb","bab",TimeUnit.SECONDS, 5);
//    log.info(MemCacheUtils.get("bb"));
//
//    MemCacheUtils.set("cc","bdb",TimeUnit.SECONDS, 5);
//    log.info(MemCacheUtils.get("cc"));
//
//    TimeUnit.SECONDS.sleep(6);
//    log.info(MemCacheUtils.get("aaa"));
//    log.info(MemCacheUtils.get("bb"));
//    log.info(MemCacheUtils.get("cc"));
//
//    MemCacheUtils.printSize();
//
//    System.exit(0);
//}
    @Data
    public static class Apple {
        private String id;
        private String color;
    }
    @Data
    public static class Banana {
        private String id;
        private String name;
    }


//    public static void main(String[] args) {
//        List<Apple> appleList = new ArrayList<>();
//        Apple apple1 = new Apple();
//        apple1.setId("1");
//        appleList.add(apple1);
//        Apple apple2 = new Apple();
//        apple2.setId("2");
//        appleList.add(apple2);
//
//        System.out.println(appleList);
//
//        List<Banana> bnanaList = new ArrayList<>();
//        Banana banana1 = new Banana();
//        banana1.setId("1");
//        bnanaList.add(banana1);
//        Banana banana2 = new Banana();
//        banana2.setId("3");
//        bnanaList.add(banana2);
//
//        List<Apple> collect = appleList.stream().filter(apple -> apple.getId().equals("1")).collect(Collectors.toList());
//        List<Apple> collect1 = appleList.stream().filter(apple -> {
//            return bnanaList.stream().noneMatch(banana -> banana.getId().equals(apple.getId()));
//        }).collect(Collectors.toList());
//        System.out.println(collect1);
//
//    }

    public static void main(String[] args) throws IOException {
        String zipFilePath = "C:\\Users\\Admin\\Pictures\\vr\\collection\\234.zip"; // 请替换为实际的文件路径
        String savePath = "C:\\Users\\Admin\\Pictures\\vr\\collection\\temp\\";

        FileUtils.decompressZip(zipFilePath, savePath);
        List<String> fileNameFromDirectory = FileUtils.getFileNameFromDirectory(savePath);
        System.out.println(fileNameFromDirectory);
        File saveFile = new File(savePath);
        FileStorageService fileStorageService = new FileStorageService();
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
