import epam.training.service.LRUCacheService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CyclicBarrier;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LRUCacheServiceTest {

    LRUCacheService lruCacheService;

    @BeforeAll
    public void init() {
        lruCacheService = new LRUCacheService(4, 5000);
    }

    @AfterEach
    public void clearCache() {
        lruCacheService.clearCache();
    }

    @Test
    public void addAndGetNewItemInCache() {
        lruCacheService.put(1, "item1");
        assertEquals("item1", lruCacheService.get(1));
    }

    @Test
    public void deleteItemEvictionPolicy() {
        lruCacheService.put(1, "item1");
        lruCacheService.put(2, "item2");
        lruCacheService.put(3, "item3");
        lruCacheService.put(4, "item4");
        lruCacheService.put(5, "item5");
        assertNull(lruCacheService.get(1));
        assertEquals("item5", lruCacheService.get(5));
    }


    @Test
    public void deleteItemEvictionPolicyLRU() {
        lruCacheService.put(1, "item1");
        lruCacheService.put(2, "item2");
        lruCacheService.put(3, "item3");
        lruCacheService.put(4, "item4");
        lruCacheService.get(1);
        lruCacheService.put(5, "item5");
        assertNull(lruCacheService.get(2));
        assertNotNull(lruCacheService.get(1));
        assertEquals("item5", lruCacheService.get(5));
    }

    @Test
    public void deleteItemCheckTimeEviction() throws InterruptedException {
        lruCacheService.put(1, "item1");
        lruCacheService.get(1);
        sleep(10000);
        assertNull(lruCacheService.get(1));
    }

    @Test
    public void deleteItemCheckLogOutput() {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        fillCache();
        assertEquals("Key - 1 with value - item1 removed due to SIZE",
                outputStreamCaptor.toString()
                        .trim());
    }

    @Test
    public void checkStatisticAverageTimePutOperation() {
        fillCache();
        Double averageTime = lruCacheService.getAverageTimeSpentAddingNewValues();
        assertNotNull(averageTime);
        assertEquals(0.0, averageTime);
    }

    @Test
    public void checkStatisticNumberOfEvictions() {
        LRUCacheService lruCacheServiceForStats = new LRUCacheService(4, 5000);
        fillCache();
        lruCacheServiceForStats.put(1, "item1");
        lruCacheServiceForStats.put(2, "item2");
        lruCacheServiceForStats.put(3, "item3");
        lruCacheServiceForStats.put(4, "item4");
        lruCacheServiceForStats.put(5, "item5");
        assertEquals(1, lruCacheServiceForStats.getNumberOfEvictions());
    }

    @Test
    public void checkConcurrency() throws Exception {
        CyclicBarrier gate = new CyclicBarrier(4);

        ConcurrentLRUTestThread t1 = new ConcurrentLRUTestThread(gate, 1, "item1");
        ConcurrentLRUTestThread t2 = new ConcurrentLRUTestThread(gate, 2, "item2");
        ConcurrentLRUTestThread t3 = new ConcurrentLRUTestThread(gate, 3, "item3");

        t1.start();
        t2.start();
        t3.start();

        gate.await();

        t1.join();
        t2.join();
        t3.join();

        assertEquals("item1", lruCacheService.get(1));
        assertEquals("item2", lruCacheService.get(2));
        assertEquals("item3", lruCacheService.get(3));
    }

    class ConcurrentLRUTestThread extends Thread {
        private CyclicBarrier gate;
        private Integer key;
        private String value;

        public ConcurrentLRUTestThread(CyclicBarrier gate, Integer key, String value) {
            this.gate = gate;
            this.key = key;
            this.value = value;
        }

        @Override
        public void run() {
            try {
                gate.await();
                lruCacheService.put(key, value);
                lruCacheService.get(key);
            } catch (Throwable x) {
                System.out.println(">>>>> " + System.currentTimeMillis()
                        + " - " + Thread.currentThread().getId() + " ConcurrentLRUTestThread exception");
            }
        }
    }

    private void fillCache() {
        lruCacheService.put(1, "item1");
        lruCacheService.put(2, "item2");
        lruCacheService.put(3, "item3");
        lruCacheService.put(4, "item4");
        lruCacheService.put(5, "item5");
    }
}
