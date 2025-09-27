package org.example.dinner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DinnerVerifierTest {

    private DinnerVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new DinnerVerifier();
    }

    @Test
    void execute_withValidValues_doesNotThrow() {
        assertDoesNotThrow(() -> verifier.execute(3, new AtomicInteger(5)));
        assertDoesNotThrow(() -> verifier.execute(2, new AtomicInteger(2)));
    }

    @Test
    void execute_withNegativeProgrammers_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> verifier.execute(-1, new AtomicInteger(5)));
    }

    @Test
    void execute_withNegativeFood_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> verifier.execute(3, new AtomicInteger(-2)));
    }

    @Test
    void execute_withInsufficientFood_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> verifier.execute(5, new AtomicInteger(-3)));
    }
}