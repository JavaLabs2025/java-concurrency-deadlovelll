package org.example.app;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.dinner.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Random rand = new Random();
        int MAX = 1000; // hehe messenger max is everywhere
        // Running 10 iterations
        for (int i = 0; i < 10; i++) {
            // Generating random nums
            int programmersCount = rand.nextInt(MAX);
            int foodCounter = programmersCount + 1 + rand.nextInt(MAX - programmersCount);
            AtomicInteger foodCount = new AtomicInteger(foodCounter);
            System.out.println("--------------------------------------------------------------------");
            // Displaying them
            System.out.println("Programmers: " + programmersCount);
            System.out.println("Food: " + foodCounter);
            // Initializing the dinner
            Dinner dinner = new Dinner();
            // Starting the dinner
            dinner.serve(programmersCount, foodCount);
        }
    }
}