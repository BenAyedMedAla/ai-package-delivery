package code;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask for grid size
        System.out.println("Enter grid size (rows x columns):");
        int m = scanner.nextInt();
        int n = scanner.nextInt();

        // Ask for number of customers
        System.out.println("Enter number of customers:");
        int numCustomers = scanner.nextInt();

        // Ask for number of stores (max 3 as per PDF)
        System.out.println("Enter number of stores (1, 2, or 3):");
        int numStores = scanner.nextInt();

        // Ask for number of trucks (must match the number of stores)
        System.out.println("Enter number of trucks (same as stores, max 3):");
        int numTrucks = scanner.nextInt();

        // Ask for the search strategy or all strategies
        System.out.println("Enter search strategy (BF, DF, ID, UC, GR1, GR2, AS1, AS2) or 'all' to run all strategies:");
        String strategyInput = scanner.next();

        // Generate grid and traffic data using the user input
        String initialState = DeliverySearch.GenGrid(m, n, numCustomers, numStores); // Updated
        String traffic = DeliverySearch.GenTraffic(m, n); // Updated

        System.out.println("Generated initialState:");
        System.out.println(initialState);
        System.out.println("Generated traffic:");
        System.out.println(traffic);

       if ("all".equalsIgnoreCase(strategyInput)) {
    String[] names = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};
    System.out.println("=== Performance Comparison ===");
    System.out.println("Strategy | Result (time ms)");
    System.out.println("---------|------------------");

    for (String name : names) {
        long start = System.nanoTime();
        String result = DeliverySearch.solve(initialState, traffic, name, false);
        long time = (System.nanoTime() - start) / 1_000_000; // ms
        System.out.printf("%-5s | %s (%dms)%n", name, result.replace("\n", " | "), time);
    }
}else {
            // Run a specific strategy
            try {
                Strategy strategy = Strategy.fromString(strategyInput);
                long start = System.nanoTime();
                String result = DeliverySearch.solve(initialState, traffic, strategyInput, false);
                long time = (System.nanoTime() - start) / 1_000_000; // ms
                System.out.printf("%-8s | %s (%dms)%n", strategyInput, result.replace("\n", " | "), time);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid strategy. Please choose a valid strategy.");
            }
        }
    }
}
