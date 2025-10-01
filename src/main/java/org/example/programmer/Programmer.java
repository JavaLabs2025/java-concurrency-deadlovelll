package org.example.programmer;

import org.example.fork.Fork;
import org.example.utils.IndexFetcher;

import java.lang.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class Programmer extends Thread {

    // Resources of the thread
    private final int id;
    final HashMap<Integer, Fork> userForks;
    private int portionsEaten;
    private final int forkIdx1;
    private final int forkIdx2;
    private final Random rand;

    // Mutual resources shared between threads
    private final ConcurrentHashMap<Integer, Fork> forks;
    private final AtomicInteger foodLeft;

    public Programmer(
            int id,
            ConcurrentHashMap<Integer, Fork> forks,
            AtomicInteger foodLeft
    ) {
        // Resources of the thread
        this.id = id;
        this.userForks = new HashMap<>();
        this.portionsEaten = 0;
        IndexFetcher indexFetcher = new IndexFetcher();
        this.forkIdx1 = indexFetcher.fetch(forks, this.id - 1);
        this.forkIdx2 = indexFetcher.fetch(forks, this.id + 1);
        this.rand = new Random();

        // Mutual resources shared between threads
        this.forks = forks;
        this.foodLeft = foodLeft;
    }

    @Override
    public void run() {
        try {
            // Trying to eat while food is here
            while (!forks.isEmpty() && takeOnePortionIfAvailable()) {
                boolean ate = false;
                // Trying while the programmer eat
                while (!ate) {
                    // If grabbed forks - eat
                    if (grabForks()) {
                        try {
                            EatDinnder();
                            ate = true;
                        } finally {
                            ReleaseForks();
                        }
                    } else {
                        Thread.sleep(50);
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
        // Grabbing the forks in ascending order by their id to prevent deadlocks.
        // Deadlock can occur if two threads try to acquire the same two forks in different orders:
        //   - Thread A picks fork 1 then waits for fork 2
        //   - Thread B picks fork 2 then waits for fork 1
        //   In this situation both threads wait forever.
        //
        // To prevent this, we always pick the fork with the smaller id first (first),
        // and the larger id second (second). This ensures all threads acquire forks
        // in the same order, eliminating the possibility of circular waiting.
        // p.s: it was the most difficult part of homework.
        Fork first = forkIdx1 < forkIdx2 ? forks.get(forkIdx1) : forks.get(forkIdx2);
        Fork second = forkIdx1 < forkIdx2 ? forks.get(forkIdx2) : forks.get(forkIdx1);

        // Locking only the shared forks
        first.lock();
        second.lock();
        userForks.put(first.getId(), first);
        userForks.put(second.getId(), second);
        return true;
    }

    private void EatDinnder() throws InterruptedException {
        // Increment the eaten portions
        this.portionsEaten++;
        // We are eating
        Thread.sleep(10);
    }

    private void ReleaseForks() {
        // Getting forks that programmer have
        Fork first = userForks.remove(forkIdx1);
        Fork second = userForks.remove(forkIdx2);
        // Unlocking forks
        if (first != null) first.unlock();
        if (second != null) second.unlock();
    }
}