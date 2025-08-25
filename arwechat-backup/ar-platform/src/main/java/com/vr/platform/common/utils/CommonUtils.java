package com.vr.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class CommonUtils {

    public static String getCurrentTimeStamp(){
        //获取当前时间戳
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
        return Instant.now()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter);
    }

    public static void main(String[] args) {
        System.out.println(getCurrentTimeStamp());
    }

    public static synchronized String getSixRandomNum() {
        //4位随机数
        String randomNumeric = RandomStringUtils.randomNumeric(6);
        log.info("6位随机数:{}", randomNumeric);
        return randomNumeric;
    }

    public static String bufferedImageToBase64(BufferedImage bufferedImage) {
        // 创建一个字节输出流
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // 将BufferedImage写入到字节输出流中，使用JPEG格式
            ImageIO.write(bufferedImage, "jpg", stream);
            // 将流中的数据转换为字节数组
            byte[] imageBytes = stream.toByteArray();
            // 使用Base64进行编码
            Base64.Encoder encoder = Base64.getEncoder();
            // 编码后的字节转换为字符串
            return encoder.encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding image to Base64", e);
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }
}
