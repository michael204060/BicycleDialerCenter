package com.bikedc.cache;

import com.bikedc.model.Bicycle;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BicycleCache {
    private final int MAX_CACHE_SIZE = 100;
    private final Map<Long, Bicycle> cache = new LinkedHashMap<Long, Bicycle>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Bicycle> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
    private final Map<Long, Integer> accessCount = new HashMap<>();

    public synchronized Bicycle get(Long id) {
        Bicycle bicycle = cache.get(id);
        if (bicycle != null) {
            accessCount.put(id, accessCount.getOrDefault(id, 0) + 1);
        }
        return bicycle;
    }

    public synchronized void put(Long id, Bicycle bicycle) {
        cache.put(id, bicycle);
        accessCount.put(id, 0);
    }

    public synchronized void evict(Long id) {
        cache.remove(id);
        accessCount.remove(id);
    }

    public synchronized void clear() {
        cache.clear();
        accessCount.clear();
    }

    public Map<Long, Integer> getCacheStats() {
        return new HashMap<>(accessCount);
    }
}