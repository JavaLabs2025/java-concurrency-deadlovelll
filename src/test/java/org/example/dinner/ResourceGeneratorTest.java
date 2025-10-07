package org.example.dinner;

import org.example.fork.Fork;
import org.example.programmer.Programmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ResourceGeneratorTest {

    private ResourceGenerator generator;
    private BlockingQueue<Fork> forks;
    private ArrayList<Programmer> programmers;
    private AtomicInteger foodAmount;

    @BeforeEach
    public void setUp() {
        generator = new ResourceGenerator();
        forks = new LinkedBlockingQueue<>();
        programmers = new ArrayList<>();
        foodAmount = new AtomicInteger(100);
    }

    @Test
    void test_generate_values_not_null(){
        generator.generate(programmers, forks, 10, foodAmount);
        assertNotEquals(programmers.size(), 0);
        assertNotEquals(forks.size(), 0);
    }
}
