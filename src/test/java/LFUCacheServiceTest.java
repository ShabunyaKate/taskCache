import epam.training.service.LFUCacheService;
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
public class LFUCacheServiceTest {
    LFUCacheService lfuCacheService;

    @BeforeAll
    public void init() {
        lfuCacheService = new LFUCacheService(4,5000);
    }

    @AfterEach
    public void clearCache()
    {
        lfuCacheService.clearCache();
    }

    @Test
    public void addAndGetNewItemInCache()
    {
        lfuCacheService.put(1,"item1");
        assertEquals("item1",lfuCacheService.get(1));
    }

    @Test
    public void deleteItemEvictionPolicy()
    {
        lfuCacheService.put(1,"item1");
        lfuCacheService.put(2,"item2");
        lfuCacheService.put(3,"item3");
        lfuCacheService.put(4,"item4");
        lfuCacheService.put(5,"item5");
        assertNull(lfuCacheService.get(1));
        assertEquals("item5", lfuCacheService.get(5));
    }

    @Test
    public void deleteItemEvictionPolicyLFU()
    {
        lfuCacheService.put(1,"item1");
        lfuCacheService.put(2,"item2");
        lfuCacheService.put(3,"item3");
        lfuCacheService.put(4,"item4");
        lfuCacheService.get(1);
        lfuCacheService.put(5,"item5");
        assertNull(lfuCacheService.get(2));
        assertNotNull(lfuCacheService.get(1));
        assertEquals("item5", lfuCacheService.get(5));
    }

    @Test
    public void deleteItemCheckTimeEviction() throws InterruptedException {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        lfuCacheService.put(1,"item1");
        sleep(15000);
        assertEquals("Removing entity: key = 1 value = item1",
                outputStreamCaptor.toString()
                        .trim());
        assertNull(lfuCacheService.get(1));
    }

    @Test
    public void deleteItemCheckLogOutput()
    {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        fillCache();
        assertEquals("Removing entity: key = 1 value = item1",
                outputStreamCaptor.toString()
                        .trim());
    }

    @Test
    public void checkStatisticAverageTimePutOperation()
    {
        fillCache();
        Double averageTime = lfuCacheService.getAverageTimeSpentAddingNewValues();
        assertNotNull(averageTime);
        assertEquals(0.0, averageTime);
    }

    @Test
    public void checkStatisticNumberOfEvictions()
    {
        fillCache();
        assertEquals(1,lfuCacheService.getNumberOfEvictions());
    }

    @Test
    public void checkConcurrency() throws Exception {
        CyclicBarrier gate = new CyclicBarrier(4);

        LFUCacheServiceTest.ConcurrentLRUTestThread t1 = new LFUCacheServiceTest.ConcurrentLRUTestThread(gate, 1, "item1");
        LFUCacheServiceTest.ConcurrentLRUTestThread t2 = new LFUCacheServiceTest.ConcurrentLRUTestThread(gate, 2, "item2");
        LFUCacheServiceTest.ConcurrentLRUTestThread t3 = new LFUCacheServiceTest.ConcurrentLRUTestThread(gate, 3, "item3");

        t1.start();
        t2.start();
        t3.start();

        gate.await();

        t1.join();
        t2.join();
        t3.join();

        assertEquals("item1", lfuCacheService.get(1));
        assertEquals("item2", lfuCacheService.get(2));
        assertEquals("item3", lfuCacheService.get(3));
    }

    class ConcurrentLRUTestThread extends Thread {
        private CyclicBarrier gate;
        private Integer key;
        private String value;
        public ConcurrentLRUTestThread(CyclicBarrier gate, Integer key, String value) {
            this.gate = gate;
            this.key = key;
            this.value =value;
        }
        @Override
        public void run() {
            try {
                gate.await();
                lfuCacheService.put(key, value);
                lfuCacheService.get(key);
            } catch (Throwable x) {
                System.out.println(">>>>> "+ System.currentTimeMillis()
                        +" - "+Thread.currentThread().getId() + " ConcurrentLRUTestThread exception");
            }
        }
    }

    private void fillCache(){
        lfuCacheService.put(1,"item1");
        lfuCacheService.put(2,"item2");
        lfuCacheService.put(3,"item3");
        lfuCacheService.put(4,"item4");
        lfuCacheService.put(5,"item5");
    }
}

