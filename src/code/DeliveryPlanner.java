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
// Replace the assign() method body with this implementation
public List<int[]> assign() {
    List<int[]> assignments = new ArrayList<>();

    // Matrix of truck-to-customer costs
    double[][] costMatrix = new double[trucks.size()][customers.size()];

    // Calculate the cost for all combinations of trucks and customers
    for (int c = 0; c < customers.size(); c++) {
        for (int t = 0; t < trucks.size(); t++) {
            String pathStr = DeliverySearch.path(ds, trucks.get(t), customers.get(c), strategy);
            String[] parts = pathStr.split(";");
            if (parts.length >= 3) {
    try {
        double cost = Double.parseDouble(parts[1]);
        costMatrix[t][c] = cost;
    } catch (NumberFormatException ex) {
        costMatrix[t][c] = Double.POSITIVE_INFINITY;
    }
} else {
    costMatrix[t][c] = Double.POSITIVE_INFINITY;
}

        }
    }

    // For each customer pick the truck with the minimal cost (trucks can be reused)
    for (int c = 0; c < customers.size(); c++) {
        double minCost = Double.POSITIVE_INFINITY;
        int bestTruck = -1;
        for (int t = 0; t < trucks.size(); t++) {
            if (costMatrix[t][c] < minCost) {
                minCost = costMatrix[t][c];
                bestTruck = t;
            }
        }
        if (bestTruck != -1 && minCost != Double.POSITIVE_INFINITY) {
            assignments.add(new int[]{bestTruck, c});
        } else {
            // no reachable truck for this customer; handle as you prefer:
            // keep unassigned (skip), or add with -1 to indicate unreachable.
            System.out.println("Warning: Customer " + c + " unreachable by all trucks.");
        }
    }

    return assignments;
}



}
