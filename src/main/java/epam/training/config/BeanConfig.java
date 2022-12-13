package epam.training.config;
import epam.training.service.LFUCacheService;
import epam.training.service.LRUCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class BeanConfig {

    public LRUCacheService lruCacheService(@Value("${cache_service.lru.max_size}") Integer maxSize,
                                           @Value("${cache_service.lru.timestamp}") Integer timestamp ) {
      return new LRUCacheService(maxSize, timestamp);
    }

    public LFUCacheService lfUCacheService(@Value("${cache_service.lfu.max_size}") Integer maxSize,
                                           @Value("${cache_service.lfu.timestamp}") Integer timestamp ) {
        return new LFUCacheService(maxSize, timestamp);
    }

}
