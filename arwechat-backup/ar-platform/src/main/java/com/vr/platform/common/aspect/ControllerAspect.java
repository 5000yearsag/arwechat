package com.vr.platform.common.aspect;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangchenyang
 * @date 2022/4/4 0004
 */
@Component
@Aspect
@Slf4j
public class ControllerAspect {

    @AfterReturning("execution(* com.vr.platform.modules..*Controller.*(..))")
    public void logServiceAccess(JoinPoint joinPoint){
        log.info("Completed: " + joinPoint);
    }

    @Pointcut("execution(* com.vr.platform..*Controller.*(..))")
    private void pointCutMethod(){

    }

    /**
     * 声明环绕通知
     */
    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        long begin = System.nanoTime();
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        // 获取请求地址
        String ip = getIpAddr(request);
        Map<String, String> headersInfo = getHeadersInfo(request);
        Gson gson = new Gson();
        log.info("\n[访问方法开始]>>>>>：{}，\n[I P]>>>>>：{}，\n[Headers]>>>>>：{}，\n[入参]>>>>>：{}", pjp.getSignature().toString(), ip, gson.toJson(headersInfo),Arrays.toString(pjp.getArgs()));
        Object obj = pjp.proceed();
        long end = System.nanoTime();
        if (!StringUtils.isEmpty(obj)) {
            log.info("\n[访问方法结束]>>>>>：{}，\n[返参]>>>>>：{}，\n[耗时]>>>>>：{}毫秒", pjp.getSignature().toString(), gson.toJson(obj.toString()), (end - begin) / 1000000);
            return obj;
        }
        return obj;
    }



    /**
     * 获取当前网络ip
     * @param request
     * @return
     */
    public String getIpAddr(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                //根据网卡取本机配置的IP
                InetAddress inet=null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress= inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    //get request headers
    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}
