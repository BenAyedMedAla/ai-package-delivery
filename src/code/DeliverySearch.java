package code;

import java.util.*;
import java.util.stream.Collectors;

public class DeliverySearch extends GenericSearch implements Problem<State, Action> {

    // Grid representation
    private final int m; // rows
    private final int n; // columns
    private final Map<State, Map<State, Integer>> edgeTraffic; // from -> to -> traffic
    private final List<Tunnel> tunnels; // list of tunnels
    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;

    // Heuristics
    private final Heuristic<State> h1;
    private final Heuristic<State> h2;

    // For single pathfinding
    private State start;
    private State goal;

    public DeliverySearch(int m, int n, Map<State, Map<State, Integer>> edgeTraffic, List<Tunnel> tunnels,
                          List<State> stores, List<State> customers, List<State> trucks,
                          Heuristic<State> h1, Heuristic<State> h2) {
        this.m = m;
        this.n = n;
        this.edgeTraffic = edgeTraffic;
        this.tunnels = tunnels;
        this.stores = stores;
        this.customers = customers;
        this.trucks = trucks;
        this.h1 = h1;
        this.h2 = h2;
    }

    // Set start and goal for pathfinding
    public void setPath(State start, State goal) {
        this.start = start;
        this.goal = goal;
        if (h1 instanceof ManhattanHeuristic) ((ManhattanHeuristic) h1).setGoal(goal, tunnels);
        if (h2 instanceof EuclideanHeuristic) ((EuclideanHeuristic) h2).setGoal(goal, tunnels);
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
        Map<State, Integer> neighbors = edgeTraffic.get(state);
        if (neighbors != null) {
            State up = new State(state.x-1, state.y);
            if (neighbors.containsKey(up)) actions.add(Action.UP);
            State down = new State(state.x+1, state.y);
            if (neighbors.containsKey(down)) actions.add(Action.DOWN);
            State left = new State(state.x, state.y-1);
            if (neighbors.containsKey(left)) actions.add(Action.LEFT);
            State right = new State(state.x, state.y+1);
            if (neighbors.containsKey(right)) actions.add(Action.RIGHT);
        }

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
            // Manhattan distance
            return Math.abs(nextState.x - state.x) + Math.abs(nextState.y - state.y);
        }
        // Travel time proportional to traffic level
        Map<State, Integer> neighbors = edgeTraffic.get(state);
        if (neighbors != null) {
            Integer trafficLevel = neighbors.get(nextState);
            if (trafficLevel != null && trafficLevel != 0) {
                return trafficLevel;
            }
        }
        return Double.POSITIVE_INFINITY; // no edge or blocked
    }

    // Static method to generate random grid - returns initialState string
    public static String GenGrid() {
        int m = 5;
        int n = 5;
        List<State> customers = List.of(new State(4, 4), new State(2, 2));
        List<Tunnel> tunnels = List.of(new Tunnel(new State(0, 0), new State(4, 4)));
        int P = customers.size();
        int S = 1; // number of stores, but not used

        StringBuilder sb = new StringBuilder();
        sb.append(m).append(";").append(n).append(";").append(P).append(";").append(S).append(";");
        for (State c : customers) {
            sb.append(c.x).append(",").append(c.y).append(",");
        }
        if (!customers.isEmpty()) sb.setLength(sb.length() - 1); // remove last comma
        sb.append(";");
        for (Tunnel t : tunnels) {
            sb.append(t.from.x).append(",").append(t.from.y).append(",").append(t.to.x).append(",").append(t.to.y).append(",");
        }
        if (!tunnels.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    // Generate traffic string
    public static String GenTraffic() {
        // For simplicity, all adjacent cells with traffic 1
        StringBuilder sb = new StringBuilder();
        int m = 5, n = 5;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i > 0) sb.append(i).append(",").append(j).append(",").append(i-1).append(",").append(j).append(",1;");
                if (i < m-1) sb.append(i).append(",").append(j).append(",").append(i+1).append(",").append(j).append(",1;");
                if (j > 0) sb.append(i).append(",").append(j).append(",").append(i).append(",").append(j-1).append(",1;");
                if (j < n-1) sb.append(i).append(",").append(j).append(",").append(i).append(",").append(j+1).append(",1;");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1); // remove last ;
        return sb.toString();
    }

    // Find path from store to customer
    public GenericSearch.SearchResult<State, Action> path(State store, State customer, Strategy strategy) {
        setPath(store, customer);
        Heuristic<State> h1 = new ManhattanHeuristic();
        Heuristic<State> h2 = new EuclideanHeuristic();
        return GenericSearch.search(this, strategy, h1, h2);
    }

    // Parse grid from string (old format)
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
        // Build edgeTraffic from traffic[][]
        Map<State, Map<State, Integer>> edgeTraffic = new HashMap<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (traffic[i][j] > 0) {
                    State from = new State(i, j);
                    edgeTraffic.putIfAbsent(from, new HashMap<>());
                    // add adjacent
                    if (i > 0 && traffic[i-1][j] > 0) {
                        State to = new State(i-1, j);
                        edgeTraffic.get(from).put(to, traffic[i-1][j]);
                        edgeTraffic.putIfAbsent(to, new HashMap<>());
                        edgeTraffic.get(to).put(from, traffic[i][j]);
                    }
                    if (i < m-1 && traffic[i+1][j] > 0) {
                        State to = new State(i+1, j);
                        edgeTraffic.get(from).put(to, traffic[i+1][j]);
                        edgeTraffic.putIfAbsent(to, new HashMap<>());
                        edgeTraffic.get(to).put(from, traffic[i][j]);
                    }
                    if (j > 0 && traffic[i][j-1] > 0) {
                        State to = new State(i, j-1);
                        edgeTraffic.get(from).put(to, traffic[i][j-1]);
                        edgeTraffic.putIfAbsent(to, new HashMap<>());
                        edgeTraffic.get(to).put(from, traffic[i][j]);
                    }
                    if (j < n-1 && traffic[i][j+1] > 0) {
                        State to = new State(i, j+1);
                        edgeTraffic.get(from).put(to, traffic[i][j+1]);
                        edgeTraffic.putIfAbsent(to, new HashMap<>());
                        edgeTraffic.get(to).put(from, traffic[i][j]);
                    }
                }
            }
        }
        Heuristic<State> h1 = new ManhattanHeuristic();
        Heuristic<State> h2 = new EuclideanHeuristic();
        return new DeliverySearch(m, n, edgeTraffic, tunnels, stores, customers, trucks, h1, h2);
    }

    // Parse from new format
    public static DeliverySearch fromStrings(String initialState, String trafficStr) {
        String[] parts = initialState.split(";");
        int m = Integer.parseInt(parts[0]);
        int n = Integer.parseInt(parts[1]);
        int P = Integer.parseInt(parts[2]);
        int S = Integer.parseInt(parts[3]);
        List<State> customers = new ArrayList<>();
        String[] custStr = parts[4].split(",");
        for (int i = 0; i < custStr.length; i += 2) {
            int x = Integer.parseInt(custStr[i]);
            int y = Integer.parseInt(custStr[i+1]);
            customers.add(new State(x, y));
        }
        List<Tunnel> tunnels = new ArrayList<>();
        if (parts.length > 5) {
            String[] tunnelStr = parts[5].split(",");
            for (int i = 0; i < tunnelStr.length; i += 4) {
                int x1 = Integer.parseInt(tunnelStr[i]);
                int y1 = Integer.parseInt(tunnelStr[i+1]);
                int x2 = Integer.parseInt(tunnelStr[i+2]);
                int y2 = Integer.parseInt(tunnelStr[i+3]);
                tunnels.add(new Tunnel(new State(x1, y1), new State(x2, y2)));
            }
        }
        // Parse traffic
        Map<State, Map<State, Integer>> edgeTraffic = new HashMap<>();
        String[] edges = trafficStr.split(";");
        for (String edge : edges) {
            String[] e = edge.split(",");
            int sx = Integer.parseInt(e[0]);
            int sy = Integer.parseInt(e[1]);
            int dx = Integer.parseInt(e[2]);
            int dy = Integer.parseInt(e[3]);
            int t = Integer.parseInt(e[4]);
            if (t == 0) continue; // blocked: do not add edge
            State from = new State(sx, sy);
            State to = new State(dx, dy);
            edgeTraffic.putIfAbsent(from, new HashMap<>());
            edgeTraffic.get(from).put(to, t);
            edgeTraffic.putIfAbsent(to, new HashMap<>());
            edgeTraffic.get(to).put(from, t);
        }
        // Generate stores and trucks based on S
        List<State> stores = new ArrayList<>();
        if (S >= 1) stores.add(new State(0, 0));
        if (S >= 2) stores.add(new State(m-1, n-1));
        if (S >= 3) stores.add(new State(m-1, 0));
        List<State> trucks = new ArrayList<>(stores);
        Heuristic<State> h1 = new ManhattanHeuristic();
        Heuristic<State> h2 = new EuclideanHeuristic();
        return new DeliverySearch(m, n, edgeTraffic, tunnels, stores, customers, trucks, h1, h2);
    }

    // Plan: assign trucks to customers and compute routes - returns formatted string
    public String plan(Strategy strategy, boolean visualize) {
        DeliveryPlanner planner = new DeliveryPlanner(stores, customers, trucks, this, strategy);
        List<int[]> assignments = planner.assign(); // list of [truckIndex, customerIndex]

        StringBuilder sb = new StringBuilder();
        for (int[] assign : assignments) {
            int truckIdx = assign[0];
            int custIdx = assign[1];
            State start = trucks.get(truckIdx);
            State goal = customers.get(custIdx);
            String pathStr = path(this, start, goal, strategy);
            if (pathStr.equals("no path;0;0")) continue;
            String[] parts = pathStr.split(";");
            String actions = parts[0];
            double pathCost = Double.parseDouble(parts[1]);
            int nodes = Integer.parseInt(parts[2]);
            sb.append("(Truck").append(truckIdx).append(",Customer").append(custIdx).append(");")
              .append(actions).append(";").append((int)pathCost).append(";").append(nodes).append("\n");
            if (visualize) {
                System.out.println("Truck " + truckIdx + " to Customer " + custIdx + ": " + actions + " cost=" + (int)pathCost + " nodes=" + nodes);
            }
        }
        return sb.toString().trim();
    }

    // Solve: main entry
    public static String solve(String initialState, String traffic, String strategy, boolean visualize) {
        Strategy strat = Strategy.fromString(strategy);
        DeliverySearch ds = fromStrings(initialState, traffic);
        return ds.plan(strat, visualize);
    }

    // path: returns "plan;cost;nodesExpanded"
    public static String path(DeliverySearch ds, State start, State goal, Strategy strategy) {
        ds.setPath(start, goal);
        GenericSearch.SearchResult<State, Action> result = GenericSearch.search(ds, strategy, ds.h1, ds.h2);
        if (result.cost == Double.POSITIVE_INFINITY) {
            return "no path;0;0";
        }
        String actions = result.actions.stream().map(Action::toString).collect(Collectors.joining(","));
        return actions + ";" + (int)result.cost + ";" + result.nodesExpanded;
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

    // Heuristics
    private static class ManhattanHeuristic implements Heuristic<State> {
        private State goal;

        public void setGoal(State goal, List<Tunnel> tunnels) {
            this.goal = goal;
        }

        @Override
        public double h(State state) {
            if (goal == null) return 0;
            return Math.abs(state.x - goal.x) + Math.abs(state.y - goal.y);
        }
    }

    private static class EuclideanHeuristic implements Heuristic<State> {
        private State goal;
        private List<Tunnel> tunnels;

        public void setGoal(State goal, List<Tunnel> tunnels) {
            this.goal = goal;
            this.tunnels = tunnels;
        }

        @Override
        public double h(State state) {
            if (goal == null) return 0;
            double direct = Math.sqrt((state.x - goal.x)*(state.x - goal.x) + (state.y - goal.y)*(state.y - goal.y));
            double minTunnel = Double.POSITIVE_INFINITY;
            if (tunnels != null) {
                for (Tunnel t : tunnels) {
                    // manhattan to t.from + euclidean from t.to to goal
                    double viaFrom = Math.abs(state.x - t.from.x) + Math.abs(state.y - t.from.y) +
                                     Math.sqrt((t.to.x - goal.x)*(t.to.x - goal.x) + (t.to.y - goal.y)*(t.to.y - goal.y));
                    // manhattan to t.to + euclidean from t.from to goal
                    double viaTo = Math.abs(state.x - t.to.x) + Math.abs(state.y - t.to.y) +
                                   Math.sqrt((t.from.x - goal.x)*(t.from.x - goal.x) + (t.from.y - goal.y)*(t.from.y - goal.y));
                    minTunnel = Math.min(minTunnel, Math.min(viaFrom, viaTo));
                }
            }
            return Math.min(direct, minTunnel);
        }
    }
}
