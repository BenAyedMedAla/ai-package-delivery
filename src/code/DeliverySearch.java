package code;

import java.util.*;
import java.util.stream.Collectors;

public class DeliverySearch implements Problem<State, Action> {

    // Grid representation
    private final int m; // rows
    private final int n; // columns
    private final int[][] traffic; // m x n, 0=blocked, 1-4=traffic level
    private final List<Tunnel> tunnels; // list of tunnels
    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;

    // For single pathfinding
    private State start;
    private State goal;

    public DeliverySearch(int m, int n, int[][] traffic, List<Tunnel> tunnels,
                          List<State> stores, List<State> customers, List<State> trucks) {
        this.m = m;
        this.n = n;
        this.traffic = traffic;
        this.tunnels = tunnels;
        this.stores = stores;
        this.customers = customers;
        this.trucks = trucks;
    }

    // Set start and goal for pathfinding
    public void setPath(State start, State goal) {
        this.start = start;
        this.goal = goal;
    }

    @Override
    public State initialState() {
        return start;
    }

    @Override
    public boolean isGoal(State state) {
        return state.equals(goal);
    }

    @Override
    public List<Action> actions(State state) {
        List<Action> actions = new ArrayList<>();
        int x = state.x;
        int y = state.y;

        // Check each direction
        if (x > 0 && traffic[x-1][y] > 0) actions.add(Action.UP);
        if (x < m-1 && traffic[x+1][y] > 0) actions.add(Action.DOWN);
        if (y > 0 && traffic[x][y-1] > 0) actions.add(Action.LEFT);
        if (y < n-1 && traffic[x][y+1] > 0) actions.add(Action.RIGHT);

        // Check for tunnel
        for (Tunnel tunnel : tunnels) {
            if (state.equals(tunnel.from) || state.equals(tunnel.to)) {
                actions.add(Action.TUNNEL);
                break; // assume only one tunnel per position
            }
        }

        return actions;
    }

    @Override
    public State result(State state, Action action) {
        int x = state.x;
        int y = state.y;
        switch (action) {
            case UP: return new State(x-1, y);
            case DOWN: return new State(x+1, y);
            case LEFT: return new State(x, y-1);
            case RIGHT: return new State(x, y+1);
            case TUNNEL:
                for (Tunnel tunnel : tunnels) {
                    if (state.equals(tunnel.from)) return tunnel.to;
                    if (state.equals(tunnel.to)) return tunnel.from;
                }
                throw new IllegalStateException("No tunnel found for state: " + state);
            default: throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    @Override
    public double stepCost(State state, Action action, State nextState) {
        if (action == Action.TUNNEL) {
            return 1.0; // fixed cost for tunnel
        }
        // Travel time proportional to traffic level
        int trafficLevel = traffic[nextState.x][nextState.y];
        return trafficLevel; // assuming time = traffic level
    }

    // Static method to generate random grid
    public static String GenGrid() {
        int m = 5;
        int n = 5;
        int[][] traffic = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                traffic[i][j] = 1; // all passable for simplicity
            }
        }

        List<Tunnel> tunnels = new ArrayList<>(); // no tunnels for simplicity

        List<State> stores = List.of(new State(0, 0));
        List<State> customers = List.of(new State(4, 4));
        List<State> trucks = List.of(new State(0, 0));

        // Build string
        StringBuilder sb = new StringBuilder();
        sb.append(m).append(" ").append(n).append("\n");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sb.append(traffic[i][j]);
                if (j < n-1) sb.append(" ");
            }
            sb.append("\n");
        }
        sb.append(tunnels.size()).append("\n");
        sb.append(stores.size()).append("\n");
        for (State s : stores) {
            sb.append(s.x).append(" ").append(s.y).append("\n");
        }
        sb.append(customers.size()).append("\n");
        for (State c : customers) {
            sb.append(c.x).append(" ").append(c.y).append("\n");
        }
        sb.append(trucks.size()).append("\n");
        for (State tr : trucks) {
            sb.append(tr.x).append(" ").append(tr.y).append("\n");
        }
        return sb.toString();
    }

    // Find path from store to customer
    public GenericSearch.SearchResult<State, Action> path(State store, State customer, Strategy strategy) {
        setPath(store, customer);
        Heuristic<State> h1 = new ManhattanHeuristic(customer); // placeholder
        Heuristic<State> h2 = new EuclideanHeuristic(customer); // placeholder
        return GenericSearch.search(this, strategy, h1, h2);
    }

    // Parse grid from string
    public static DeliverySearch fromString(String gridStr) {
        String[] lines = gridStr.split("\n");
        int idx = 0;
        String[] mn = lines[idx++].split(" ");
        int m = Integer.parseInt(mn[0]);
        int n = Integer.parseInt(mn[1]);
        int[][] traffic = new int[m][n];
        for (int i = 0; i < m; i++) {
            String[] row = lines[idx++].split(" ");
            for (int j = 0; j < n; j++) {
                traffic[i][j] = Integer.parseInt(row[j]);
            }
        }
        int numTunnels = Integer.parseInt(lines[idx++]);
        List<Tunnel> tunnels = new ArrayList<>();
        for (int i = 0; i < numTunnels; i++) {
            String[] t = lines[idx++].split(" ");
            State from = new State(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
            State to = new State(Integer.parseInt(t[2]), Integer.parseInt(t[3]));
            tunnels.add(new Tunnel(from, to));
        }
        int numStores = Integer.parseInt(lines[idx++]);
        List<State> stores = new ArrayList<>();
        for (int i = 0; i < numStores; i++) {
            String[] s = lines[idx++].split(" ");
            stores.add(new State(Integer.parseInt(s[0]), Integer.parseInt(s[1])));
        }
        int numCustomers = Integer.parseInt(lines[idx++]);
        List<State> customers = new ArrayList<>();
        for (int i = 0; i < numCustomers; i++) {
            String[] c = lines[idx++].split(" ");
            customers.add(new State(Integer.parseInt(c[0]), Integer.parseInt(c[1])));
        }
        int numTrucks = Integer.parseInt(lines[idx++]);
        List<State> trucks = new ArrayList<>();
        for (int i = 0; i < numTrucks; i++) {
            String[] tr = lines[idx++].split(" ");
            trucks.add(new State(Integer.parseInt(tr[0]), Integer.parseInt(tr[1])));
        }
        return new DeliverySearch(m, n, traffic, tunnels, stores, customers, trucks);
    }

    // Plan: assign trucks to customers and compute routes
    public List<GenericSearch.SearchResult<State, Action>> plan(Strategy strategy, boolean visualize) {
        List<GenericSearch.SearchResult<State, Action>> results = new ArrayList<>();
        DeliveryPlanner planner = new DeliveryPlanner(stores, customers, trucks);
        List<int[]> assignments = planner.assign(); // list of [truckIndex, customerIndex]

        for (int[] assign : assignments) {
            int truckIdx = assign[0];
            int custIdx = assign[1];
            State start = trucks.get(truckIdx);
            State goal = customers.get(custIdx);
            GenericSearch.SearchResult<State, Action> result = path(start, goal, strategy);
            results.add(result);
            if (visualize) {
                System.out.println("Truck " + truckIdx + " to Customer " + custIdx + ": " + result.actions + " cost=" + result.cost + " nodes=" + result.nodesExpanded);
            }
        }
        return results;
    }

    // Solve: main entry for single path
    public static String solve(String initialState, String traffic, Strategy strategy, boolean visualize) {
        DeliverySearch ds = fromString(initialState);
        if (ds.stores.isEmpty() || ds.customers.isEmpty()) {
            return "no path;0;0";
        }
        State start = ds.stores.get(0);
        State goal = ds.customers.get(0);
        GenericSearch.SearchResult<State, Action> result = ds.path(start, goal, strategy);
        if (result.cost == Double.POSITIVE_INFINITY) {
            return "no path;0;0";
        }
        String actions = result.actions.stream().map(Action::toString).collect(Collectors.joining(","));
        String output = actions + ";" + (int)result.cost + ";" + result.nodesExpanded;
        if (visualize) {
            System.out.println("Path found: " + actions);
            System.out.println("Total cost: " + (int)result.cost);
            System.out.println("Nodes expanded: " + result.nodesExpanded);
        }
        return output;
    }

    // Inner class for Tunnel
    public static class Tunnel {
        public final State from;
        public final State to;

        public Tunnel(State from, State to) {
            this.from = from;
            this.to = to;
        }
    }

    // Placeholder heuristics
    private static class ManhattanHeuristic implements Heuristic<State> {
        private final State goal;

        public ManhattanHeuristic(State goal) {
            this.goal = goal;
        }

        @Override
        public double h(State state) {
            return Math.abs(state.x - goal.x) + Math.abs(state.y - goal.y);
        }
    }

    private static class EuclideanHeuristic implements Heuristic<State> {
        private final State goal;

        public EuclideanHeuristic(State goal) {
            this.goal = goal;
        }

        @Override
        public double h(State state) {
            int dx = state.x - goal.x;
            int dy = state.y - goal.y;
            return Math.sqrt(dx*dx + dy*dy);
        }
    }
}
