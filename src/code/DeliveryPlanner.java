package code;

import java.util.*;

/**
 * DeliveryPlanner assigns packages to trucks optimally.
 * 
 * Strategy: Greedy assignment - each customer is assigned to the truck
 * with the minimal delivery cost from that truck's current/initial position.
 * 
 * Note: This is a simplified version. For optimal global assignment,
 * consider Hungarian Algorithm or other optimization techniques.
 */
public class DeliveryPlanner {

    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;
    private final DeliverySearch ds;
    private final Strategy strategy;

    public DeliveryPlanner(List<State> stores,
                           List<State> customers,
                           List<State> trucks,
                           DeliverySearch ds,
                           Strategy strategy) {
        this.stores = stores;
        this.customers = customers;
        this.trucks = trucks;
        this.ds = ds;
        this.strategy = strategy;
    }

    /**
     * Assign each customer to the truck with minimal path cost.
     * 
     * Algorithm Options:
     * 1. GREEDY: Each customer → nearest truck (current implementation)
     * 2. OPTIMAL: Solve assignment problem for minimal total cost
     * 3. SEQUENTIAL: Assign and update truck positions after each delivery
     * 
     * @return List of (truckIndex, customerIndex) pairs
     */
    public List<int[]> assign() {
        // Choose assignment strategy based on problem size
        if (customers.size() <= 10 && trucks.size() <= 3) {
            return greedyAssignment();
        } else {
            return greedyAssignment(); // Fallback for large problems
        }
    }

    /**
     * Greedy Assignment: Each customer gets the closest truck.
     * Time Complexity: O(T * C * SearchCost)
     * where T = trucks, C = customers
     */
    private List<int[]> greedyAssignment() {
        List<int[]> assignments = new ArrayList<>();
        
        // Compute cost matrix
        double[][] costMatrix = computeCostMatrix();
        
        // For each customer, find truck with minimum cost
        for (int c = 0; c < customers.size(); c++) {
            int bestTruck = findBestTruck(costMatrix, c);
            
            if (bestTruck != -1) {
                assignments.add(new int[]{bestTruck, c});
            } else {
                System.err.println("[WARNING] Customer " + c + 
                    " at " + customers.get(c) + " is unreachable by all trucks!");
            }
        }
        
        return assignments;
    }

    /**
     * Optimal Assignment using Hungarian Algorithm.
     * Minimizes total delivery cost across all trucks.
     * 
     * For project: This is EXTRA CREDIT if you implement it!
     */
    private List<int[]> optimalAssignment() {
        // TODO: Implement Hungarian Algorithm or Branch & Bound
        // This would give globally optimal truck-customer assignments
        
        // For now, fall back to greedy
        return greedyAssignment();
    }

    /**
     * Sequential Assignment: Assign closest customer to each truck,
     * then update truck position (simulating delivery completion).
     */
    private List<int[]> sequentialAssignment() {
        List<int[]> assignments = new ArrayList<>();
        Set<Integer> assignedCustomers = new HashSet<>();
        Map<Integer, State> truckPositions = new HashMap<>();
        
        // Initialize truck positions
        for (int t = 0; t < trucks.size(); t++) {
            truckPositions.put(t, trucks.get(t));
        }
        
        // Assign customers one by one
        while (assignedCustomers.size() < customers.size()) {
            double minCost = Double.POSITIVE_INFINITY;
            int bestTruck = -1;
            int bestCustomer = -1;
            
            // Find best (truck, customer) pair
            for (int t = 0; t < trucks.size(); t++) {
                State truckPos = truckPositions.get(t);
                
                for (int c = 0; c < customers.size(); c++) {
                    if (assignedCustomers.contains(c)) continue;
                    
                    String pathStr = DeliverySearch.path(ds, truckPos, customers.get(c), strategy);
                    double cost = parseCost(pathStr);
                    
                    if (cost < minCost) {
                        minCost = cost;
                        bestTruck = t;
                        bestCustomer = c;
                    }
                }
            }
            
            if (bestTruck != -1 && bestCustomer != -1) {
                assignments.add(new int[]{bestTruck, bestCustomer});
                assignedCustomers.add(bestCustomer);
                
                // Update truck position (returns to store after delivery)
                truckPositions.put(bestTruck, stores.get(bestTruck));
            } else {
                break; // No more reachable customers
            }
        }
        
        return assignments;
    }

    /**
     * Compute cost matrix for all truck-customer pairs.
     * costMatrix[t][c] = cost for truck t to reach customer c
     */
    private double[][] computeCostMatrix() {
        double[][] costMatrix = new double[trucks.size()][customers.size()];
        
        for (int t = 0; t < trucks.size(); t++) {
            for (int c = 0; c < customers.size(); c++) {
                String pathStr = DeliverySearch.path(
                    ds, 
                    trucks.get(t), 
                    customers.get(c), 
                    strategy
                );
                costMatrix[t][c] = parseCost(pathStr);
            }
        }
        
        return costMatrix;
    }

    /**
     * Find the truck with minimum cost to reach customer c.
     */
    private int findBestTruck(double[][] costMatrix, int customerIdx) {
        double minCost = Double.POSITIVE_INFINITY;
        int bestTruck = -1;
        
        for (int t = 0; t < trucks.size(); t++) {
            if (costMatrix[t][customerIdx] < minCost) {
                minCost = costMatrix[t][customerIdx];
                bestTruck = t;
            }
        }
        
        return (minCost == Double.POSITIVE_INFINITY) ? -1 : bestTruck;
    }

    /**
     * Extract cost from path string: "actions;cost;nodesExpanded"
     */
    private double parseCost(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        
        // Handle "no path" case
        if (pathStr.startsWith("no path")) {
            return Double.POSITIVE_INFINITY;
        }
        
        String[] parts = pathStr.split(";");
        if (parts.length < 2) {
            return Double.POSITIVE_INFINITY;
        }
        
        try {
            return Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Failed to parse cost from: " + pathStr);
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Get statistics about the assignment plan.
     * Useful for reporting and debugging.
     */
    public AssignmentStats getStats(List<int[]> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return new AssignmentStats(0, 0, 0, 0);
        }
        
        double totalCost = 0;
        int[] deliveriesPerTruck = new int[trucks.size()];
        
        for (int[] assignment : assignments) {
            int truckIdx = assignment[0];
            int customerIdx = assignment[1];
            
            deliveriesPerTruck[truckIdx]++;
            
            String pathStr = DeliverySearch.path(
                ds, 
                trucks.get(truckIdx), 
                customers.get(customerIdx), 
                strategy
            );
            totalCost += parseCost(pathStr);
        }
        
        int maxDeliveries = 0;
        int minDeliveries = Integer.MAX_VALUE;
        for (int count : deliveriesPerTruck) {
            maxDeliveries = Math.max(maxDeliveries, count);
            if (count > 0) minDeliveries = Math.min(minDeliveries, count);
        }
        
        return new AssignmentStats(
            assignments.size(),
            totalCost,
            maxDeliveries,
            minDeliveries == Integer.MAX_VALUE ? 0 : minDeliveries
        );
    }

    /**
     * Statistics about delivery assignments.
     */
    public static class AssignmentStats {
        public final int totalAssignments;
        public final double totalCost;
        public final int maxDeliveriesPerTruck;
        public final int minDeliveriesPerTruck;
        
        public AssignmentStats(int totalAssignments, double totalCost, 
                             int maxDeliveriesPerTruck, int minDeliveriesPerTruck) {
            this.totalAssignments = totalAssignments;
            this.totalCost = totalCost;
            this.maxDeliveriesPerTruck = maxDeliveriesPerTruck;
            this.minDeliveriesPerTruck = minDeliveriesPerTruck;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Assignments: %d, Total Cost: %.2f, Max per truck: %d, Min per truck: %d",
                totalAssignments, totalCost, maxDeliveriesPerTruck, minDeliveriesPerTruck
            );
        }
    }

    /**
     * Visualize the assignment plan.
     */
    public void visualizeAssignments(List<int[]> assignments) {
        System.out.println("\n=== DELIVERY ASSIGNMENTS ===");
        
        Map<Integer, List<Integer>> truckToCustomers = new HashMap<>();
        for (int[] assignment : assignments) {
            truckToCustomers.computeIfAbsent(assignment[0], k -> new ArrayList<>())
                           .add(assignment[1]);
        }
        
        for (int t = 0; t < trucks.size(); t++) {
            List<Integer> customers = truckToCustomers.getOrDefault(t, new ArrayList<>());
            System.out.printf("Truck %d (at %s): %d deliveries → Customers %s%n",
                t, trucks.get(t), customers.size(), customers);
        }
        
        AssignmentStats stats = getStats(assignments);
        System.out.println("\n" + stats);
        System.out.println("============================\n");
    }
}