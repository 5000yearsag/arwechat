package com.vr.platform.authentication.handler;

import cn.hutool.core.util.ObjectUtil;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.common.utils.MemCacheUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutHandler {

    @SneakyThrows
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = request.getHeader(Constant.TOKEN);
//        Object userInfo = redisTemplate.opsForValue().get(Constant.AUTHENTICATION_TOKEN + token);
        Object userInfo = MemCacheUtils.get(Constant.AUTHENTICATION_TOKEN + token);
        if (ObjectUtil.isNotNull(userInfo)) {
            String user[] = userInfo.toString().split(",");
            MemCacheUtils.delete(Constant.AUTHENTICATION_TOKEN + token);
        }
//        redisTemplate.delete(Constant.AUTHENTICATION_TOKEN + token);
    }
}
