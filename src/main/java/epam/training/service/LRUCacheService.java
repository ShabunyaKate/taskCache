package epam.training.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//Guava
@Service
public class LRUCacheService implements CacheService {

    private Cache<Integer, String> cache;

    public LRUCacheService() {
    }

    public LRUCacheService(Integer cacheCapacity, Integer timeStampOfRemoval) {

        cache = CacheBuilder.newBuilder().maximumSize(cacheCapacity)
                .expireAfterAccess(timeStampOfRemoval, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<Integer, String>) notification -> System.out.println("Key - " + notification.getKey()
                        + " with value - " + notification.getValue()
                        + " removed due to " + notification.getCause()))
                .recordStats()
                .concurrencyLevel(1).
                build();
    }

    @Override
    public String get(int key) {
        return cache.getIfPresent(key);
    }

    @Override
    public double getAverageTimeSpentAddingNewValues() {
        return cache.stats().averageLoadPenalty();
    }

    @Override
    public long getNumberOfEvictions() {
        return cache.stats().evictionCount();
    }

    @Override
    public void put(int key, String value) {
        cache.put(key, value);
    }

    @Override
    public void clearCache() {
        cache.cleanUp();
        cache.invalidateAll();
    }
}