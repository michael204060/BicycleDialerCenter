package com.bikedc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.bikedc.controller.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();

        logger.info("Entering: {}.{}() with arguments = {}", className, methodName, args);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            logger.info("Exiting: {}.{}() with result = {}, execution time = {} ms",
                    className, methodName, result, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("Exception in {}.{}(): {}, execution time = {} ms",
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis());
            throw e;
        }
    }

    @Around("execution(* com.bikedc.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();

        logger.debug("Service method called: {}.{}()", className, methodName);

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.error("Service exception in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}