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

    // Optimal assignment: for each customer, choose truck with lowest cost path
    public List<int[]> assign() {
        List<int[]> assignments = new ArrayList<>();
        Set<Integer> usedTrucks = new HashSet<>();
        for (int c = 0; c < customers.size(); c++) {
            double minCost = Double.POSITIVE_INFINITY;
            int bestTruck = -1;
            for (int t = 0; t < trucks.size(); t++) {
                if (usedTrucks.contains(t)) continue;
                String pathStr = DeliverySearch.path(ds, trucks.get(t), customers.get(c), strategy);
                if (!pathStr.equals("no path;0;0")) {
                    String[] parts = pathStr.split(";");
                    double cost = Double.parseDouble(parts[1]);
                    if (cost < minCost) {
                        minCost = cost;
                        bestTruck = t;
                    }
                }
            }
            if (bestTruck != -1) {
                assignments.add(new int[]{bestTruck, c});
                usedTrucks.add(bestTruck);
            }
        }
        return assignments;
    }
}
