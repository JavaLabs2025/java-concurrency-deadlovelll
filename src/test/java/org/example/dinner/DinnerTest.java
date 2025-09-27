package org.example.dinner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DinnerTest {

    private Dinner dinner;

    @BeforeEach
    void setUp() {
        dinner = new Dinner();
    }

    @Test
    void serve_withEnoughFood_doesNotThrow() {
        int programmersCount = 3;
        AtomicInteger foodCount = new AtomicInteger(5);

        assertDoesNotThrow(() -> dinner.serve(programmersCount, foodCount));
    }

    @Test
    void serve_withExactFood_doesNotThrow() {
        int programmersCount = 2;
        AtomicInteger foodCount = new AtomicInteger(2);

        assertDoesNotThrow(() -> dinner.serve(programmersCount, foodCount));
    }

    @Test
    void serve_withZeroFood_doesNotThrow() {
        int programmersCount = 1;
        AtomicInteger foodCount = new AtomicInteger(0);

        assertThrows(IllegalArgumentException.class, () -> dinner.serve(programmersCount, foodCount));
    }
}
