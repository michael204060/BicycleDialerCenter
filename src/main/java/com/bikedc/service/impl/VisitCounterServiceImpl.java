package com.bikedc.service.impl;

import com.bikedc.service.VisitCounterService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class VisitCounterServiceImpl implements VisitCounterService {
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void incrementCounter(String endpoint) {
        counters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public long getCount(String endpoint) {
        return counters.getOrDefault(endpoint, new AtomicLong(0)).get();
    }

    @Override
    public Map<String, Long> getVisitStats() {
        return counters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
}