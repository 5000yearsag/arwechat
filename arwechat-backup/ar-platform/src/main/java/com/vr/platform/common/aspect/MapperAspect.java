package com.vr.platform.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author zhangchenyang
 * @date 2022/1/14 0014
 */
@Component
@Aspect
@Slf4j
public class MapperAspect {

    @AfterReturning("execution(* com.corner..*Dao.*(..))")
    public void logServiceAccess(JoinPoint joinPoint){
        log.info("Completed: " + joinPoint);
    }

    @Pointcut("execution(* com.corner..*Dao.*(..))")
    private void pointCutMethod(){

    }

    /**
     * 声明环绕通知
     */
    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        long begin = System.nanoTime();
        Object obj = pjp.proceed();
        long end = System.nanoTime();
        log.info("调用Mapper方法：{}，\n参数：{}，\n耗时：{}毫秒", pjp.getSignature().toString(), Arrays.toString(pjp.getArgs()), (end - begin) / 1000000);
        return obj;
    }

}
