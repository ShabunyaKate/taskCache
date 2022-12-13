package epam.training.service;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class LFUCacheService implements CacheService {

    private Map<Integer, String> valueMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> countMap = new ConcurrentHashMap<>();
    private TreeMap<Integer, List<Integer>> frequencyMap = new TreeMap<>();
    private Map<Integer, Long> timeMap = new ConcurrentHashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock writeLock = lock.writeLock();
    private Lock readLock = lock.readLock();
    private int cacheCapacity;
    private int timeStampOfRemoval;
    private Double averageTimeSpentAddingNewValues = 0.0;
    private Long numberOfEvictions = 0L;

    public LFUCacheService() {
        initializeCleaner();
    }

    public LFUCacheService(Integer cacheCapacity, Integer timeStampOfRemoval) {
        this.cacheCapacity = cacheCapacity;
        this.timeStampOfRemoval = timeStampOfRemoval;
        initializeCleaner();
    }

    @Override
    public String get(int key) {
        readLock.lock();
        try {
            if (!valueMap.containsKey(key) || cacheCapacity == 0) {
                System.out.println("Key - " + key + " not found");
                return null;
            }
            int frequency = countMap.get(key);
            frequencyMap.get(frequency).remove(Integer.valueOf(key));

            if (frequencyMap.get(frequency).size() == 0) {
                frequencyMap.remove(frequency);
            }
            frequencyMap.computeIfAbsent(frequency + 1, k -> new LinkedList<>()).add(key);
            countMap.put(key, frequency + 1);
            addOrUpdateTimeInTimeMap(key);
            return valueMap.get(key);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public double getAverageTimeSpentAddingNewValues() {
        return averageTimeSpentAddingNewValues;
    }

    @Override
    public long getNumberOfEvictions() {
        return numberOfEvictions;
    }


    @Override
    public void put(int key, String value) {
        writeLock.lock();
        long start = System.currentTimeMillis();
        try {
            if (!valueMap.containsKey(key)) {
                if (valueMap.size() == cacheCapacity && valueMap.size() != 0) {
                    int lowestCount = frequencyMap.firstKey();
                    int keyToDelete = frequencyMap.get(lowestCount).remove(0);
                    if (frequencyMap.get(lowestCount).size() == 0) {
                        frequencyMap.remove(lowestCount);
                    }
                    String valueOfDeletedEntity = valueMap.remove(keyToDelete);
                    countMap.remove(keyToDelete);
                    numberOfEvictions++;
                    System.out.println("Removing entity: key = " + keyToDelete +
                            " value = " + valueOfDeletedEntity);
                }
                valueMap.put(key, value);
                countMap.put(key, 1);
                frequencyMap.computeIfAbsent(1, k -> new LinkedList<>()).add(key);
                addOrUpdateTimeInTimeMap(key);
            } else {
                valueMap.put(key, value);
                int frequency = countMap.get(key);
                frequencyMap.get(frequency).remove(Integer.valueOf(key));

                if (frequencyMap.get(frequency).size() == 0) {
                    frequencyMap.remove(frequency);
                }
                frequencyMap.computeIfAbsent(frequency + 1, k -> new LinkedList<>()).add(key);
                countMap.put(key, frequency + 1);
                addOrUpdateTimeInTimeMap(key);
            }
        } finally {
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            calculateAverageTimeSpentAddingNewValues(timeElapsed);
            writeLock.unlock();
        }
    }

    public String removeValueFromCache(int key) {
        writeLock.lock();
        try {
            countMap.remove(key);
            frequencyMap.remove(key);
            timeMap.remove(key);
            return valueMap.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clearCache() {
        valueMap.clear();
        countMap.clear();
        timeMap.clear();
        frequencyMap.clear();
        averageTimeSpentAddingNewValues = 0.0;
        numberOfEvictions = 0L;
    }

    private void addOrUpdateTimeInTimeMap(int key) {
        Date date = new Date();
        timeMap.put(key, date.getTime());
    }

    private void calculateAverageTimeSpentAddingNewValues(long timeValue) {
        averageTimeSpentAddingNewValues = (averageTimeSpentAddingNewValues + timeValue) / 2;
    }

    private void initializeCleaner() {
        new LFUCacheService.CleanerThread().start();
    }

    class CleanerThread extends Thread {
        @Override
        public void run() {
            System.out.println("Initiating Cleaner Thread..");
            while (true) {
                cleanCache();
                try {
                    Thread.sleep(timeStampOfRemoval / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cleanCache() {
            long currentTime = new Date().getTime();
            String valueOfDeletedEntity = null;
            for (Integer key : timeMap.keySet()) {
                if (currentTime > (timeMap.get(key) + timeStampOfRemoval)) {
                    valueOfDeletedEntity = removeValueFromCache(key);
                    numberOfEvictions++;
                    System.out.println("Removing entity: key = " + key +
                            " value = " + valueOfDeletedEntity);
                }
            }
        }
    }
}
