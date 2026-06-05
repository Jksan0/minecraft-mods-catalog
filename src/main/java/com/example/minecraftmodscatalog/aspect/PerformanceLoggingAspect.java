package com.example.minecraftmodscatalog.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Around("execution(* com.example.minecraftmodscatalog.service..*.*(..)) && !execution(* *.toString())")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder argsStr = new StringBuilder();
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    argsStr.append(", ");
                }
                argsStr.append(getArgString(args[i]));
            }
        }

        long startTime = System.nanoTime();
        log.debug("Method execution started: {}.{}({})", className, methodName, argsStr);

        try {
            Object result = joinPoint.proceed();
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            log.info("Method execution completed: {}.{}() - Duration: {}ms", className, methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Method execution failed: {}.{}() - Duration: {}ms - Exception: {}",
                    className, methodName, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String getArgString(Object arg) {
        if (arg == null) {
            return "null";
        }
        String str = arg.toString();
        if (str.length() > 100) {
            return str.substring(0, 100) + "...";
        }
        return str;
    }
}

