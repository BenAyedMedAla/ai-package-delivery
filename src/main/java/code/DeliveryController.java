package code;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

        return new Grid(rows, columns, gridData, stores, customers, tunnels);
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
                // Single strategy
                Strategy strat = Strategy.fromString(strategyInput);
                String displayName = strat.getDisplayName();

                long start = System.nanoTime();
                String result = DeliverySearch.solve(initialState, traffic, strategyInput, false);
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

                return ResponseEntity.ok(new StrategyResult(displayName, time, totalNodes, totalCost));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid strategy: " + request.getStrategy()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
}