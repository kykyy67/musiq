package by.aleksandr.music.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceExecutionLoggingAspect {

    @Around("execution(* by.aleksandr.music.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startedAt;
            log.info("Service method {} completed in {} ms", method, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startedAt;
            log.error("Service method {} failed in {} ms", method, duration, ex);
            throw ex;
        }
    }
}