package org.example.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.dinner.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("src/main/config.properties"));

        int iterations = Integer.parseInt(props.getProperty("iterations", "10"));
        int fixedProgrammers = Integer.parseInt(props.getProperty("programmers_count", "-1"));
        int fixedFood = Integer.parseInt(props.getProperty("food_count", "-1"));

        for (int i = 0; i < iterations; i++) {
            AtomicInteger foodCount = new AtomicInteger(fixedFood);

            System.out.println("--------------------------------------------------------------------");
            System.out.println("Programmers: " + fixedProgrammers);
            System.out.println("Food: " + fixedFood);

            Dinner dinner = new Dinner();
            dinner.serve(fixedProgrammers, foodCount);
        }
    }
}