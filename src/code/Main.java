package code;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String grid = DeliverySearch.GenGrid();
        System.out.println("Generated Grid:");
        System.out.println(grid);

        Strategy[] strategies = {Strategy.BF, Strategy.DF, Strategy.ID, Strategy.UC, Strategy.GR1, Strategy.GR2, Strategy.AS1, Strategy.AS2};
        String[] names = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};

        System.out.println("=== Performance Comparison ===");
        System.out.println("Strategy | Path | Cost | Nodes | Time");
        System.out.println("---------|------|------|-------|------");

        for (int i = 0; i < strategies.length; i++) {
            long start = System.nanoTime();
            String result = DeliverySearch.solve(grid, "", strategies[i], false);
            long time = (System.nanoTime() - start) / 1_000_000; // ms
            String[] parts = result.split(";");
            String path = parts[0].equals("no path") ? "no path" : parts[0];
            String cost = parts.length > 1 ? parts[1] : "0";
            String nodes = parts.length > 2 ? parts[2] : "0";
            System.out.printf("%-8s | %-20s | %-4s | %-5s | %dms%n", names[i], path.substring(0, Math.min(20, path.length())), cost, nodes, time);
        }
    }
}