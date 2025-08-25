package com.vr.platform.common.utils;

import com.vr.platform.common.bean.ennum.AllowFileTypeEnums;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.exception.BizException;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class FileUtils {

    public static String getUrl(HttpServletRequest request) {
        String port = "";
        if(request.getServerPort() != 80 && request.getServerPort() !=443){
            port = ":"+request.getServerPort();

        }
        return request.getScheme()+"://"+request.getServerName()+port+request.getContextPath()+"/";
    }

    public static boolean validateFileSuffix(String fileName, List<String> formatList){
        //取得文件后缀
        String suffix = getExtension(fileName);
        for(String format : formatList) {
            String[] fileType_array = format.split("/");
            if(fileType_array != null && fileType_array.length >0){
                for(String fileType : fileType_array){
                    if(fileType.equalsIgnoreCase(suffix)){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static String getExtension(String path){
        if(path != null && !"".equals(path.trim())){
            String extension = FilenameUtils.getExtension(path);
            if(extension != null && !"".equals(extension.trim())){//后缀名改为小写
                extension = extension.toLowerCase();
            }
            return extension;
        }
        return "";

    }

    public static void writeFile(String path, String fileName, MultipartFile inputFile){
        FileOutputStream outStream = null;
        try {
            //文件输出流
            outStream = new FileOutputStream(new File(path, fileName));
            //写入硬盘
            int count;
            byte [] bufferedbytes= new byte[1024];
            BufferedInputStream inputStream= new BufferedInputStream(inputFile.getInputStream());
            while ((count = inputStream.read(bufferedbytes))!=-1){
                outStream.write(bufferedbytes, 0, count);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("写文件",e);
            }
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_FAIL);
        }finally{
            if(outStream != null){
                try {
                    outStream.close();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error("写文件",e);
                    }
                }
            }
        }
    }

    // 从A路径拷贝文件到B路径
    public static void copyFile(String sourceFile,String targetFile){
        Path sourcePath = Paths.get(sourceFile);
        Path targetPath = Paths.get(targetFile);
        try {
            Files.copy(sourcePath, targetPath, REPLACE_EXISTING);
            log.info("文件复制成功from:{} to:{}",sourcePath,targetPath);
            System.out.println("文件复制成功");
        } catch (Exception e) {
            System.err.println("文件复制失败: " + e.getMessage());
        }
    }

    public static String getFileNameFromUrl(String urlStr) throws MalformedURLException {
        URL url = new URL(urlStr);
        String fileName = url.getPath();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }


    public static Boolean createFolder(String path){
        //生成文件目录
        File dirFile  = new File(path);
        if (!(dirFile.exists()) && !(dirFile.isDirectory())){
            dirFile.mkdirs();
            return true;
        }
        return false;
    }

    public static String getFileDimension(String filePath) {
        if(filePath == null || filePath.trim().isEmpty()){
            log.error("文件路径不能为空");
        }
        log.info("GetFileDimension 文件路径："+filePath);
        String dimensions = "";
        IContainer container = IContainer.make();
        int result = container.open(filePath, IContainer.Type.READ, null);
        if (result > 0){
            log.info("GetFileDimension 文件打开成功");
            int numStreams = container.getNumStreams();
            for (int i = 0; i < numStreams; i++) {
                IStream stream = container.getStream(i);
                IStreamCoder coder = stream.getStreamCoder();
                if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                    dimensions = coder.getWidth() + "*" + coder.getHeight();
                }
            }

        }
        container.close();
        return dimensions;
    }


    /**
     * 把zip文件解压到指定的文件夹
     * @param zipFilePath   zip文件路径, 如 "D:/test/aa.zip"
     * @param saveFileDir   解压后的文件存放路径, 如"D:/test/"
     */
    public static void decompressZip(String zipFilePath,String saveFileDir) throws IOException {
        if(isEndsWithZip(zipFilePath)) {
            createFolder(saveFileDir);
            Path destDirPath = Paths.get(saveFileDir);
            // Ensure the destination directory exists
            if (Files.notExists(destDirPath)) {
                Files.createDirectories(destDirPath);
            }

            try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
                ZipEntry entry = zipIn.getNextEntry();
                while (entry != null) {
                    Path filePath = destDirPath.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    } else {
                        // If it's a file, extract it
                        Files.createDirectories(filePath.getParent());
                        Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
        }
    }

    /**
     * 判断文件名是否以.zip为后缀
     * @param fileName        需要判断的文件名
     * @return 是zip文件返回true,否则返回false
     */
    public static boolean isEndsWithZip(String fileName) {
        boolean flag = false;
        if(fileName != null && !"".equals(fileName.trim())) {
            if(fileName.endsWith(".ZIP")||fileName.endsWith(".zip")){
                flag = true;
            }
        }
        return flag;
    }
    public static List<String> getFileNameFromDirectory(String path){
        List<String> fileNameList = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            //获取文件夹下的文件
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    log.error("目录下不允许有文件夹:"+files[i].getPath());
                    throw new BizException(BizReturnCode.BIZ_REQUEST_PARAM_ERROR,"目录下不允许有文件夹:"+files[i].getPath());
                } else {
                    fileNameList.add(files[i].getName());
                }
            }
        } else {
            //这是一个文件
            fileNameList.add(file.getName());
        }
        return fileNameList;
    }

    public static String getDomainFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getProtocol()+"//"+url.getHost();
        } catch (MalformedURLException e) {
            // Handle the exception appropriately
            log.error("Invalid URL: " + urlString);
            return "";
        }
    }

    public static String getPathFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getPath();
        } catch (MalformedURLException e) {
            // Handle the exception appropriately
            log.error("Invalid URL: " + urlString);
            return "";
        }
    }
}
