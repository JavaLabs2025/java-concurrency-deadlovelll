package org.example.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.dinner.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("src/main/config.properties"));

        int iterations = Integer.parseInt(props.getProperty("iterations", "10"));
        int maxProgrammers = Integer.parseInt(props.getProperty("max_programmers", "1000"));
        int fixedProgrammers = Integer.parseInt(props.getProperty("programmers_count", "-1"));
        int fixedFood = Integer.parseInt(props.getProperty("food_count", "-1"));

        Random rand = new Random();

        for (int i = 0; i < iterations; i++) {
            int programmersCount = (fixedProgrammers >= 0) ? fixedProgrammers : rand.nextInt(maxProgrammers);
            int foodCounter = (fixedFood >= 0) ? fixedFood : programmersCount + 1 + rand.nextInt(maxProgrammers - programmersCount);
            AtomicInteger foodCount = new AtomicInteger(foodCounter);

            System.out.println("--------------------------------------------------------------------");
            System.out.println("Programmers: " + programmersCount);
            System.out.println("Food: " + foodCounter);

            Dinner dinner = new Dinner();
            dinner.serve(programmersCount, foodCount);
        }
    }
}