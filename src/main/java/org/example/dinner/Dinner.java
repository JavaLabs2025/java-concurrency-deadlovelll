package org.example.dinner;

import org.example.programmer.Programmer;
import org.example.fork.Fork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dinner {

    // Dependencies
    private final DinnerVerifier verifier = new DinnerVerifier();
    private final ResourceGenerator resourceGenerator = new ResourceGenerator();
    private final ArrayList<Programmer> programmers = new ArrayList<>();
    private final ConcurrentHashMap<Integer, Fork> forks = new ConcurrentHashMap<>();
    private HashMap<Integer, Integer> counts = new HashMap<>();

    public void serve(int programmersCount, AtomicInteger foodCount) throws InterruptedException {
        // Verifying the values to not run with garbage ones
        verifier.execute(programmersCount, foodCount);
        // Generating resources
        resourceGenerator.generate(programmers, forks, programmersCount, foodCount);
        // Starting dinner
        for (Programmer p : programmers) p.start();
        for (Programmer p : programmers) p.join();
        System.out.println("Dinner is over!");
        System.out.println("Food amount is " + foodCount);
        // Counting stats to display
        for (Programmer programmer : programmers) {
            int count = counts.getOrDefault(programmer.getPortionsEaten(), 0);
            counts.put(programmer.getPortionsEaten(), count + 1);
        }
        // Displaying stats
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int portionsEaten = entry.getKey();
            int count = entry.getValue();
            System.out.println("Portions eaten: " + portionsEaten + ", Count: " + count);
        }
        System.out.println("--------------------------------------------------------------------");
    }
}