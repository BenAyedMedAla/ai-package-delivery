package code;

import java.util.*;

public class DeliveryPlanner {
    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;
    private final DeliverySearch ds;
    private final Strategy strategy;

    public DeliveryPlanner(List<State> stores, List<State> customers, List<State> trucks, DeliverySearch ds, Strategy strategy) {
        this.stores = stores;
        this.customers = customers;
        this.trucks = trucks;
        this.ds = ds;
        this.strategy = strategy;
    }

    // Assign each customer to the truck with lowest cost path (trucks can be reused)
// Try assigning trucks in a way that minimizes the total cost
public List<int[]> assign() {
    List<int[]> assignments = new ArrayList<>();
    List<Double> truckCosts = new ArrayList<>();
    List<Double> customerCosts = new ArrayList<>();
    
    // Matrix of truck-to-customer costs
    double[][] costMatrix = new double[trucks.size()][customers.size()];

    // Calculate the cost for all combinations of trucks and customers
    for (int c = 0; c < customers.size(); c++) {
        for (int t = 0; t < trucks.size(); t++) {
            String pathStr = DeliverySearch.path(ds, trucks.get(t), customers.get(c), strategy);
            String[] parts = pathStr.split(";");
            if (parts.length == 3) {
                double cost = Double.parseDouble(parts[1]);
                costMatrix[t][c] = cost;
            } else {
                costMatrix[t][c] = Double.POSITIVE_INFINITY; // If no valid path
            }
        }
    }

    // Now, we select the best assignment based on the minimum total cost
    boolean[] usedTrucks = new boolean[trucks.size()];
    for (int c = 0; c < customers.size(); c++) {
        double minCost = Double.POSITIVE_INFINITY;
        int bestTruck = -1;
        
        // Find the best truck for the customer based on the lowest cost
        for (int t = 0; t < trucks.size(); t++) {
            if (usedTrucks[t] || costMatrix[t][c] == Double.POSITIVE_INFINITY) continue;

            if (costMatrix[t][c] < minCost) {
                minCost = costMatrix[t][c];
                bestTruck = t;
            }
        }

        if (bestTruck != -1) {
            // Assign the truck to the customer
            assignments.add(new int[]{bestTruck, c});
            usedTrucks[bestTruck] = true; // Mark the truck as used
        }
    }

    return assignments;
}


}
