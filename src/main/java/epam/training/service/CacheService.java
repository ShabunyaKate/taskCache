package epam.training.service;

public interface CacheService {
   void put(int key, String value);
   String get(int key);
   double getAverageTimeSpentAddingNewValues();
   long getNumberOfEvictions();
   void clearCache();

}
