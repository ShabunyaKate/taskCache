package epam.training.contoller;
import epam.training.service.LFUCacheService;
import epam.training.service.LRUCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("caches")
public class RestCacheController
{
    @Autowired
    private LRUCacheService lruCacheService;
    @Autowired
    private LFUCacheService lfuCacheService;

    @RequestMapping(
            value = "/lru/{id}",
            params = { "id" },
            method = GET)
    @ResponseBody
    public String getLruCacheEntityById(@RequestParam("id") int id) {
        return lruCacheService.get(id);
    }

    @RequestMapping(
            value = "/lru/{id}",
            params = { "id", "value"},
            method = POST)
    @ResponseBody
    public void putLruCacheEntity(@RequestParam("id") int id,
                                    @RequestParam("id") String value) {
        lruCacheService.put(id,value);
    }

    @RequestMapping(
            value = "/lfu/{id}",
            params = { "id" },
            method = GET)
    @ResponseBody
    public String getLfuCacheEntityById(@RequestParam("id") int id) {
        return lfuCacheService.get(id);
    }

    @RequestMapping(
            value = "/lfu/{id}",
            params = { "id", "value" },
            method = POST)
    @ResponseBody
    public void putLfuCacheEntity(@RequestParam("id") int id,
                                  @RequestParam("id") String value) {
        lfuCacheService.put(id,value);
    }
}
