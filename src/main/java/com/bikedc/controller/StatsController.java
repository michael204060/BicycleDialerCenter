package com.bikedc.controller;

import com.bikedc.service.VisitCounterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final VisitCounterService visitCounterService;

    public StatsController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/visits")
    public Map<String, Long> getVisitStats() {
        return visitCounterService.getVisitStats();
    }
}