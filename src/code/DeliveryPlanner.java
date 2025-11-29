package code;

import java.util.*;

public class DeliveryPlanner {
    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;

    public DeliveryPlanner(List<State> stores, List<State> customers, List<State> trucks) {
        this.stores = stores;
        this.customers = customers;
        this.trucks = trucks;
    }

    // Simple assignment: assign trucks to customers in order
    public List<int[]> assign() {
        List<int[]> assignments = new ArrayList<>();
        int num = Math.min(trucks.size(), customers.size());
        for (int i = 0; i < num; i++) {
            assignments.add(new int[]{i, i});
        }
        return assignments;
    }
}
