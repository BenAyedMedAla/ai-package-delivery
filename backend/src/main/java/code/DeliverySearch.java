package code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

        // Add tunnel action ONLY if at tunnel entrance
        for (Tunnel tunnel : tunnels) {
            if (tunnel.isEntrance(state)) {
                actions.add(Action.TUNNEL);
                break;
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
            return Math.abs(nextState.x - state.x) + Math.abs(nextState.y - state.y);
        }
        
        Map<State, Integer> neighbors = edgeTraffic.get(state);
        if (neighbors != null) {
            Integer trafficLevel = neighbors.get(nextState);
            if (trafficLevel != null && trafficLevel > 0) {
                return trafficLevel;
            }
        }
        
        return Double.POSITIVE_INFINITY;
    }

    // ------------------ GRID VISUALIZATION ------------------

    /**
     * Visualize the grid with truck positions, stores, customers, and tunnels
     */
    private void visualizeGrid(List<State> truckPositions, int deliveryNum, int truckIdx, int customerIdx) {
        System.out.println("\n" + "═".repeat(Math.max(50, n * 4 + 10)));
        System.out.println("DELIVERY #" + deliveryNum + ": Store" + truckIdx + " → Customer" + customerIdx);
        System.out.println("═".repeat(Math.max(50, n * 4 + 10)));

        // Create grid representation
        char[][] grid = new char[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = '.'; // Empty space
            }
        }

        // Mark stores
        for (int i = 0; i < stores.size(); i++) {
            State s = stores.get(i);
            grid[s.x][s.y] = 'S';
        }

        // Mark customers
        for (int i = 0; i < customers.size(); i++) {
            State c = customers.get(i);
            if (grid[c.x][c.y] == '.') {
                grid[c.x][c.y] = 'C';
            } else {
                grid[c.x][c.y] = 'X'; // Overlapping position
            }
        }

        // Mark tunnel entrances
        for (Tunnel t : tunnels) {
            if (grid[t.from.x][t.from.y] == '.') {
                grid[t.from.x][t.from.y] = 'T';
            }
            if (grid[t.to.x][t.to.y] == '.') {
                grid[t.to.x][t.to.y] = 'T';
            }
        }

        // Mark trucks (overwrites other markers to show current position)
        for (int i = 0; i < truckPositions.size(); i++) {
            State t = truckPositions.get(i);
            grid[t.x][t.y] = (char) ('0' + i); // Truck 0, 1, 2
        }

        // Print grid with borders
        System.out.print("    ");
        for (int j = 0; j < n; j++) {
            System.out.printf("%3d ", j);
        }
        System.out.println();
        System.out.print("   ┌");
        for (int j = 0; j < n; j++) {
            System.out.print("───");
            if (j < n - 1) System.out.print("┬");
        }
        System.out.println("┐");

        for (int i = 0; i < m; i++) {
            System.out.printf("%2d │", i);
            for (int j = 0; j < n; j++) {
                System.out.printf(" %c ", grid[i][j]);
                if (j < n - 1) System.out.print("│");
            }
            System.out.println("│");
            
            if (i < m - 1) {
                System.out.print("   ├");
                for (int j = 0; j < n; j++) {
                    System.out.print("───");
                    if (j < n - 1) System.out.print("┼");
                }
                System.out.println("┤");
            }
        }

        System.out.print("   └");
        for (int j = 0; j < n; j++) {
            System.out.print("───");
            if (j < n - 1) System.out.print("┴");
        }
        System.out.println("┘");

        // Legend
        System.out.println("\nLegend:");
        System.out.println("  0,1,2 = Trucks  |  S = Store  |  C = Customer  |  T = Tunnel  |  . = Empty");
        System.out.println();
    }

    /**
     * Visualize step-by-step path execution
     */
    private void visualizePathExecution(State startPos, List<Action> actions, int truckIdx, int customerIdx) {
        State current = startPos;
        List<State> allTruckPositions = new ArrayList<>(trucks);
        
        System.out.println("\n" + "▼".repeat(60));
        System.out.println("STEP-BY-STEP: Truck " + truckIdx + " delivering to Customer " + customerIdx);
        System.out.println("▼".repeat(60));
        
        // Initial position
        System.out.println("\nStep 0: Initial Position");
        visualizeGrid(allTruckPositions, 0, truckIdx, customerIdx);
        
        try {
            Thread.sleep(500); // Pause for visualization
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Execute each action
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            current = result(current, action);
            allTruckPositions.set(truckIdx, current);
            
            System.out.println("\nStep " + (i + 1) + ": Action = " + action);
            System.out.println("  Truck " + truckIdx + " moved to " + current);
            visualizeGrid(allTruckPositions, i + 1, truckIdx, customerIdx);
            
            try {
                Thread.sleep(500); // Pause between steps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("\n✓ DELIVERY COMPLETE!");
        System.out.println("▲".repeat(60) + "\n");
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

        // Store locations are IMPLIED by S
        List<State> stores = new ArrayList<>();
        if (S >= 1) stores.add(new State(0, 0));
        if (S >= 2) stores.add(new State(m - 1, n - 1));
        if (S >= 3) stores.add(new State(m - 1, 0));

        // Parse tunnel locations
        List<Tunnel> tunnels = new ArrayList<>();
        if (parts.length > 5 && !parts[5].isEmpty()) {
            String[] tStr = parts[5].split(",");
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

            // Include all edges, including blocked ones (cost = 0)
            edgeTraffic.putIfAbsent(from, new HashMap<>());
            edgeTraffic.get(from).put(to, cost);
        }

        List<State> trucks = new ArrayList<>(stores);

        return new DeliverySearch(m, n, edgeTraffic, tunnels, stores, customers, trucks,
                new ManhattanHeuristic(), new TrafficAwareHeuristic(1));
    }

    // ------------------ PLANNING ------------------

    public String plan(Strategy strategy, boolean visualize) {
        DeliveryPlanner planner = new DeliveryPlanner(stores, customers, trucks, this, strategy);
        List<int[]> assignments = planner.assign();

        StringBuilder sb = new StringBuilder();
        
        // Track current truck positions
        List<State> currentTruckPositions = new ArrayList<>(trucks);
        
        int deliveryNum = 1;

        for (int[] assignment : assignments) {
            int truckIdx = assignment[0];
            int customerIdx = assignment[1];

            State startPos = currentTruckPositions.get(truckIdx);
            State goalPos = customers.get(customerIdx);

            // Get path
            setPath(startPos, goalPos);
            GenericSearch.SearchResult<State, Action> result = 
                    GenericSearch.search(this, strategy, h1, h2);

            if (result.cost == Double.POSITIVE_INFINITY) {
                System.err.println("Warning: No path found from Store" + truckIdx + 
                                 " to Customer" + customerIdx);
                continue;
            }

            String deliveryPath = result.actions.stream()
                    .map(Action::toString)
                    .collect(Collectors.joining(","));
            int deliveryCost = (int) result.cost;
            int deliveryNodes = result.nodesExpanded;

            // Output format
            sb.append("(Store").append(truckIdx)
              .append(",Customer").append(customerIdx).append(");")
              .append(deliveryPath).append(";")
              .append(deliveryCost).append(";")
              .append(deliveryNodes).append("\n");

            if (visualize) {
                // Show step-by-step grid visualization
                visualizePathExecution(startPos, result.actions, truckIdx, customerIdx);
            }

            // Update truck position after delivery
            currentTruckPositions.set(truckIdx, goalPos);
            
            // Truck returns to store (update position, but don't visualize return)
            State storeLocation = stores.get(truckIdx);
            currentTruckPositions.set(truckIdx, storeLocation);
            
            deliveryNum++;
        }

        return sb.toString().trim();
    }

    public static String plan(String initialState, String traffic, String strategy, boolean visualize) {
        DeliverySearch ds = fromStrings(initialState, traffic);
        return ds.plan(Strategy.fromString(strategy), visualize);
    }

    public static String solve(String initialState, String traffic, String strategy, boolean visualize) {
        return plan(initialState, traffic, strategy, visualize);
    }

    // Getter methods for accessing private fields
    public List<State> getStores() { return stores; }
    public List<State> getCustomers() { return customers; }
    public List<State> getTrucks() { return trucks; }
    public Heuristic<State> getH1() { return h1; }
    public Heuristic<State> getH2() { return h2; }

    // ------------------ RANDOM GENERATORS ------------------

    public static String GenGrid() {
        Random rand = new Random();
        int m = rand.nextInt(8) + 3;
        int n = rand.nextInt(8) + 3;
        int numCustomers = rand.nextInt(10) + 1;
        int numStores = rand.nextInt(3) + 1;
        return GenGrid(m, n, numCustomers, numStores);
    }

    public static String GenGrid(int m, int n, int numCustomers, int numStores) {
        List<State> customers = new ArrayList<>();
        Set<String> usedPositions = new HashSet<>();
        Random rand = new Random();

        if (numStores >= 1) usedPositions.add("0,0");
        if (numStores >= 2) usedPositions.add((m-1) + "," + (n-1));
        if (numStores >= 3) usedPositions.add((m-1) + ",0");

        for (int i = 0; i < numCustomers; i++) {
            int x, y;
            do {
                x = rand.nextInt(m);
                y = rand.nextInt(n);
            } while (usedPositions.contains(x + "," + y));
            usedPositions.add(x + "," + y);
            customers.add(new State(x, y));
        }

        List<Tunnel> tunnels = new ArrayList<>();
        int numTunnels = rand.nextInt(5);
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

        StringBuilder sb = new StringBuilder();
        sb.append(m).append(";").append(n).append(";")
          .append(numCustomers).append(";").append(numStores).append(";");

        for (int i = 0; i < customers.size(); i++) {
            State c = customers.get(i);
            sb.append(c.x).append(",").append(c.y);
            if (i < customers.size() - 1) sb.append(",");
        }
        sb.append(";");

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

        // Generate costs for "canonical" directions only (right and down)
        // This ensures bidirectional edges have the same cost
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // Generate cost for moving right (if possible)
                if (j < n - 1) {
                    int cost = rand.nextInt(5); // 0-4, where 0 = blocked
                    // Add both directions with same cost
                    sb.append(i).append(",").append(j).append(",")
                      .append(i).append(",").append(j + 1).append(",")
                      .append(cost).append(";");
                    sb.append(i).append(",").append(j + 1).append(",")
                      .append(i).append(",").append(j).append(",")
                      .append(cost).append(";");
                }

                // Generate cost for moving down (if possible)
                if (i < m - 1) {
                    int cost = rand.nextInt(5); // 0-4, where 0 = blocked
                    // Add both directions with same cost
                    sb.append(i).append(",").append(j).append(",")
                      .append(i + 1).append(",").append(j).append(",")
                      .append(cost).append(";");
                    sb.append(i + 1).append(",").append(j).append(",")
                      .append(i).append(",").append(j).append(",")
                      .append(cost).append(";");
                }
            }
        }

        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}