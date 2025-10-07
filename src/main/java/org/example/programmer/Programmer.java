package org.example.programmer;

import org.example.fork.Fork;
import java.lang.*;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Programmer implements Runnable  {

    // Resources of the thread
    private final int id;
    final HashMap<Integer, Fork> userForks;
    private int portionsEaten;
    private final Semaphore availableForks;

    // Mutual resources shared between threads
    private final BlockingQueue<Fork> forks;
    private final AtomicInteger foodLeft;

    public Programmer(
            int id,
            BlockingQueue<Fork> forks,
            AtomicInteger foodLeft,
            Semaphore availableForks
    ) {
        // Resources of the thread
        this.id = id;
        this.userForks = new HashMap<>();
        this.portionsEaten = 0;
        this.availableForks = availableForks;

        // Mutual resources shared between threads
        this.forks = forks;
        this.foodLeft = foodLeft;
    }

    @Override
    public void run() {
        try {
            // Trying to eat while food is here
            while (takeOnePortionIfAvailable()) {
                boolean ate = false;
                // Trying while the programmer eat
                while (!ate) {
                    // If grabbed forks - eat
                    if (grabForks()) {
                        try {
                            EatDinnder();
                            ate = true;
                        } finally {
                            releaseForks();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean takeOnePortionIfAvailable() {
        int cur = foodLeft.get();
        // No food available - stop
        if (cur <= 0) return false;
        if (foodLeft.compareAndSet(cur, cur - 1)) return true;
        return false;
    }

    public int getProgId(){ return this.id; }
    public int getPortionsEaten(){ return this.portionsEaten; }

    private boolean grabForks() {
        try {
            availableForks.acquire(2);
            Fork first = forks.take();
            Fork second = forks.take();
            userForks.put(1, first);
            userForks.put(2, second);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void releaseForks() throws InterruptedException {
        Fork first = userForks.remove(1);
        Fork second = userForks.remove(2);
        if (first != null) forks.put(first);
        if (second != null) forks.put(second);
        availableForks.release(2);
    }

    private void EatDinnder() throws InterruptedException {
        // Increment the eaten portions
        this.portionsEaten++;
        // We are eating
        Thread.sleep(10);
    }
}