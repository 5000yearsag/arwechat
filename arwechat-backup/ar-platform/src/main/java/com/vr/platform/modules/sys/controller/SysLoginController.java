package com.vr.platform.modules.sys.controller;

import cn.hutool.core.util.ObjectUtil;
import com.google.code.kaptcha.Producer;
import com.vr.platform.common.annotation.AuthIgnore;
import com.vr.platform.common.base.AbstractController;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.utils.CommonUtils;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.common.utils.MemCacheUtils;
import com.vr.platform.modules.sys.entity.GetCaptchaReq;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * 登录相关
 *
 * @author czx
 * @email object_czx@163.com
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/sys")
@Api(value = "SysLoginController", tags = "登录相关")
public class SysLoginController extends AbstractController {

    private final Producer producer;

    @AuthIgnore
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseFormat hello() {
        return ResponseFormat.success("hello welcome to use x-springboot");
    }

    /**
     * 验证码
     */
    @AuthIgnore
    @SneakyThrows
    @PostMapping(value = "/code")
    public ResponseFormat<String> captcha(@RequestBody GetCaptchaReq getCaptchaReq, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //生成文字验证码
        String text = producer.createText();
        log.info("验证码:{}", text);
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        //redis 60秒
        MemCacheUtils.set(Constant.NUMBER_CODE_KEY +getCaptchaReq.getRandomStr(),text , TimeUnit.SECONDS, 60);

        String base64Str = CommonUtils.bufferedImageToBase64(image);
        return ResponseFormat.success(base64Str);
    }

    /**
     * 退出
     */
    @AuthIgnore
    @GetMapping(value = "/logout")
    public ResponseFormat logout(HttpServletRequest request) {
        String token = request.getHeader(Constant.TOKEN);
        Object userInfo = MemCacheUtils.get(Constant.AUTHENTICATION_TOKEN + token);
        if (ObjectUtil.isNotNull(userInfo)) {
            MemCacheUtils.delete(Constant.AUTHENTICATION_TOKEN + token);
        }
        return ResponseFormat.fail(BizReturnCode.BIZ_USER_LOGOUT);
    }
}
