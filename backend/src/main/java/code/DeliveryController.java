package code;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
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

        // Create grid representation as list of all coordinates
        List<Position> gridData = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                gridData.add(new Position(i, j));
            }
        }

        return new Grid(rows, columns, gridData, stores, customers, tunnels, traffic);
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

                    // Measure memory and CPU
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

                    String result = DeliverySearch.solve(initialState, traffic, stratInput, false);

                    long endTime = System.nanoTime();
                    long timeMs = (endTime - startTime) / 1_000_000;
                    long memAfter = runtime.totalMemory() - runtime.freeMemory();
                    long memUsed = (memAfter - memBefore) / 1024; // KB
                    long cpuAfter = threadBean.getCurrentThreadCpuTime();
                    long cpuUsed = (cpuAfter - cpuBefore) / 1_000_000; // ms

                    // Parse result
                    String[] lines = result.split("\n");
                    int totalNodes = 0;
                    int totalCost = 0;
                    int deliveries = 0;
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            String[] parts = line.split(";");
                            if (parts.length >= 4) {
                                totalCost += Integer.parseInt(parts[2]);
                                totalNodes += Integer.parseInt(parts[3]);
                                deliveries++;
                            }
                        }
                    }

                    results.add(new StrategyResult(displayName, timeMs, totalNodes, totalCost, deliveries, memUsed, cpuUsed));
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
        List<String> warnings = planner.getWarnings();

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

            // Update truck position to customer location
            currentTruckPositions.set(truckIdx, goalPos);

            // Add delivery completion step
            steps.add("Step " + steps.size() + ": Delivery completed for Customer " + customerIdx + " by Truck " + truckIdx);

            // Generate return path to store
            State storeLocation = ds.getStores().get(truckIdx);
            steps.add("Step " + steps.size() + ": Truck " + truckIdx + " returning to Store " + truckIdx);
            
            ds.setPath(goalPos, storeLocation);
            GenericSearch.SearchResult<State, Action> returnResult = GenericSearch.search(ds, strategy, ds.getH1(), ds.getH2());
            
            if (returnResult.cost != Double.POSITIVE_INFINITY) {
                // Record return path execution steps
                State returnCurrent = goalPos;
                for (Action action : returnResult.actions) {
                    returnCurrent = ds.result(returnCurrent, action);
                    steps.add("Step " + steps.size() + ": Action = " + action + ", Truck " + truckIdx + " moved to (" + returnCurrent.x + "," + returnCurrent.y + ") returning to Store " + truckIdx);
                }
            }
            
            // Update truck position to store
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

        return new StrategyExecutionResult(steps, metrics, warnings);
    }
}