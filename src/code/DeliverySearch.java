package code;

import java.util.*;
import java.util.stream.Collectors;

public class DeliverySearch extends GenericSearch implements Problem<State, Action> {

    private final int m; 
    private final int n; 
    private final Map<State, Map<State, Integer>> edgeTraffic;
    private final List<Tunnel> tunnels;
    private final List<State> stores;
    private final List<State> customers;
    private final List<State> trucks;

    private final Heuristic<State> h1;
    private final Heuristic<State> h2;

    private State start;
    private State goal;

    public DeliverySearch(int m, int n, Map<State, Map<State, Integer>> edgeTraffic, 
                          List<Tunnel> tunnels, List<State> stores, List<State> customers, 
                          List<State> trucks, Heuristic<State> h1, Heuristic<State> h2) {
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

    public void setPath(State start, State goal) {
        this.start = start;
        this.goal = goal;
        if (h1 instanceof ManhattanHeuristic) {
            ((ManhattanHeuristic) h1).setGoal(goal, tunnels);
        }
        if (h2 instanceof TrafficAwareHeuristic) {
            ((TrafficAwareHeuristic) h2).setGoal(goal, tunnels);
        }
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

        // Add regular movement actions
        if (neighbors != null) {
            State up = new State(state.x - 1, state.y);
            if (neighbors.containsKey(up) && neighbors.get(up) > 0) {
                actions.add(Action.UP);
            }

            State down = new State(state.x + 1, state.y);
            if (neighbors.containsKey(down) && neighbors.get(down) > 0) {
                actions.add(Action.DOWN);
            }

            State left = new State(state.x, state.y - 1);
            if (neighbors.containsKey(left) && neighbors.get(left) > 0) {
                actions.add(Action.LEFT);
            }

            State right = new State(state.x, state.y + 1);
            if (neighbors.containsKey(right) && neighbors.get(right) > 0) {
                actions.add(Action.RIGHT);
            }
        }

        // ✅ FIX: Add tunnel action ONLY if at tunnel entrance
        for (Tunnel tunnel : tunnels) {
            if (tunnel.isEntrance(state)) {
                actions.add(Action.TUNNEL);
                break; // Only one tunnel action needed per state
            }
        }

        return actions;
    }

    @Override
    public State result(State state, Action action) {
        switch (action) {
            case UP:
                return new State(state.x - 1, state.y);
            case DOWN:
                return new State(state.x + 1, state.y);
            case LEFT:
                return new State(state.x, state.y - 1);
            case RIGHT:
                return new State(state.x, state.y + 1);
            case TUNNEL:
                // ✅ FIX: Find the tunnel and return other end
                for (Tunnel tunnel : tunnels) {
                    State otherEnd = tunnel.getOtherEnd(state);
                    if (otherEnd != null) {
                        return otherEnd;
                    }
                }
                throw new IllegalStateException("No tunnel found for state: " + state);
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    @Override
    public double stepCost(State state, Action action, State nextState) {
        if (action == Action.TUNNEL) {
            // ✅ FIX: Tunnel cost is Manhattan distance between entrances
            // This is correct as per spec
            return Math.abs(nextState.x - state.x) + Math.abs(nextState.y - state.y);
        }
        
        // Regular movement cost is traffic level
        Map<State, Integer> neighbors = edgeTraffic.get(state);
        if (neighbors != null) {
            Integer trafficLevel = neighbors.get(nextState);
            if (trafficLevel != null && trafficLevel > 0) {
                return trafficLevel;
            }
        }
        
        return Double.POSITIVE_INFINITY;
    }

    // ------------------ PATHFINDING ------------------

    public GenericSearch.SearchResult<State, Action> path(State store, State customer, Strategy strategy) {
        setPath(store, customer);
        return GenericSearch.search(this, strategy, h1, h2);
    }

    public static String path(DeliverySearch ds, State start, State goal, Strategy strategy) {
        ds.setPath(start, goal);
        GenericSearch.SearchResult<State, Action> result = 
                GenericSearch.search(ds, strategy, ds.h1, ds.h2);

        if (result.cost == Double.POSITIVE_INFINITY) {
            return "no path;0;0";
        }

        String actions = result.actions.stream()
                .map(Action::toString)
                .collect(Collectors.joining(","));
        
        return actions + ";" + (int) result.cost + ";" + result.nodesExpanded;
    }

    // ------------------ PARSING ------------------

    public static DeliverySearch fromStrings(String initialState, String trafficStr) {
        String[] parts = initialState.split(";");

        int m = Integer.parseInt(parts[0]);
        int n = Integer.parseInt(parts[1]);
        int P = Integer.parseInt(parts[2]);
        int S = Integer.parseInt(parts[3]);

        // Parse customer locations
        List<State> customers = new ArrayList<>();
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] custStr = parts[4].split(",");
            for (int i = 0; i + 1 < custStr.length; i += 2) {
                customers.add(new State(
                    Integer.parseInt(custStr[i]), 
                    Integer.parseInt(custStr[i + 1])
                ));
            }
        }

        // ✅ Parse store locations (default positions)
        List<State> stores = new ArrayList<>();
        if (S >= 1) stores.add(new State(0, 0));
        if (S >= 2) stores.add(new State(m - 1, n - 1));
        if (S >= 3) stores.add(new State(m - 1, 0));

        // ✅ FIX: Parse tunnel locations correctly
        // Format: TunnelX_1,TunnelY_1,TunnelX_2,TunnelY_2,...
        // Each tunnel needs 4 coordinates (2 points)
        List<Tunnel> tunnels = new ArrayList<>();
        if (parts.length > 5 && !parts[5].isEmpty()) {
            String[] tStr = parts[5].split(",");
            // Need exactly 4 coordinates per tunnel
            for (int i = 0; i + 3 < tStr.length; i += 4) {
                try {
                    int x1 = Integer.parseInt(tStr[i]);
                    int y1 = Integer.parseInt(tStr[i + 1]);
                    int x2 = Integer.parseInt(tStr[i + 2]);
                    int y2 = Integer.parseInt(tStr[i + 3]);
                    
                    State entrance1 = new State(x1, y1);
                    State entrance2 = new State(x2, y2);
                    
                    tunnels.add(new Tunnel(entrance1, entrance2));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Failed to parse tunnel at index " + i);
                }
            }
        }

        // Parse traffic information
        Map<State, Map<State, Integer>> edgeTraffic = new HashMap<>();
        for (String edge : trafficStr.split(";")) {
            String[] e = edge.split(",");
            if (e.length < 5) continue;
            
            State from = new State(Integer.parseInt(e[0]), Integer.parseInt(e[1]));
            State to = new State(Integer.parseInt(e[2]), Integer.parseInt(e[3]));
            int cost = Integer.parseInt(e[4]);

            // Skip blocked roads (traffic = 0)
            if (cost == 0) continue;

            // Add the edge (GenTraffic already creates both directions)
            edgeTraffic.putIfAbsent(from, new HashMap<>());
            edgeTraffic.get(from).put(to, cost);
        }

        List<State> trucks = new ArrayList<>(stores);

        // Use Manhattan and Traffic-Aware heuristics
        return new DeliverySearch(m, n, edgeTraffic, tunnels, stores, customers, trucks,
                new ManhattanHeuristic(), new TrafficAwareHeuristic(1));
    }

    // ------------------ PLANNING ------------------

    /**
     * ✅ FIXED: Only output delivery path, not return trip
     * ✅ FIXED: Use "Store" instead of "Truck"
     * ✅ FIXED: Only show delivery cost/nodes
     */
    public String plan(Strategy strategy, boolean visualize) {
        DeliveryPlanner planner = new DeliveryPlanner(stores, customers, trucks, this, strategy);
        List<int[]> assignments = planner.assign();

        StringBuilder sb = new StringBuilder();
        
        // Track current truck positions (initially at stores)
        List<State> currentTruckPositions = new ArrayList<>(trucks);

        for (int[] assignment : assignments) {
            int truckIdx = assignment[0];
            int customerIdx = assignment[1];

            State start = currentTruckPositions.get(truckIdx);
            State goal = customers.get(customerIdx);

            // Path from current position to customer (DELIVERY ONLY)
            String deliveryResult = path(this, start, goal, strategy);

            if (deliveryResult.equals("no path;0;0")) {
                System.err.println("Warning: No path found from Store" + truckIdx + 
                                 " to Customer" + customerIdx);
                continue;
            }

            String[] deliveryParts = deliveryResult.split(";");
            String deliveryPath = deliveryParts[0];
            int deliveryCost = Integer.parseInt(deliveryParts[1]);
            int deliveryNodes = Integer.parseInt(deliveryParts[2]);

            // ✅ Output format: (Store#,Customer#);path;cost;nodes
            sb.append("(Store").append(truckIdx)
              .append(",Customer").append(customerIdx).append(");")
              .append(deliveryPath).append(";")
              .append(deliveryCost).append(";")
              .append(deliveryNodes).append("\n");

            if (visualize) {
                System.out.println("═".repeat(60));
                System.out.println("Store " + truckIdx + " → Customer " + customerIdx);
                System.out.println("  From: " + start + " To: " + goal);
                System.out.println("  Path: " + deliveryPath);
                System.out.println("  Cost: " + deliveryCost + " | Nodes Expanded: " + deliveryNodes);
                System.out.println("═".repeat(60));
            }

            // Calculate return path (internal only, for truck position tracking)
            State storeLocation = stores.get(truckIdx);
            path(this, goal, storeLocation, strategy); // Calculate but don't use result
            
            // Truck returns to store, ready for next delivery
            currentTruckPositions.set(truckIdx, storeLocation);
        }

        return sb.toString().trim();
    }

    /**
     * ✅ ADDED: Static plan method matching spec signature
     */
    public static String plan(String initialState, String traffic, String strategy, boolean visualize) {
        DeliverySearch ds = fromStrings(initialState, traffic);
        return ds.plan(Strategy.fromString(strategy), visualize);
    }

    public static String solve(String initialState, String traffic, String strategy, boolean visualize) {
        return plan(initialState, traffic, strategy, visualize);
    }

    // ------------------ RANDOM GENERATORS ------------------

    /**
     * ✅ ADDED: Parameterless GenGrid() as per spec
     */
    public static String GenGrid() {
        Random rand = new Random();
        
        // Random grid size (minimum 3x3, maximum 10x10)
        int m = rand.nextInt(8) + 3; // 3-10
        int n = rand.nextInt(8) + 3; // 3-10
        
        // Random number of packages/customers (1-10 as per spec)
        int numCustomers = rand.nextInt(10) + 1;
        
        // Random number of stores (1-3 as per spec)
        int numStores = rand.nextInt(3) + 1;
        
        return GenGrid(m, n, numCustomers, numStores);
    }

    /**
     * Generate grid with specific parameters
     */
    public static String GenGrid(int m, int n, int numCustomers, int numStores) {
        List<State> customers = new ArrayList<>();
        Set<String> usedPositions = new HashSet<>();
        Random rand = new Random();

        // Reserve store positions
        if (numStores >= 1) usedPositions.add("0,0");
        if (numStores >= 2) usedPositions.add((m-1) + "," + (n-1));
        if (numStores >= 3) usedPositions.add((m-1) + ",0");

        // Generate random customer positions
        for (int i = 0; i < numCustomers; i++) {
            int x, y;
            do {
                x = rand.nextInt(m);
                y = rand.nextInt(n);
            } while (usedPositions.contains(x + "," + y));
            usedPositions.add(x + "," + y);
            customers.add(new State(x, y));
        }

        // ✅ Generate tunnels (0-2 tunnels)
        List<Tunnel> tunnels = new ArrayList<>();
        int numTunnels = rand.nextInt(3); // 0, 1, or 2 tunnels
        for (int i = 0; i < numTunnels; i++) {
            int x1, y1, x2, y2;
            do {
                x1 = rand.nextInt(m);
                y1 = rand.nextInt(n);
            } while (usedPositions.contains(x1 + "," + y1));
            
            do {
                x2 = rand.nextInt(m);
                y2 = rand.nextInt(n);
            } while (usedPositions.contains(x2 + "," + y2) || (x1 == x2 && y1 == y2));
            
            tunnels.add(new Tunnel(new State(x1, y1), new State(x2, y2)));
        }

        // Build output string
        // Format: m;n;P;S;customerLocations;tunnelLocations
        StringBuilder sb = new StringBuilder();
        sb.append(m).append(";").append(n).append(";")
          .append(numCustomers).append(";").append(numStores).append(";");

        // Customer locations
        for (int i = 0; i < customers.size(); i++) {
            State c = customers.get(i);
            sb.append(c.x).append(",").append(c.y);
            if (i < customers.size() - 1) sb.append(",");
        }
        sb.append(";");

        // Tunnel locations (4 coordinates per tunnel)
        for (int i = 0; i < tunnels.size(); i++) {
            Tunnel t = tunnels.get(i);
            sb.append(t.from.x).append(",").append(t.from.y).append(",")
              .append(t.to.x).append(",").append(t.to.y);
            if (i < tunnels.size() - 1) sb.append(",");
        }

        return sb.toString();
    }

    public static String GenTraffic(int m, int n) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // Add edge to up neighbor
                if (i > 0) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i - 1).append(",").append(j).append(",")
                      .append(rand.nextInt(4) + 1).append(";");
                }
                // Add edge to down neighbor
                if (i < m - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i + 1).append(",").append(j).append(",")
                      .append(rand.nextInt(4) + 1).append(";");
                }
                // Add edge to left neighbor
                if (j > 0) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j - 1).append(",")
                      .append(rand.nextInt(4) + 1).append(";");
                }
                // Add edge to right neighbor
                if (j < n - 1) {
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j + 1).append(",")
                      .append(rand.nextInt(4) + 1).append(";");
                }
            }
        }
        
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}