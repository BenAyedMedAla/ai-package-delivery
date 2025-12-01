package com.aipackagedelivery;

import com.aipackagedelivery.code.DeliverySearch;
import com.aipackagedelivery.code.Strategy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class DeliveryController {

    @GetMapping("/generateGrid")
    public ResponseEntity<Map<String, Object>> generateGrid(
            @RequestParam(defaultValue = "2") int numTrucks,
            @RequestParam(defaultValue = "3") int numCustomers) {
        try {
            String initialState = DeliverySearch.GenGrid(numTrucks, numCustomers);
            String traffic = DeliverySearch.GenTraffic();
            Map<String, Object> response = new HashMap<>();
            response.put("grid", initialState);
            response.put("traffic", traffic);
            // Parse tunnels from initialState (but since GenGrid returns matrix, tunnels are included)
            response.put("tunnels", new ArrayList<>()); // For now
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate grid: " + e.getMessage()));
        }
    }

    public static class PlanRequest {
        private String initialState;
        private String traffic;
        private String strategy;

        public String getInitialState() { return initialState; }
        public void setInitialState(String initialState) { this.initialState = initialState; }
        public String getTraffic() { return traffic; }
        public void setTraffic(String traffic) { this.traffic = traffic; }
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }

    @PostMapping(value = "/planRoutes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> planRoutes(@RequestParam String initialState, @RequestParam String traffic, @RequestParam String strategy) {
        try {
            initialState = initialState.replace("\\n", "\n");
            String strategyStr = strategy;

            if (initialState == null || traffic == null || strategyStr == null) {
                return ResponseEntity.badRequest().body("Missing required parameters: initialState, traffic, strategy");
            }

            DeliverySearch ds = DeliverySearch.fromString(initialState);

            // Run all strategies and build output like terminal
            StringBuilder sb = new StringBuilder();
            Strategy[] strategies = {Strategy.BF, Strategy.DF, Strategy.ID, Strategy.UC, Strategy.GR1, Strategy.GR2, Strategy.AS1, Strategy.AS2};
            String[] names = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};

            for (int i = 0; i < strategies.length; i++) {
                long start = System.nanoTime();
                String result = ds.plan(strategies[i], false);
                long time = (System.nanoTime() - start) / 1_000_000; // ms
                sb.append(result.trim()).append(" (").append(time).append("ms) ").append(names[i]).append(" | ");
            }

            // Remove last " | "
            if (sb.length() > 3) {
                sb.setLength(sb.length() - 3);
            }

            return ResponseEntity.ok(sb.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid strategy: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to plan routes: " + e.getMessage());
        }
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance() {
        try {
            String initialState = DeliverySearch.GenGrid();
            String traffic = DeliverySearch.GenTraffic();
            Strategy[] strategies = {Strategy.BF, Strategy.DF, Strategy.ID, Strategy.UC, Strategy.GR1, Strategy.GR2, Strategy.AS1, Strategy.AS2};
            String[] names = {"BF", "DF", "ID", "UC", "GR1", "GR2", "AS1", "AS2"};

            Map<String, Object> performance = new HashMap<>();
            for (int i = 0; i < strategies.length; i++) {
                long start = System.nanoTime();
                String result = DeliverySearch.solve(initialState, traffic, names[i], false);
                long time = (System.nanoTime() - start) / 1_000_000; // ms

                Map<String, Object> metrics = new HashMap<>();
                if (!result.equals("no path;0;0")) {
                    String[] lines = result.split("\n");
                    int totalCost = 0;
                    int totalNodes = 0;
                    for (String line : lines) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            totalCost += Integer.parseInt(parts[3]);
                            totalNodes += Integer.parseInt(parts[4]);
                        }
                    }
                    metrics.put("cost", totalCost);
                    metrics.put("nodesExpanded", totalNodes);
                } else {
                    metrics.put("cost", 0);
                    metrics.put("nodesExpanded", 0);
                }
                metrics.put("time", time + "ms");
                performance.put(names[i], metrics);
            }

            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get performance: " + e.getMessage()));
        }
    }
}