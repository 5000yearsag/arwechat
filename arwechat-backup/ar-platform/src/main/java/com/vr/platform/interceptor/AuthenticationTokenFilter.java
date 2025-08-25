package com.vr.platform.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.platform.authentication.detail.CustomUserDetailsService;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.bean.response.ReturnCode;
import com.vr.platform.common.utils.Constant;
import com.vr.platform.common.utils.MemCacheUtils;
import com.vr.platform.common.utils.SpringContextUtils;
import com.vr.platform.config.AuthIgnoreConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
public class AuthenticationTokenFilter extends BasicAuthenticationFilter {

    private AuthIgnoreConfig authIgnoreConfig;
    private ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationTokenFilter(AuthenticationManager authenticationManager, AuthIgnoreConfig authIgnoreConfig) {
        super(authenticationManager);
        this.authIgnoreConfig = authIgnoreConfig;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher(authIgnoreConfig.getIgnoreUrls().toString());
        String token = request.getHeader(Constant.TOKEN);
        if (StrUtil.isBlank(token) || StrUtil.equals(token, "null")) {
            token = request.getParameter(Constant.TOKEN);
        }



        if (StrUtil.isNotBlank(token) && !StrUtil.equals(token, "null")) {
            final String requestURI = request.getRequestURI();

            if(!authIgnoreConfig.isContains(requestURI) && !requestURI.contains(Constant.TOKEN_ENTRY_POINT_URL)){
//                Object userInfo = redisTemplate.opsForValue().get(Constant.AUTHENTICATION_TOKEN + token);
                Object userInfo = MemCacheUtils.get(Constant.AUTHENTICATION_TOKEN + token);
                if (ObjectUtil.isNull(userInfo)) {
                    writer(response, "无效token");
                    return;
                }
                String user[] = userInfo.toString().split(",");
                if (user == null || user.length != 2) {
                    writer(response, "无效token");
                    return;
                }

                String userId = user[0];
                CustomUserDetailsService customUserDetailsService = SpringContextUtils.getBean(CustomUserDetailsService.class);
                UserDetails userDetails = customUserDetailsService.loadUserByUserId(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }


    @SneakyThrows
    public void writer(HttpServletResponse response, String msg) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(ResponseFormat.fail(ReturnCode.UNAUTHORIZED, msg)));
    }
}
