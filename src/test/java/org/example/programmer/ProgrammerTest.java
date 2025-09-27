package org.example.programmer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.fork.Fork;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProgrammerTest {

    private Programmer programmer;
    private ConcurrentHashMap<Integer, Fork> forks;
    private AtomicInteger foodLeft;

    @BeforeEach
    public void setUp() {
        forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));
        foodLeft  = new AtomicInteger(2);
        programmer = new Programmer(1, forks, foodLeft);
    }

    @Test
    public void testProgrammerEat() throws InterruptedException {
        programmer.start();
        programmer.join();
        int portionsEaten = programmer.getPortionsEaten();
        assertEquals(2, portionsEaten);
    }

    @Test
    void testProgrammerEatMultiplePortions() throws InterruptedException {
        Programmer multiMealProgrammer = new Programmer(0, forks, foodLeft);
        multiMealProgrammer.start();
        multiMealProgrammer.join();
        assertEquals(2, multiMealProgrammer.getPortionsEaten());
    }

    @Test
    void testTwoProgrammersGrabForks() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertEquals(2, forks.size());
        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());
    }

    @Test
    void testConcurrentProgrammers() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);
        Programmer p3 = new Programmer(2, forks, foodLeft);

        p1.start();
        p2.start();
        p3.start();

        p1.join();
        p2.join();
        p3.join();

        assertEquals(forks.size(), 2);
    }

    @Test
    void testRepeatedEating() throws InterruptedException {
        Programmer p = new Programmer(0, forks, foodLeft);
        p.start();
        p.join();

        assertEquals(2, p.getPortionsEaten());
        assertEquals(forks.size(), 2);
    }

    @Test
    void testForksConsistencyAfterConcurrentEating() throws InterruptedException {
        ConcurrentHashMap<Integer, Fork> sharedForks = new ConcurrentHashMap<>();
        sharedForks.put(0, new Fork(0));
        sharedForks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, sharedForks, foodLeft);
        Programmer p2 = new Programmer(1, sharedForks, foodLeft);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertEquals(2, sharedForks.size());
        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());
    }

    @Test
    void testZeroMeals() throws InterruptedException {
        foodLeft  = new AtomicInteger(0);
        Programmer p = new Programmer(0, forks, foodLeft);
        p.start();
        p.join();
        assertEquals(0, p.getPortionsEaten());
        assertEquals(2, forks.size());
    }

    @Test
    void testNegativeMeals() throws InterruptedException {
        foodLeft  = new AtomicInteger(-10);
        Programmer p = new Programmer(0, forks, foodLeft);
        p.start();
        p.join();
        assertEquals(0, p.getPortionsEaten());
    }

    @Test
    void testMissingForks() throws InterruptedException {
        forks = new ConcurrentHashMap<>();
        Programmer p = new Programmer(5, forks, foodLeft);
        p.start();
        p.join();
        assertEquals(0, p.getPortionsEaten());
    }

    @Test
    void testMoreProgrammersThanForks() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);
        Programmer p3 = new Programmer(2, forks, foodLeft);

        p1.start();
        p2.start();
        p3.start();

        p1.join();
        p2.join();
        p3.join();

        assertEquals(2, forks.size());
        int total = p1.getPortionsEaten() + p2.getPortionsEaten() + p3.getPortionsEaten();
        assertTrue(total <= 3);
    }

    @Test
    void testManyProgrammers() throws InterruptedException {
        int numProgrammers = 10;
        Programmer[] programmers = new Programmer[numProgrammers];

        for (int i = 0; i < numProgrammers; i++) {
            programmers[i] = new Programmer(i, forks, foodLeft);
            programmers[i].start();
        }
        for (Programmer p : programmers) {
            p.join();
        }
        assertEquals(2, forks.size());
    }

    @Test
    void testNoDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);
        Programmer p3 = new Programmer(2, forks, foodLeft);

        Future<?> f1 = executor.submit(p1);
        Future<?> f2 = executor.submit(p2);
        Future<?> f3 = executor.submit(p3);

        f1.get(5, TimeUnit.SECONDS);
        f2.get(5, TimeUnit.SECONDS);
        f3.get(5, TimeUnit.SECONDS);

        executor.shutdownNow();
        assertEquals(2, forks.size());
    }

    @Test
    void testForksReturned() throws InterruptedException {
        Programmer p = new Programmer(0, forks, foodLeft);
        p.start();
        p.join();
        assertEquals(2, forks.size());
    }

    @Test
    void testNoForkDoubleOwnership() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertTrue(p1.userForks.isEmpty());
        assertTrue(p2.userForks.isEmpty());
        assertEquals(2, forks.size());
    }

    @Test
    void testGetProgId() {
        Programmer p = new Programmer(42, forks, foodLeft);
        assertEquals(42, p.getProgId());
    }

    @Test
    void testIncrementPortions() throws InterruptedException {
        Programmer p = new Programmer(0, forks, foodLeft);
        p.start();
        p.join();
        assertEquals(2, p.getPortionsEaten());
    }

    @Test
    void testMultipleProgrammers() throws InterruptedException {
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        AtomicInteger foodLeft = new AtomicInteger(2);

        Programmer p1 = new Programmer(0, forks, foodLeft);
        Programmer p2 = new Programmer(1, forks, foodLeft);

        p1.start();
        p2.start();

        p1.join();
        p2.join();

        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());

        assertEquals(2, forks.size());
    }

    @Test
    void testFastSwitching() throws InterruptedException {
        foodLeft  = new AtomicInteger(10);
        ConcurrentHashMap<Integer, Fork> localForks = new ConcurrentHashMap<>();
        localForks.put(0, new Fork(0));
        localForks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, localForks, foodLeft);
        Programmer p2 = new Programmer(1, localForks, foodLeft);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertEquals(2, localForks.size());
        assertEquals(5, p1.getPortionsEaten());
        assertEquals(5, p2.getPortionsEaten());
    }

    @Test
    void testForksMutualExclusion() throws InterruptedException {
        ConcurrentHashMap<Integer, Fork> sharedForks = new ConcurrentHashMap<>();
        sharedForks.put(0, new Fork(0));
        sharedForks.put(1, new Fork(1));
        AtomicInteger food = new AtomicInteger(2);

        Programmer p1 = new Programmer(0, sharedForks, food);
        Programmer p2 = new Programmer(1, sharedForks, food);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertEquals(2, p1.getPortionsEaten() + p2.getPortionsEaten());
        assertEquals(2, sharedForks.size());
    }

    @Test
    void testNoOverEating() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(1);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, forks, food);
        Programmer p2 = new Programmer(1, forks, food);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        assertEquals(1, p1.getPortionsEaten() + p2.getPortionsEaten());
    }

    @Test
    void testForksReturnedOnInterrupt() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(10);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p = new Programmer(0, forks, food);
        p.start();
        p.interrupt();
        p.join();

        assertEquals(2, forks.size());
        assertTrue(p.userForks.isEmpty());
    }

    @Test
    void testManyProgrammersLimitedFood() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(3);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer[] programmers = new Programmer[5];
        for (int i = 0; i < 5; i++) {
            programmers[i] = new Programmer(i, forks, food);
            programmers[i].start();
        }
        for (Programmer p : programmers) p.join();

        int total = 0;
        for (Programmer p : programmers) total += p.getPortionsEaten();
        assertEquals(3, total);
        assertEquals(2, forks.size());
    }

    void testRandomThreadOrder() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(4);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, forks, food);
        Programmer p2 = new Programmer(1, forks, food);
        Programmer p3 = new Programmer(2, forks, food);

        List<Programmer> list = Arrays.asList(p1, p2, p3);
        Collections.shuffle(list);
        list.forEach(Thread::start);
        list.forEach(p -> {
            try { p.join(); } catch (InterruptedException ignored) {}
        });

        int total = p1.getPortionsEaten() + p2.getPortionsEaten() + p3.getPortionsEaten();
        assertEquals(4, total);
    }

    @Test
    void testNoDeadlockTwoProgrammers() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(2);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, forks, food);
        Programmer p2 = new Programmer(1, forks, food);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(p1);
        executor.submit(p2);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(2, p1.getPortionsEaten() + p2.getPortionsEaten());
    }

    @Test
    void testFoodDistributionManyThreads() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(5);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer[] programmers = new Programmer[10];
        for (int i = 0; i < 10; i++) {
            programmers[i] = new Programmer(i, forks, food);
            programmers[i].start();
        }
        for (Programmer p : programmers) p.join();

        int total = Arrays.stream(programmers).mapToInt(Programmer::getPortionsEaten).sum();
        assertEquals(5, total);
        assertEquals(2, forks.size());
    }

    @Test
    void testSimultaneousStart() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(2);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        CountDownLatch latch = new CountDownLatch(1);
        Programmer p1 = new Programmer(0, forks, food) {
            public void run() { try { latch.await(); super.run(); } catch (InterruptedException ignored) {} }
        };
        Programmer p2 = new Programmer(1, forks, food) {
            public void run() { try { latch.await(); super.run(); } catch (InterruptedException ignored) {} }
        };

        p1.start();
        p2.start();
        latch.countDown();

        p1.join();
        p2.join();

        assertEquals(2, p1.getPortionsEaten() + p2.getPortionsEaten());
    }

    @Test
    void testForksAlwaysReturned() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(3);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, forks, food);
        Programmer p2 = new Programmer(1, forks, food);
        Programmer p3 = new Programmer(2, forks, food);

        p1.start(); p2.start(); p3.start();
        p1.join(); p2.join(); p3.join();

        assertEquals(2, forks.size());
    }

    @Test
    void testAtomicFoodDecrement() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(1);
        ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));

        Programmer p1 = new Programmer(0, forks, food);
        Programmer p2 = new Programmer(1, forks, food);

        p1.start();
        p2.start();
        p1.join();
        p2.join();

        int totalEaten = p1.getPortionsEaten() + p2.getPortionsEaten();
        assertEquals(1, totalEaten);
    }
}
