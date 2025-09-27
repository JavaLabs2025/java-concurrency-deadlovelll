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
            while (true) {
                if (this.forks.isEmpty()) break;
                // If no food left - break the loop
                if (!takeOnePortionIfAvailable()) break;
                boolean ate = false;
                // Loop until programmer eat
                while (!ate) {
                    // If successfully grabbed forks
                    // - eat
                    if (grabForks(this.forks)) {
                        try {
                            EatDinnder();
                            ate = true;
                        } finally {
                            // Returning forks back
                            ReleaseForks(this.forks);
                        }
                    } else {
                        // Little sleep
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

    private boolean grabForks(ConcurrentHashMap<Integer, Fork> forks) {
        // Getting forks from the table
        Fork fork1 = forks.get(this.forkIdx1);
        Fork fork2 = forks.get(this.forkIdx2);
        if (fork1 == null || fork2 == null) return false;

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
        Fork first = forkIdx1 < forkIdx2 ? fork1 : fork2;
        Fork second = forkIdx1 < forkIdx2 ? fork2 : fork1;
        // Synchronizing only the shared forks
        synchronized (first) {
            synchronized (second) {
                forks.remove(first.getId());
                forks.remove(second.getId());
            }
        }
        // Because userForks used only in current thread
        // no synchronization
        this.userForks.put(first.getId(), first);
        this.userForks.put(second.getId(), second);
        return true;
    }

    private void EatDinnder() throws InterruptedException {
        // Increment the eaten portions
        this.portionsEaten++;
        // We are eating
        Thread.sleep(1000 + rand.nextInt(500));
    }

    private void ReleaseForks(ConcurrentHashMap<Integer, Fork> forks) {
        // Getting forks that programmer have
        Fork fork1 = this.userForks.get(this.forkIdx1);
        Fork fork2 = this.userForks.get(this.forkIdx2);

        // Putting back the forks
        forks.put(fork1.getId(), fork1);
        forks.put(fork2.getId(), fork2);
        // Removing the forks from programmer
        this.userForks.remove(fork1.getId());
        this.userForks.remove(fork2.getId());
    }
}