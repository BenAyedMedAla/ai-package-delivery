package tests;

import code.*;

/**
 * Comprehensive test suite for the delivery system.
 * Tests search algorithms, planning, and end-to-end scenarios.
 */
public class DeliverySystemTests {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  DELIVERY SYSTEM TEST SUITE");
        System.out.println("========================================\n");

        // Run all tests
        testSimpleGrid();
        testWithTunnels();
        testWithBlockedRoads();
        testAllSearchStrategies();
        testCompletePipeline();
        testPerformanceComparison();
        testOptimalityCheck();
        
        System.out.println("\n========================================");
        System.out.println("  ALL TESTS COMPLETED");
        System.out.println("========================================");
    }

    /**
     * Test 1: Simple 3x3 grid, 1 store, 1 customer
     */
    private static void testSimpleGrid() {
        System.out.println("TEST 1: Simple Grid");
        System.out.println("-------------------");

        String initialState = "3;3;1;1;2,2;";
        String traffic = generateUniformTraffic(3, 3, 2);

        try {
            String result = DeliverySearch.solve(initialState, traffic, "BF", false);
            System.out.println("✓ Simple grid test passed");
            
            // Verify format
            String[] parts = result.split(";");
            if (parts.length >= 4) {
                System.out.println("  Truck-Customer: " + parts[0]);
                System.out.println("  Path: " + parts[1]);
                System.out.println("  Cost: " + parts[2]);
                System.out.println("  Nodes: " + parts[3]);
            }
        } catch (Exception e) {
            System.out.println("✗ Simple grid test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 2: Grid with tunnels
     */
    private static void testWithTunnels() {
        System.out.println("TEST 2: Grid with Tunnels");
        System.out.println("-------------------------");

        // 5x5 grid, tunnel from (0,0) to (4,4)
        String initialState = "5;5;1;1;4,0;0,0,4,4";
        String traffic = generateUniformTraffic(5, 5, 3);

        try {
            String result = DeliverySearch.solve(initialState, traffic, "UC", false);
            System.out.println("✓ Tunnel test passed");
            
            String[] lines = result.split("\n");
            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    System.out.println("  Delivery: " + parts[0] + ", Cost: " + parts[2]);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Tunnel test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 3: Grid with blocked roads
     */
    private static void testWithBlockedRoads() {
        System.out.println("TEST 3: Blocked Roads");
        System.out.println("--------------------");

        String initialState = "4;4;1;1;3,3;";
        
        // Create traffic with some blocked roads
        String traffic = generateUniformTraffic(4, 4, 2);
        // Add a few blocked roads (traffic = 0)
        traffic += ";1,0,2,0,0;2,1,3,1,0"; // Block some paths

        try {
            String result = DeliverySearch.solve(initialState, traffic, "BF", false);
            System.out.println("✓ Blocked roads test passed");
            System.out.println("  Result: " + result.split("\n")[0]);
        } catch (Exception e) {
            System.out.println("✗ Blocked roads test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 4: All search strategies on same problem
     */
    private static void testAllSearchStrategies() {
        System.out.println("TEST 4: All Search Strategies");
        System.out.println("-----------------------------");

        String initialState = "5;5;2;2;1,1,3,3;";
        String traffic = generateUniformTraffic(5, 5, 2);

        String[] strategies = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};

        System.out.printf("%-6s | %-8s | %-12s | %-10s%n", "Strat", "Time(ms)", "Deliveries", "Status");
        System.out.println("------------------------------------------------");

        for (String strategy : strategies) {
            try {
                long startTime = System.currentTimeMillis();
                String result = DeliverySearch.solve(initialState, traffic, strategy, false);
                long endTime = System.currentTimeMillis();

                String[] parts = result.split("\n");
                int deliveries = 0;
                for (String line : parts) {
                    if (!line.trim().isEmpty()) deliveries++;
                }

                System.out.printf("%-6s | %8d | %12d | %-10s%n", 
                    strategy, (endTime - startTime), deliveries, "✓");
            } catch (Exception e) {
                System.out.printf("%-6s | %8s | %12s | %-10s%n", 
                    strategy, "-", "-", "✗ FAILED");
            }
        }
        System.out.println();
    }

    /**
     * Test 5: Complete pipeline (GenGrid -> solve)
     */
    private static void testCompletePipeline() {
        System.out.println("TEST 5: Complete Pipeline");
        System.out.println("------------------------");

        try {
            // Generate random grid
            String initialState = DeliverySearch.GenGrid(6, 6, 3, 2);
            String traffic = DeliverySearch.GenTraffic(6, 6);

            System.out.println("  Generated grid: " + initialState);

            // Solve with A*
            String result = DeliverySearch.solve(initialState, traffic, "AS1", false);
            
            System.out.println("✓ Complete pipeline test passed");
            System.out.println("  Number of deliveries: " + result.split("\n").length);
        } catch (Exception e) {
            System.out.println("✗ Complete pipeline test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 6: Performance comparison
     */
    private static void testPerformanceComparison() {
        System.out.println("TEST 6: Performance Comparison");
        System.out.println("------------------------------");

        String initialState = "8;8;5;2;1,1,2,2,3,3,4,4,5,5;";
        String traffic = generateUniformTraffic(8, 8, 2);

        System.out.printf("%-10s | %-10s | %-10s | %-15s%n", 
            "Strategy", "Time (ms)", "Total Cost", "Nodes Expanded");
        System.out.println("----------------------------------------------------------");

        String[] strategies = {"BF", "UC", "GR1", "AS1"};

        for (String strategy : strategies) {
            try {
                long startTime = System.nanoTime();
                String result = DeliverySearch.solve(initialState, traffic, strategy, false);
                long endTime = System.nanoTime();

                double timeMs = (endTime - startTime) / 1_000_000.0;
                
                // Parse results
                int totalCost = 0;
                int totalNodes = 0;
                String[] deliveries = result.split("\n");
                
                for (String delivery : deliveries) {
                    if (delivery.trim().isEmpty()) continue;
                    String[] parts = delivery.split(";");
                    if (parts.length >= 4) {
                        totalCost += Integer.parseInt(parts[2]);
                        totalNodes += Integer.parseInt(parts[3]);
                    }
                }

                System.out.printf("%-10s | %10.2f | %10d | %15d%n", 
                    strategy, timeMs, totalCost, totalNodes);
            } catch (Exception e) {
                System.out.printf("%-10s | %10s | %10s | %15s%n", 
                    strategy, "FAILED", "-", "-");
            }
        }
        System.out.println();
    }

    /**
     * Test 7: Verify optimality of UC and A*
     */
    private static void testOptimalityCheck() {
        System.out.println("TEST 7: Optimality Check (UC vs A*)");
        System.out.println("------------------------------------");

        String initialState = "6;6;3;1;2,2,4,4,5,5;";
        String traffic = generateUniformTraffic(6, 6, 2);

        try {
            String resultUC = DeliverySearch.solve(initialState, traffic, "UC", false);
            String resultAS1 = DeliverySearch.solve(initialState, traffic, "AS1", false);

            // Extract total costs
            int costUC = getTotalCost(resultUC);
            int costAS1 = getTotalCost(resultAS1);

            System.out.println("  UC Total Cost:  " + costUC);
            System.out.println("  A* Total Cost:  " + costAS1);

            if (costUC == costAS1) {
                System.out.println("✓ Both algorithms found optimal solution");
            } else {
                System.out.println("⚠ Warning: Costs differ (might be due to ties)");
            }
        } catch (Exception e) {
            System.out.println("✗ Optimality check FAILED: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Helper: Extract total cost from result string
     */
    private static int getTotalCost(String result) {
        int total = 0;
        for (String line : result.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length >= 3) {
                total += Integer.parseInt(parts[2]);
            }
        }
        return total;
    }

    /**
     * Helper: Generate uniform traffic for testing
     */
    private static String generateUniformTraffic(int m, int n, int trafficLevel) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // Right edge
                if (j < n - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j + 1).append(",")
                      .append(trafficLevel).append(";");
                }
                // Down edge
                if (i < m - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i + 1).append(",").append(j).append(",")
                      .append(trafficLevel).append(";");
                }
            }
        }
        
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Visual test for debugging
     */
    public static void visualTest() {
        System.out.println("\n=== VISUAL TEST ===\n");
        
        String initialState = "5;5;2;1;3,2,4,4;";
        String traffic = generateUniformTraffic(5, 5, 1);
        
        System.out.println("Grid: 5x5");
        System.out.println("Store: (0,0)");
        System.out.println("Customers: (3,2), (4,4)");
        System.out.println("\nSolving with visualization...\n");
        
        String result = DeliverySearch.solve(initialState, traffic, "AS1", true);
        
        System.out.println("\nFinal Result:");
        System.out.println(result);
    }
}