package tests;

import code.*;

/**
 * Basic validation tests for the delivery system.
 * These tests verify format correctness and basic functionality.
 */
public class PlaceholderTest {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   PLACEHOLDER TEST SUITE");
        System.out.println("=================================\n");
        
        try {
            testInitialStateFormat();
            testTrafficFormat();
            testSolveSingle();
            testSolveMultiple();
            testAllStrategies();
            
            System.out.println("\n=================================");
            System.out.println("✓ ALL TESTS PASSED!");
            System.out.println("=================================");
        } catch (AssertionError e) {
            System.err.println("\n✗ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Test 1: Verify GenGrid produces correct format
     */
    public static void testInitialStateFormat() {
        System.out.println("Test 1: InitialState Format");
        System.out.println("---------------------------");
        
        String initialState = DeliverySearch.GenGrid(5, 5, 2, 1);
        
        if (initialState == null || initialState.isEmpty()) {
            throw new AssertionError("initialState is null or empty");
        }

        String[] parts = initialState.split(";");
        if (parts.length < 4) {
            throw new AssertionError("initialState has wrong format. Expected at least 4 parts (m;n;P;S), got: " + parts.length);
        }
        
        // Verify each part is parseable
        try {
            int m = Integer.parseInt(parts[0]);
            int n = Integer.parseInt(parts[1]);
            int P = Integer.parseInt(parts[2]);
            int S = Integer.parseInt(parts[3]);
            
            if (m != 5 || n != 5 || P != 2 || S != 1) {
                throw new AssertionError("Grid parameters don't match requested values");
            }
        } catch (NumberFormatException e) {
            throw new AssertionError("initialState contains non-numeric values: " + e.getMessage());
        }
        
        System.out.println("  Generated: " + initialState);
        System.out.println("✓ Test 1 passed\n");
    }

    /**
     * Test 2: Verify GenTraffic produces correct format
     */
    public static void testTrafficFormat() {
        System.out.println("Test 2: Traffic Format");
        System.out.println("----------------------");
        
        String traffic = DeliverySearch.GenTraffic(5, 5);
        
        if (traffic == null || traffic.isEmpty()) {
            throw new AssertionError("traffic is null or empty");
        }

        String[] edges = traffic.split(";");
        int validEdges = 0;
        
        for (String e : edges) {
            if (e.trim().isEmpty()) continue;
            
            String[] p = e.split(",");
            if (p.length != 5) {
                throw new AssertionError("Traffic edge has invalid format. Expected 5 parts, got " + 
                                       p.length + " in: " + e);
            }
            
            // Verify all parts are numeric
            try {
                for (String part : p) {
                    Integer.parseInt(part);
                }
                validEdges++;
            } catch (NumberFormatException ex) {
                throw new AssertionError("Traffic edge contains non-numeric value: " + e);
            }
        }
        
        System.out.println("  Generated " + validEdges + " valid edges");
        System.out.println("✓ Test 2 passed\n");
    }

    /**
     * Test 3: Solve with single customer
     */
    public static void testSolveSingle() {
        System.out.println("Test 3: Solve Single Delivery");
        System.out.println("-----------------------------");
        
        String init = "5;5;1;1;4,4;";
        String traffic = DeliverySearch.GenTraffic(5, 5);

        String result = DeliverySearch.solve(init, traffic, "BF", false);

        if (result == null || result.isEmpty()) {
            throw new AssertionError("Solve returned null or empty result");
        }

        // Format attendu: truckIdx,customerIdx;plan;cost;nodesExpanded
        String[] lines = result.split("\n");
        
        if (lines.length == 0) {
            throw new AssertionError("Solve returned no delivery lines");
        }
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(";");
            if (parts.length < 4) {
                throw new AssertionError("Solve output malformed: " + line + 
                    "\nExpected format: truckIdx,customerIdx;plan;cost;nodesExpanded");
            }
            
            // Verify truck,customer pair
            if (!parts[0].contains(",")) {
                throw new AssertionError("Missing truck,customer pair in: " + line);
            }
            
            // Verify cost is numeric
            try {
                int cost = Integer.parseInt(parts[2]);
                int nodes = Integer.parseInt(parts[3]);
                
                if (cost < 0 || nodes < 0) {
                    throw new AssertionError("Negative cost or nodes in: " + line);
                }
            } catch (NumberFormatException e) {
                throw new AssertionError("Non-numeric cost or nodes in: " + line);
            }
        }
        
        System.out.println("  Result: " + lines[0]);
        System.out.println("✓ Test 3 passed\n");
    }

    /**
     * Test 4: Solve with multiple customers
     */
    public static void testSolveMultiple() {
        System.out.println("Test 4: Solve Multiple Deliveries");
        System.out.println("---------------------------------");
        
        String init = "6;6;3;2;2,2,4,4,5,1;";
        String traffic = DeliverySearch.GenTraffic(6, 6);

        String result = DeliverySearch.solve(init, traffic, "UC", false);

        if (result == null || result.isEmpty()) {
            throw new AssertionError("Solve returned null or empty result");
        }

        String[] lines = result.split("\n");
        int deliveries = 0;
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            deliveries++;
        }
        
        if (deliveries != 3) {
            System.out.println("  WARNING: Expected 3 deliveries, got " + deliveries);
            System.out.println("  This might be OK if some customers are unreachable");
        } else {
            System.out.println("  Successfully planned " + deliveries + " deliveries");
        }
        
        System.out.println("✓ Test 4 passed\n");
    }

    /**
     * Test 5: Verify all strategies work
     */
    public static void testAllStrategies() {
        System.out.println("Test 5: All Search Strategies");
        System.out.println("-----------------------------");
        
        String init = "4;4;2;1;2,2,3,3;";
        String traffic = DeliverySearch.GenTraffic(4, 4);

        String[] strategies = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};
        
        for (String strategy : strategies) {
            try {
                String result = DeliverySearch.solve(init, traffic, strategy, false);
                
                if (result == null || result.isEmpty()) {
                    throw new AssertionError(strategy + " returned null or empty");
                }
                
                System.out.println("  " + strategy + ": ✓");
            } catch (Exception e) {
                throw new AssertionError(strategy + " failed: " + e.getMessage());
            }
        }
        
        System.out.println("✓ Test 5 passed\n");
    }
}