package com.bikedc.cache;

import com.bikedc.model.Bicycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BicycleCache {
    private static final Logger logger = LoggerFactory.getLogger(BicycleCache.class);
    private final int MAX_CACHE_SIZE = 100;
    private final Map<Long, Bicycle> cache = new LinkedHashMap<Long, Bicycle>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Bicycle> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                logger.info("Evicting eldest cache entry - Bicycle ID: {}, Model: {}",
                        eldest.getKey(), eldest.getValue().getModel());
            }
            return shouldRemove;
        }
    };
    private final Map<Long, Integer> accessCount = new HashMap<>();

    public synchronized Bicycle get(Long id) {
        Bicycle bicycle = cache.get(id);
        if (bicycle != null) {
            int count = accessCount.getOrDefault(id, 0) + 1;
            accessCount.put(id, count);
            logger.debug("Cache hit - Bicycle ID: {}, Model: {}, Access count: {}",
                    id, bicycle.getModel(), count);
        } else {
            logger.debug("Cache miss - Bicycle ID: {}", id);
        }
        return bicycle;
    }

    public synchronized void put(Long id, Bicycle bicycle) {
        cache.put(id, bicycle);
        accessCount.put(id, 0);
        logger.info("Added to cache - Bicycle ID: {}, Model: {}", id, bicycle.getModel());
    }

    public synchronized void evict(Long id) {
        Bicycle removed = cache.remove(id);
        accessCount.remove(id);
        if (removed != null) {
            logger.info("Evicted from cache - Bicycle ID: {}, Model: {}", id, removed.getModel());
        }
    }

    public synchronized void clear() {
        logger.info("Clearing entire cache. Current size: {}", cache.size());
        cache.clear();
        accessCount.clear();
    }

    public Map<Long, Integer> getCacheStats() {
        logger.debug("Retrieving cache statistics");
        return new HashMap<>(accessCount);
    }

    public synchronized void logCacheStats() {
        logger.info("=== Cache Statistics ===");
        logger.info("Current cache size: {}", cache.size());
        logger.info("Most accessed bicycles:");

        accessCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    Bicycle bike = cache.get(entry.getKey());
                    String model = bike != null ? bike.getModel() : "N/A";
                    logger.info("Bicycle ID: {}, Model: {}, Access count: {}",
                            entry.getKey(), model, entry.getValue());
                });
    }
}