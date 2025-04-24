package com.bikedc.service;

import java.util.Map;

public interface VisitCounterService {
    void incrementCounter(String endpoint);

    long getCount(String endpoint);

    Map<String, Long> getVisitStats();
}