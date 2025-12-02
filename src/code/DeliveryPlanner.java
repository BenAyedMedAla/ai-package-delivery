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
    public List<int[]> assign() {
    List<int[]> assignments = new ArrayList<>();
    for (int c = 0; c < customers.size(); c++) {
        double minCost = Double.POSITIVE_INFINITY;
        int bestTruck = -1;
        for (int t = 0; t < trucks.size(); t++) {
            String pathStr = DeliverySearch.path(ds, trucks.get(t), customers.get(c), strategy);
            String[] parts = pathStr.split(";");
            if (parts.length == 3) {
                double cost = Double.parseDouble(parts[1]);
                if (cost < minCost) {
                    minCost = cost;
                    bestTruck = t;
                }
            }
        }
        if (bestTruck != -1) {
            assignments.add(new int[]{bestTruck, c});
        }
    }
    return assignments;
}
}
