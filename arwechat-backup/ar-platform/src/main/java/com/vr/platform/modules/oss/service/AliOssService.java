package com.vr.platform.modules.oss.service;

import cn.hutool.core.date.DateTime;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.internal.Mimetypes;
import com.aliyun.oss.model.*;
import com.aliyuncs.exceptions.ClientException;
import com.vr.platform.common.bean.ennum.AllowFileTypeEnums;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.exception.BizException;
import com.vr.platform.common.utils.FileUtils;
import com.vr.platform.config.AliyunConfig;
import com.vr.platform.modules.oss.entity.UploadFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AliOssService {
    @Autowired
    private OSS ossClient;
    @Autowired
    private AliyunConfig aliyunConfig;

    @Value("${file.server.ip}")
    public String fileServerIp;
    @Value("${file.server.path}")
    private String fileServerPath;
    /**
     * @desc 文件上传
     */
    public UploadFileInfo upload(MultipartFile uploadFile,String type) {
        // 校验图片格式
        boolean isLegal = false;
        String suffix = FileUtils.getExtension(uploadFile.getOriginalFilename()).toLowerCase();
        //允许上传图片格式
        List<String> formatList = AllowFileTypeEnums.getAllTypeList();
        if(!formatList.contains(suffix)){
            throw new BizException(BizReturnCode.BIZ_FILE_TYPE_ERROR);
        }
        isLegal = true;

        //封装Result对象，并且将文件的byte数组放置到result对象中
        UploadFileInfo fileUploadResult = new UploadFileInfo();
        if (!isLegal) {
            return fileUploadResult;
        }
        //文件新路径
        String fileName = uploadFile.getOriginalFilename();
        String filePath = getFilePath(fileName,type);
        // 上传到阿里云
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), filePath, new
                    ByteArrayInputStream(uploadFile.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            //上传失败
            return fileUploadResult;
        }
        fileUploadResult.setUrl(this.aliyunConfig.getUrlPrefix() + filePath);
        return fileUploadResult;
    }
    /**
     * @desc 生成路径以及文件名 例如：//images/2019/04/28/15564277465972939.jpg
     */
    private String getFilePath(String fileName,String type) {
        DateTime dateTime = new DateTime();
        return type + "/"
                + dateTime.toString("yyMMdd")+"/"+fileName;
    }
    /**
     * @desc 查看文件列表
     */
    public List<OSSObjectSummary> list() {
        // 设置最大个数。
        final int maxKeys = 200;
        // 列举文件。
        ObjectListing objectListing = ossClient.listObjects(new ListObjectsRequest(aliyunConfig.getBucketName()).withMaxKeys(maxKeys));
        List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
        return sums;
    }

    /**
     * @desc 下载文件
     */
    public InputStream exportOssFile(String objectName) {
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        OSSObject ossObject = ossClient.getObject(aliyunConfig.getBucketName(), objectName);
        // 读取文件内容。
        InputStream is = ossObject.getObjectContent();
        return is;
    }

    public String saveAliOss(MultipartFile uploadFile,String saveFilePath,String sourceFileName) {
        String filePath = saveFilePath+sourceFileName;
        // 上传到阿里云
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), filePath,
                    new ByteArrayInputStream(uploadFile.getBytes()));
        } catch (Exception e) {
            log.error("上传阿里云失败", e);
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_OSS_FAIL);
        }
        return aliyunConfig.getUrlPrefix() + filePath;
    }

    public String saveAliOssMultipart(String saveFilePath,String sourceFileName) throws ClientException {
        String objectName = saveFilePath+sourceFileName;
        String bucketName = aliyunConfig.getBucketName();
        String filePath = fileServerPath+objectName;
        // 上传到阿里云
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.
                newDefaultCredentialProvider(aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        OSS ossClient = new OSSClientBuilder().build(aliyunConfig.getEndpoint(), credentialsProvider);

        try {
            ossClient.setBucketTransferAcceleration(bucketName, true);
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
            ObjectMetadata metadata = new ObjectMetadata();
            // 根据文件自动设置ContentType。如果不设置，ContentType默认值为application/oct-srream。
            if (metadata.getContentType() == null) {
                metadata.setContentType(Mimetypes.getInstance().getMimetype(new File(filePath), objectName));
            }
            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId。
            String uploadId = upresult.getUploadId();
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = 1 * 1024 * 1024L;   //1 MB。

            // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
            final File sampleFile = new File(filePath);
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                // 设置上传的分片流。
                // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定数据。
                InputStream instream = new FileInputStream(sampleFile);
                instream.skip(startPos);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber( i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }

            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            log.info(completeMultipartUploadResult.getETag());
        } catch (Exception e) {
            log.error("上传阿里云失败", e);
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_OSS_FAIL);
        } finally {
            if (ossClient != null){
                ossClient.shutdown();
            }
        }
        return aliyunConfig.getUrlPrefix() + filePath;
    }


    public String saveAliOss(String localFilePath,String type,String fileName) {
        String filePath = getFilePath(type)+fileName;
        // 上传到阿里云
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(localFilePath));
            ossClient.putObject(aliyunConfig.getBucketName(), filePath,inputStream);
        } catch (Exception e) {
            log.error("上传阿里云失败", e);
            throw new BizException(BizReturnCode.BIZ_FILE_UPLOAD_OSS_FAIL);
        }
        return aliyunConfig.getUrlPrefix() + filePath;
    }

    /**
     * @desc 删除文件
     */
    public UploadFileInfo delete(String url) {
        String objectName = url.substring(aliyunConfig.getUrlPrefix().length());
        ossClient.deleteObject(aliyunConfig.getBucketName(), objectName);
        UploadFileInfo fileUploadResult = new UploadFileInfo();
        fileUploadResult.setUrl(objectName);
        return fileUploadResult;
    }

    private String getFilePath(String type) {
        DateTime dateTime = new DateTime();
        return type + "/"
                + dateTime.toString("yyMMdd")+"/";
    }
}
