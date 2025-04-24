package com.bikedc.aspect;

import com.bikedc.service.VisitCounterService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VisitCounterAspect {
    private final VisitCounterService visitCounterService;

    public VisitCounterAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void countVisit(JoinPoint joinPoint) {
        String endpoint = joinPoint.getSignature().toShortString();
        visitCounterService.incrementCounter(endpoint);
    }
}