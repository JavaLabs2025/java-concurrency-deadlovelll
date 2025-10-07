package org.example.programmer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.fork.Fork;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProgrammerTest {

    private Programmer programmer;
    private BlockingQueue<Fork> forks;
    private AtomicInteger foodLeft;
    private Semaphore semaphore;

    @BeforeEach
    public void setUp() {
        forks = new LinkedBlockingQueue<>();
        forks.add(new Fork(0));
        forks.add(new Fork(1));
        foodLeft  = new AtomicInteger(2);
        semaphore = new Semaphore(2);
        programmer = new Programmer(1, forks, foodLeft, semaphore);
    }

    @Test
    public void testProgrammerEat() {
        programmer.run();
        int portionsEaten = programmer.getPortionsEaten();
        assertEquals(2, portionsEaten);
    }

    @Test
    void testProgrammerEatMultiplePortions() {
        Programmer multiMealProgrammer = new Programmer(0, forks, foodLeft, semaphore);
        multiMealProgrammer.run();
        assertEquals(2, multiMealProgrammer.getPortionsEaten());
    }

    @Test
    void testTwoProgrammersGrabForks() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2, forks.size());
        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());
    }

    @Test
    void testConcurrentProgrammers() {
        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);
        Programmer p3 = new Programmer(2, forks, foodLeft, semaphore);

        p1.run();
        p2.run();
        p3.run();

        assertEquals(forks.size(), 2);
    }

    @Test
    void testRepeatedEating() {
        Programmer p = new Programmer(0, forks, foodLeft, semaphore);
        p.run();

        assertEquals(2, p.getPortionsEaten());
        assertEquals(forks.size(), 2);
    }

    @Test
    void testForksConsistencyAfterConcurrentEating() throws InterruptedException {
        BlockingQueue<Fork> sharedForks = new LinkedBlockingQueue<>();
        sharedForks.put(new Fork(0));
        sharedForks.put(new Fork(1));

        Programmer p1 = new Programmer(0, sharedForks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, sharedForks, foodLeft, semaphore);

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2, sharedForks.size());
        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());
    }

    @Test
    void testZeroMeals() throws InterruptedException {
        foodLeft  = new AtomicInteger(0);
        Programmer p = new Programmer(0, forks, foodLeft, semaphore);
        Thread t = new Thread(p);
        t.start();
        t.join();
        assertEquals(0, p.getPortionsEaten());
        assertEquals(2, forks.size());
    }

    @Test
    void testNegativeMeals() throws InterruptedException {
        foodLeft  = new AtomicInteger(-10);
        Programmer p = new Programmer(0, forks, foodLeft, semaphore);
        Thread t = new Thread(p);
        t.start();
        t.join();
        assertEquals(0, p.getPortionsEaten());
    }

    @Test
    void testMissingForks() throws InterruptedException {
        forks = new LinkedBlockingQueue<>();
        Programmer p = new Programmer(5, forks, foodLeft, semaphore);
        Thread t = new Thread(p);
        t.start();
        t.join(500);
        assertEquals(0, p.getPortionsEaten());
        assertEquals(0, forks.size());
    }

    @Test
    void testMoreProgrammersThanForks() throws InterruptedException {
        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);
        Programmer p3 = new Programmer(2, forks, foodLeft, semaphore);

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        Thread t3 = new Thread(p3);
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

        assertEquals(2, forks.size());
        int total = p1.getPortionsEaten() + p2.getPortionsEaten() + p3.getPortionsEaten();
        assertTrue(total <= 3);
    }

    @Test
    void testManyProgrammers() {
        int numProgrammers = 10;
        Programmer[] programmers = new Programmer[numProgrammers];

        for (int i = 0; i < numProgrammers; i++) {
            programmers[i] = new Programmer(i, forks, foodLeft, semaphore);
            programmers[i].run();
        }
        assertEquals(2, forks.size());
    }

    @Test
    void testNoDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);
        Programmer p3 = new Programmer(2, forks, foodLeft, semaphore);

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
    void testForksReturned() {
        Programmer p = new Programmer(0, forks, foodLeft, semaphore);
        p.run();
        assertEquals(2, forks.size());
    }

    @Test
    void testNoForkDoubleOwnership() {
        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);

        p1.run();
        p2.run();

        assertTrue(p1.userForks.isEmpty());
        assertTrue(p2.userForks.isEmpty());
        assertEquals(2, forks.size());
    }

    @Test
    void testGetProgId() {
        Programmer p = new Programmer(42, forks, foodLeft, semaphore);
        assertEquals(42, p.getProgId());
    }

    @Test
    void testIncrementPortions() throws InterruptedException {
        Programmer p = new Programmer(0, forks, foodLeft, semaphore);
        p.run();
        assertEquals(2, p.getPortionsEaten());
    }

    @Test
    void testMultipleProgrammers() throws InterruptedException {
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.add(new Fork(0));
        forks.add(new Fork(1));

        AtomicInteger foodLeft = new AtomicInteger(2);

        Programmer p1 = new Programmer(0, forks, foodLeft, semaphore);
        Programmer p2 = new Programmer(1, forks, foodLeft, semaphore);

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(1, p1.getPortionsEaten());
        assertEquals(1, p2.getPortionsEaten());

        assertEquals(2, forks.size());
    }
    @Test
    void testForksMutualExclusion() throws InterruptedException {
        BlockingQueue<Fork> sharedForks = new LinkedBlockingQueue<>();
        sharedForks.add(new Fork(0));
        sharedForks.add(new Fork(1));
        AtomicInteger food = new AtomicInteger(2);

        Programmer p1 = new Programmer(0, sharedForks, food, semaphore);
        Programmer p2 = new Programmer(1, sharedForks, food, semaphore);

        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2, p1.getPortionsEaten() + p2.getPortionsEaten());
        assertEquals(2, sharedForks.size());
    }
    @Test
    void testNoOverEating() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(1);
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer p1 = new Programmer(0, forks, food, semaphore);
        Programmer p2 = new Programmer(1, forks, food, semaphore);

        p1.run();
        p2.run();

        assertEquals(1, p1.getPortionsEaten() + p2.getPortionsEaten());
    }

    @Test
    void testManyProgrammersLimitedFood() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(3);
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer[] programmers = new Programmer[5];
        for (int i = 0; i < 5; i++) {
            programmers[i] = new Programmer(i, forks, food, semaphore);
            programmers[i].run();
        }
        int total = 0;
        for (Programmer p : programmers) total += p.getPortionsEaten();
        assertEquals(3, total);
        assertEquals(2, forks.size());
    }
    @Test
    void testNoDeadlockTwoProgrammers() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(2);
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer p1 = new Programmer(0, forks, food, semaphore);
        Programmer p2 = new Programmer(1, forks, food, semaphore);

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
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer[] programmers = new Programmer[10];
        for (int i = 0; i < 10; i++) {
            programmers[i] = new Programmer(i, forks, food, semaphore);
            programmers[i].run();
        }

        int total = Arrays.stream(programmers).mapToInt(Programmer::getPortionsEaten).sum();
        assertEquals(5, total);
        assertEquals(2, forks.size());
    }

    @Test
    void testForksAlwaysReturned() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(3);
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer p1 = new Programmer(0, forks, food, semaphore);
        Programmer p2 = new Programmer(1, forks, food, semaphore);
        Programmer p3 = new Programmer(2, forks, food, semaphore);

        p1.run(); p2.run(); p3.run();

        assertEquals(2, forks.size());
    }

    @Test
    void testAtomicFoodDecrement() throws InterruptedException {
        AtomicInteger food = new AtomicInteger(1);
        BlockingQueue<Fork> forks = new LinkedBlockingQueue<>();
        forks.put(new Fork(0));
        forks.put(new Fork(1));

        Programmer p1 = new Programmer(0, forks, food, semaphore);
        Programmer p2 = new Programmer(1, forks, food, semaphore);

        p1.run();
        p2.run();

        int totalEaten = p1.getPortionsEaten() + p2.getPortionsEaten();
        assertEquals(1, totalEaten);
    }
}
