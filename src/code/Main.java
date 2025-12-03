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

        // Ensure that numTrucks equals numStores
        if (numTrucks != numStores) {
            System.out.println("Error: Number of trucks must be equal to number of stores.");
            scanner.close();
            return;
        }

        // Consume the newline left by nextInt()
        scanner.nextLine();

        // Ask for the search strategy or all strategies
        System.out.println("Enter search strategy:");
        System.out.println("  - Type 'all' to run all strategies");
        System.out.println("  - Type one strategy: BF, DF, ID, UC, GR1, GR2, AS1, AS2");
        System.out.println("  - Type multiple strategies separated by spaces: BF UC AS1");
        String strategyInput = scanner.nextLine().trim();

        // Generate grid and traffic data using the user input
        String initialState = DeliverySearch.GenGrid(m, n, numCustomers, numStores);
        String traffic = DeliverySearch.GenTraffic(m, n);

        System.out.println("\nGenerated initialState:");
        System.out.println(initialState);
        System.out.println("\nGenerated traffic:");
        System.out.println(traffic);
        System.out.println();

        // Parse strategy input
        List<String> strategiesToRun = new ArrayList<>();
        
        if ("all".equalsIgnoreCase(strategyInput)) {
            // Run all strategies
            strategiesToRun.addAll(Arrays.asList("BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"));
        } else {
            // Split input by whitespace and add each valid strategy
            String[] inputStrategies = strategyInput.split("\\s+");
            for (String strat : inputStrategies) {
                if (!strat.isEmpty()) {
                    strategiesToRun.add(strat.toUpperCase());
                }
            }
        }

        // Validate strategies
        List<String> validStrategies = new ArrayList<>();
        for (String strat : strategiesToRun) {
            try {
                Strategy.fromString(strat);
                validStrategies.add(strat);
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: Invalid strategy '" + strat + "' - skipping");
            }
        }

        if (validStrategies.isEmpty()) {
            System.out.println("Error: No valid strategies provided.");
            scanner.close();
            return;
        }

        // Run the strategies
        System.out.println("=== Performance Comparison ===");
        System.out.println("Strategy | Result (time ms)");
        System.out.println("---------|------------------");

        for (String strategyName : validStrategies) {
            long start = System.nanoTime();
            String result = DeliverySearch.solve(initialState, traffic, strategyName, false);
            long time = (System.nanoTime() - start) / 1_000_000; // ms
            System.out.printf("%-8s | %s (%dms)%n", strategyName, result.replace("\n", " | "), time);
        }

        scanner.close();
    }
}