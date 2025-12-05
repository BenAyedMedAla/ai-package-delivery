package code;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DeliveryController {

    private String initialState;
    private String traffic;

    @PostMapping("/generate-grid")
    public Grid generateGrid(@RequestBody GridRequest request) {
        int m = request.getRows();
        int n = request.getColumns();
        int numCustomers = request.getNumCustomers();
        int numStores = request.getNumStores();

        initialState = DeliverySearch.GenGrid(m, n, numCustomers, numStores);
        traffic = DeliverySearch.GenTraffic(m, n);

        // Parse initialState to build Grid DTO
        String[] parts = initialState.split(";");

        int rows = Integer.parseInt(parts[0]);
        int columns = Integer.parseInt(parts[1]);
        int P = Integer.parseInt(parts[2]);
        int S = Integer.parseInt(parts[3]);

        List<Position> customers = new ArrayList<>();
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] custStr = parts[4].split(",");
            for (int i = 0; i + 1 < custStr.length; i += 2) {
                customers.add(new Position(
                    Integer.parseInt(custStr[i]),
                    Integer.parseInt(custStr[i + 1])
                ));
            }
        }

        List<Position> stores = new ArrayList<>();
        if (S >= 1) stores.add(new Position(0, 0));
        if (S >= 2) stores.add(new Position(rows - 1, columns - 1));
        if (S >= 3) stores.add(new Position(rows - 1, 0));
        if (S >= 4) stores.add(new Position(0, columns - 1));
        if (S >= 5) stores.add(new Position(rows / 2, columns / 2));

        List<TunnelDto> tunnels = new ArrayList<>();
        if (parts.length > 5 && !parts[5].isEmpty()) {
            String[] tStr = parts[5].split(",");
            for (int i = 0; i + 3 < tStr.length; i += 4) {
                Position from = new Position(
                    Integer.parseInt(tStr[i]),
                    Integer.parseInt(tStr[i + 1])
                );
                Position to = new Position(
                    Integer.parseInt(tStr[i + 2]),
                    Integer.parseInt(tStr[i + 3])
                );
                tunnels.add(new TunnelDto(from, to));
            }
        }

        // Create grid representation (simple empty grid)
        List<List<String>> gridData = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                row.add(".");
            }
            gridData.add(row);
        }

        // Parse traffic into 2D array (default traffic value is 1)
        List<List<Integer>> trafficGrid = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                // 10% chance of blocked cell (0), otherwise random traffic 1-4
                int trafficValue = rand.nextInt(100) < 10 ? 0 : rand.nextInt(4) + 1;
                row.add(trafficValue);
            }
            trafficGrid.add(row);
        }

        // Ensure stores and customers are not blocked
        for (Position store : stores) {
            if (store.getX() >= 0 && store.getX() < rows && store.getY() >= 0 && store.getY() < columns) {
                if (trafficGrid.get(store.getX()).get(store.getY()) == 0) {
                    trafficGrid.get(store.getX()).set(store.getY(), 1);
                }
            }
        }
        for (Position customer : customers) {
            if (customer.getX() >= 0 && customer.getX() < rows && customer.getY() >= 0 && customer.getY() < columns) {
                if (trafficGrid.get(customer.getX()).get(customer.getY()) == 0) {
                    trafficGrid.get(customer.getX()).set(customer.getY(), 1);
                }
            }
        }

        // Parse traffic string: format is "x1,y1,x2,y2,cost;"
        // This is for edge costs, not used in the grid visualization
        if (traffic != null && !traffic.isEmpty()) {
            String[] trafficParts = traffic.split(";");
            for (String part : trafficParts) {
                if (!part.trim().isEmpty()) {
                    String[] coords = part.split(",");
                    if (coords.length >= 5) {
                        int x1 = Integer.parseInt(coords[0].trim());
                        int y1 = Integer.parseInt(coords[1].trim());
                        int cost = Integer.parseInt(coords[4].trim());
                        // Store traffic cost at source position
                        // 0 = blocked route
                        if (x1 >= 0 && x1 < rows && y1 >= 0 && y1 < columns) {
                            // Only override if not already blocked
                            if (trafficGrid.get(x1).get(y1) != 0) {
                                trafficGrid.get(x1).set(y1, cost);
                            }
                        }
                    }
                }
            }
        }

        return new Grid(rows, columns, gridData, stores, customers, tunnels, trafficGrid);
    }

    @PostMapping("/choose-strategy")
    public ResponseEntity<?> chooseStrategy(@RequestBody StrategyRequest request) {
        if (initialState == null || traffic == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Grid not generated yet. Please call /generate-grid first."));
        }

        try {
            String strategyInput = request.getStrategy().toLowerCase();

            if ("all".equals(strategyInput)) {
                // Run all strategies
                List<StrategyResult> results = new ArrayList<>();
                Strategy[] allStrategies = {Strategy.BF, Strategy.DF, Strategy.ID, Strategy.UC, Strategy.GR1, Strategy.GR2, Strategy.AS1, Strategy.AS2};

                for (Strategy strat : allStrategies) {
                    String stratInput = strat.name().toLowerCase();
                    String displayName = strat.getDisplayName();

                    long start = System.nanoTime();
                    String result = DeliverySearch.solve(initialState, traffic, stratInput, false);
                    long time = (System.nanoTime() - start) / 1_000_000;

                    // Parse result
                    String[] lines = result.split("\n");
                    int totalNodes = 0;
                    int totalCost = 0;
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            String[] parts = line.split(";");
                            if (parts.length >= 4) {
                                totalCost += Integer.parseInt(parts[2]);
                                totalNodes += Integer.parseInt(parts[3]);
                            }
                        }
                    }

                    results.add(new StrategyResult(displayName, time, totalNodes, totalCost));
                }

                return ResponseEntity.ok(new StrategyResults(results));
            } else {
                // Single strategy with detailed execution
                Strategy strat = Strategy.fromString(strategyInput);
                StrategyExecutionResult executionResult = executeStrategyWithDetails(strat, strategyInput);
                return ResponseEntity.ok(executionResult);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid strategy: " + request.getStrategy()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    private StrategyExecutionResult executeStrategyWithDetails(Strategy strategy, String strategyInput) {
        // Force garbage collection for accurate memory measurement
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        long cpuBefore = threadBean.getCurrentThreadCpuTime();
        long startTime = System.nanoTime();

        // Execute strategy and capture steps
        List<String> steps = new ArrayList<>();
        DeliverySearch ds = DeliverySearch.fromStrings(initialState, traffic);

        // Get assignments
        DeliveryPlanner planner = new DeliveryPlanner(ds.getStores(), ds.getCustomers(), ds.getTrucks(), ds, strategy);
        List<int[]> assignments = planner.assign();

        // Track current truck positions
        List<State> currentTruckPositions = new ArrayList<>(ds.getTrucks());

        int deliveryNum = 1;
        int totalDeliveries = 0;
        int totalCost = 0;
        int totalNodes = 0;

        steps.add("Step 0: Initial Position");

        for (int[] assignment : assignments) {
            int truckIdx = assignment[0];
            int customerIdx = assignment[1];

            State startPos = currentTruckPositions.get(truckIdx);
            State goalPos = ds.getCustomers().get(customerIdx);

            // Add assignment step
            steps.add("Step " + steps.size() + ": Truck " + truckIdx + " assigned to Customer " + customerIdx);

            // Get path
            ds.setPath(startPos, goalPos);
            GenericSearch.SearchResult<State, Action> result = GenericSearch.search(ds, strategy, ds.getH1(), ds.getH2());

            if (result.cost == Double.POSITIVE_INFINITY) {
                steps.add("Warning: No path found from Store" + truckIdx + " to Customer" + customerIdx);
                continue;
            }

            // Record path execution steps
            State current = startPos;
            for (Action action : result.actions) {
                current = ds.result(current, action);
                steps.add("Step " + steps.size() + ": Action = " + action + ", Truck " + truckIdx + " moved to (" + current.x + "," + current.y + ") delivering to Customer " + customerIdx);
            }

            totalCost += (int) result.cost;
            totalNodes += result.nodesExpanded;
            totalDeliveries++;

            // Update truck position
            currentTruckPositions.set(truckIdx, goalPos);

            // Truck returns to store (but don't record steps for return)
            State storeLocation = ds.getStores().get(truckIdx);
            currentTruckPositions.set(truckIdx, storeLocation);

            deliveryNum++;
        }

        steps.add("Delivery Complete!");

        long endTime = System.nanoTime();
        long timeMs = (endTime - startTime) / 1_000_000;
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memUsed = (memAfter - memBefore) / 1024; // KB
        long cpuAfter = threadBean.getCurrentThreadCpuTime();
        long cpuUsed = (cpuAfter - cpuBefore) / 1_000_000; // ms

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timeMs", timeMs);
        metrics.put("deliveries", totalDeliveries);
        metrics.put("totalCost", totalCost);
        metrics.put("nodesExpanded", totalNodes);
        metrics.put("memoryKB", memUsed);
        metrics.put("cpuMs", cpuUsed);

        return new StrategyExecutionResult(steps, metrics);
    }
}