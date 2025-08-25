package com.vr.platform.interceptor;

import cn.hutool.core.util.StrUtil;
import com.vr.platform.common.exception.CustomAuthenticationException;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.common.utils.MemCacheUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.AntPathMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ValidateCodeFilter extends OncePerRequestFilter {

    private AntPathMatcher pathMatcher = new AntPathMatcher();
    private AuthenticationFailureHandler authenticationFailureHandler;

    public ValidateCodeFilter(AuthenticationFailureHandler authenticationFailureHandler){
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String url = request.getRequestURI();
        if (pathMatcher.match(Constant.TOKEN_ENTRY_POINT_URL,url)){
            String captcha = request.getParameter("captcha");
            String randomStr = request.getParameter("randomStr");

            if(StrUtil.isBlank(captcha) || StrUtil.isBlank(randomStr)){
                CustomAuthenticationException exception = new CustomAuthenticationException("验证码为空");
                authenticationFailureHandler.onAuthenticationFailure(request,response,exception);
                return;
            }

            String code_key = MemCacheUtils.get(Constant.NUMBER_CODE_KEY + randomStr);
            if(StrUtil.isEmpty(code_key)){
                CustomAuthenticationException exception = new CustomAuthenticationException("验证码过期");
                authenticationFailureHandler.onAuthenticationFailure(request,response,exception);
                return;
            }

            if(!captcha.equalsIgnoreCase(code_key)){
                CustomAuthenticationException exception = new CustomAuthenticationException("验证码不正确");
                authenticationFailureHandler.onAuthenticationFailure(request,response,exception);
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}
