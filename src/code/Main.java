package code;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   DELIVERY SYSTEM - AI PROJECT         ║");
        System.out.println("║   Package Delivery Optimization        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Ask for grid size
        System.out.println("Enter grid size (rows columns):");
        System.out.print("  Example: 5 5\n  > ");
        int m = scanner.nextInt();
        int n = scanner.nextInt();

        // Ask for number of customers
        System.out.println("\nEnter number of customers (1-10):");
        System.out.print("  > ");
        int numCustomers = scanner.nextInt();
        if (numCustomers < 1 || numCustomers > 10) {
            System.out.println("Warning: Customers should be between 1-10. Adjusting...");
            numCustomers = Math.max(1, Math.min(10, numCustomers));
        }

        // Ask for number of stores (max 3 as per PDF)
        System.out.println("\nEnter number of stores (1-3):");
        System.out.print("  > ");
        int numStores = scanner.nextInt();
        if (numStores < 1 || numStores > 3) {
            System.out.println("Warning: Stores must be between 1-3. Adjusting...");
            numStores = Math.max(1, Math.min(3, numStores));
        }

        // Number of trucks = number of stores (automatic)
        int numTrucks = numStores;
        System.out.println("→ Number of trucks automatically set to: " + numTrucks);

        // Consume the newline left by nextInt()
        scanner.nextLine();

        // Ask for the search strategy
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SEARCH STRATEGY SELECTION");
        System.out.println("=".repeat(50));
        System.out.println("Available strategies:");
        System.out.println("  - 'all'  : Run all 8 strategies");
        System.out.println("  - Single : BF, DF, ID, UC, GR1, GR2, AS1, AS2");
        System.out.println("  - Multiple: e.g., 'BF UC AS1' (space-separated)");
        System.out.print("\nYour choice: ");
        String strategyInput = scanner.nextLine().trim();

        // Generate grid and traffic data
        System.out.println("\n" + "=".repeat(50));
        System.out.println("GENERATING GRID...");
        System.out.println("=".repeat(50));
        
        String initialState = DeliverySearch.GenGrid(m, n, numCustomers, numStores);
        String traffic = DeliverySearch.GenTraffic(m, n);

        System.out.println("✓ Grid generated successfully");
        System.out.println("\nInitial State:");
        System.out.println("  " + initialState);
        System.out.println("\nTraffic (first 100 chars):");
        System.out.println("  " + traffic.substring(0, Math.min(100, traffic.length())) + "...");
        System.out.println();

        // Parse strategy input
        List<String> strategiesToRun = new ArrayList<>();
        
        if ("all".equalsIgnoreCase(strategyInput)) {
            strategiesToRun.addAll(Arrays.asList("BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"));
        } else {
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
                System.out.println("⚠ Warning: Invalid strategy '" + strat + "' - skipping");
            }
        }

        if (validStrategies.isEmpty()) {
            System.out.println("✗ Error: No valid strategies provided.");
            scanner.close();
            return;
        }

        // Ask for visualization
        System.out.print("\nEnable visualization? (y/n): ");
        String vizInput = scanner.nextLine().trim().toLowerCase();
        boolean visualize = vizInput.equals("y") || vizInput.equals("yes");

        // Run the strategies
        System.out.println("\n" + "╔" + "═".repeat(70) + "╗");
        System.out.println("║" + " ".repeat(15) + "DELIVERY SEARCH - RESULTS" + " ".repeat(30) + "║");
        System.out.println("╚" + "═".repeat(70) + "╝\n");

        // Store results for comparison
        Map<String, StrategyResult> results = new LinkedHashMap<>();

        for (String strategyName : validStrategies) {
            // Force garbage collection for accurate memory measurement
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            Runtime runtime = Runtime.getRuntime();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            long cpuBefore = threadBean.getCurrentThreadCpuTime();
            long start = System.nanoTime();
            
            String result = DeliverySearch.solve(initialState, traffic, strategyName, visualize);
            
            long time = (System.nanoTime() - start) / 1_000_000; // ms
            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            long memUsed = (memAfter - memBefore) / 1024; // KB
            long cpuAfter = threadBean.getCurrentThreadCpuTime();
            long cpuUsed = (cpuAfter - cpuBefore) / 1_000_000; // ms
            
            // Extract metrics from result
            String[] lines = result.split("\n");
            int totalNodes = 0;
            int totalCost = 0;
            int deliveries = 0;
            
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(";");
                    if (parts.length >= 4) {
                        totalCost += Integer.parseInt(parts[2]);
                        totalNodes += Integer.parseInt(parts[3]);
                        deliveries++;
                    }
                }
            }
            
            results.put(strategyName, new StrategyResult(
                strategyName, time, memUsed, cpuUsed, totalNodes, totalCost, deliveries, result
            ));
            
            // Display individual result
            System.out.println("\n" + "─".repeat(70));
            System.out.println("Strategy: " + strategyName);
            System.out.println("─".repeat(70));
            
            if (!visualize) {
                // Show abbreviated results if not visualizing
                for (int i = 0; i < Math.min(3, lines.length); i++) {
                    if (!lines[i].trim().isEmpty()) {
                        System.out.println("  " + lines[i]);
                    }
                }
                if (lines.length > 3) {
                    System.out.println("  ... (" + (lines.length - 3) + " more deliveries)");
                }
            }
            
            System.out.printf("\nMetrics:\n");
            System.out.printf("  Time:       %6d ms\n", time);
            System.out.printf("  Deliveries: %6d\n", deliveries);
            System.out.printf("  Total Cost: %6d\n", totalCost);
            System.out.printf("  Nodes Exp:  %6d\n", totalNodes);
            System.out.printf("  Memory:     %6d KB\n", memUsed);
            System.out.printf("  CPU:        %6d ms\n", cpuUsed);
        }

        // Comparison table
        if (validStrategies.size() > 1) {
            printComparisonTable(results);
        }

        // Best strategy recommendation
        printRecommendation(results);

        scanner.close();
    }

    /**
     * Print comparison table for multiple strategies
     */
    private static void printComparisonTable(Map<String, StrategyResult> results) {
        System.out.println("\n\n" + "╔" + "═".repeat(85) + "╗");
        System.out.println("║" + " ".repeat(30) + "COMPARISON TABLE" + " ".repeat(39) + "║");
        System.out.println("╚" + "═".repeat(85) + "╝\n");
        
        System.out.printf("%-8s | %10s | %10s | %10s | %12s | %10s%n",
            "Strategy", "Time(ms)", "Cost", "Nodes", "Memory(KB)", "Complete");
        System.out.println("─".repeat(85));
        
        for (StrategyResult r : results.values()) {
            System.out.printf("%-8s | %10d | %10d | %10d | %12d | %10s%n",
                r.strategy, r.timeMs, r.totalCost, r.nodesExpanded, 
                r.memoryKB, (r.deliveries > 0 ? "✓" : "✗"));
        }
    }

    /**
     * Print recommendation based on results
     */
    private static void printRecommendation(Map<String, StrategyResult> results) {
        System.out.println("\n" + "╔" + "═".repeat(70) + "╗");
        System.out.println("║" + " ".repeat(25) + "RECOMMENDATIONS" + " ".repeat(30) + "║");
        System.out.println("╚" + "═".repeat(70) + "╝\n");
        
        // Find best by different criteria
        StrategyResult fastest = null;
        StrategyResult leastNodes = null;
        StrategyResult optimal = null;
        
        for (StrategyResult r : results.values()) {
            if (fastest == null || r.timeMs < fastest.timeMs) fastest = r;
            if (leastNodes == null || r.nodesExpanded < leastNodes.nodesExpanded) leastNodes = r;
            if (optimal == null || r.totalCost < optimal.totalCost) optimal = r;
        }
        
        System.out.println("🚀 Fastest:          " + fastest.strategy + 
                         " (" + fastest.timeMs + " ms)");
        System.out.println("🧠 Least Nodes:      " + leastNodes.strategy + 
                         " (" + leastNodes.nodesExpanded + " nodes)");
        System.out.println("✨ Best Cost:        " + optimal.strategy + 
                         " (cost = " + optimal.totalCost + ")");
        
        System.out.println("\n💡 Overall Recommendation:");
        if (results.containsKey("AS1") || results.containsKey("AS2")) {
            System.out.println("   A* provides the best balance of optimality and efficiency.");
        } else if (results.containsKey("UC")) {
            System.out.println("   Uniform Cost Search guarantees optimal solutions.");
        } else {
            System.out.println("   Consider using A* or UC for optimal solutions.");
        }
    }

    /**
     * Result container for strategy comparison
     */
    private static class StrategyResult {
        String strategy;
        long timeMs;
        long memoryKB;
        long cpuMs;
        int nodesExpanded;
        int totalCost;
        int deliveries;
        String fullResult;

        StrategyResult(String strategy, long timeMs, long memoryKB, long cpuMs,
                      int nodesExpanded, int totalCost, int deliveries, String fullResult) {
            this.strategy = strategy;
            this.timeMs = timeMs;
            this.memoryKB = memoryKB;
            this.cpuMs = cpuMs;
            this.nodesExpanded = nodesExpanded;
            this.totalCost = totalCost;
            this.deliveries = deliveries;
            this.fullResult = fullResult;
        }
    }
}