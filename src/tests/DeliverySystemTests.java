package tests ;

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
            System.out.println("  Result: " + result);
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
        String initialState = "5;5;1;1;4,0;0,0,4,4;";
        String traffic = generateUniformTraffic(5, 5, 3);

        try {
            String result = DeliverySearch.solve(initialState, traffic, "UC", false);
            System.out.println("✓ Tunnel test passed");
            System.out.println("  Result: " + result);
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
        
        // Block some roads by setting traffic to 0
        String traffic = 
            "0,0,0,1,2;0,0,1,0,2;" +
            "1,0,1,1,2;1,0,2,0,0;" +  // Block (1,0) -> (2,0)
            "2,0,2,1,2;2,0,3,0,2;" +
            "3,0,3,1,2;" +
            "0,1,0,2,2;0,1,1,1,2;" +
            "1,1,1,2,2;1,1,2,1,2;" +
            "2,1,2,2,2;2,1,3,1,2;" +
            "3,1,3,2,2;" +
            "0,2,0,3,2;0,2,1,2,2;" +
            "1,2,1,3,2;1,2,2,2,2;" +
            "2,2,2,3,2;2,2,3,2,2;" +
            "3,2,3,3,2;" +
            "0,3,1,3,2;" +
            "1,3,2,3,2;" +
            "2,3,3,3,2";

        try {
            String result = DeliverySearch.solve(initialState, traffic, "BF", false);
            System.out.println("✓ Blocked roads test passed");
            System.out.println("  Result: " + result);
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

        for (String strategy : strategies) {
            try {
                long startTime = System.currentTimeMillis();
                String result = DeliverySearch.solve(initialState, traffic, strategy, false);
                long endTime = System.currentTimeMillis();

                String[] parts = result.split("\n");
                System.out.printf("  %-4s: ✓ (%.0f ms) - %d deliveries%n", 
                    strategy, (endTime - startTime) * 1.0, parts.length);
            } catch (Exception e) {
                System.out.println("  " + strategy + ": ✗ FAILED - " + e.getMessage());
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
            System.out.println("  Solution:");
            for (String line : result.split("\n")) {
                System.out.println("    " + line);
            }
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
        System.out.println("--------------------------------------------------------");

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
     * Helper: Generate uniform traffic for testing
     */
    private static String generateUniformTraffic(int m, int n, int trafficLevel) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // Add all valid edges
                if (i > 0) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i - 1).append(",").append(j).append(",")
                      .append(trafficLevel).append(";");
                }
                if (i < m - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i + 1).append(",").append(j).append(",")
                      .append(trafficLevel).append(";");
                }
                if (j > 0) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j - 1).append(",")
                      .append(trafficLevel).append(";");
                }
                if (j < n - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j + 1).append(",")
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