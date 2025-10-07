package org.example.dinner;

import org.example.programmer.Programmer;
import org.example.fork.Fork;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceGenerator {
    public void generate(
            ArrayList<Programmer> programmers,
            BlockingQueue<Fork> forks,
            int amount,
            AtomicInteger foodAmount
    ) {
        Semaphore availableForks = new Semaphore(amount, true);
        // Generating the N amount of forks
        for (int i = 0; i < amount; i++) {
            forks.add(new Fork(i));
        }
        // The same amount of programmers
        for (int i = 0; i < amount; i++) {
            programmers.add(new Programmer(i, forks, foodAmount, availableForks));
        }
    }
}