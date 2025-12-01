package com.aipackagedelivery.code;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String initialState = DeliverySearch.GenGrid();
        String traffic = DeliverySearch.GenTraffic();
        System.out.println("Generated initialState:");
        System.out.println(initialState);
        System.out.println("Generated traffic:");
        System.out.println(traffic);

        Strategy[] strategies = {Strategy.BF, Strategy.DF, Strategy.ID, Strategy.UC, Strategy.GR1, Strategy.GR2, Strategy.AS1, Strategy.AS2};
        String[] names = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};

        System.out.println("=== Performance Comparison ===");
        System.out.println("Strategy | Result");
        System.out.println("---------|-------");

        for (int i = 0; i < strategies.length; i++) {
            long start = System.nanoTime();
            String result = DeliverySearch.solve(initialState, traffic, names[i], false);
            long time = (System.nanoTime() - start) / 1_000_000; // ms
            System.out.printf("%-8s | %s (%dms)%n", names[i], result.replace("\n", " | "), time);
        }
    }
}