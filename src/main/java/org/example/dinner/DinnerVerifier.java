package org.example.dinner;

import java.util.concurrent.atomic.AtomicInteger;

public class DinnerVerifier {
    public void execute(int ProgrammersCount, AtomicInteger FoodCount) throws IllegalArgumentException {
        // Verifying that the amount can't be negative
        if (ProgrammersCount <= 0 || FoodCount.get() <= 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        if (FoodCount.get() < ProgrammersCount) {
            throw new IllegalArgumentException("Some of programmers would still be hungry");
        }
    }
}