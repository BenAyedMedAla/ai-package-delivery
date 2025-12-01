package com.aipackagedelivery;

import com.aipackagedelivery.code.DeliverySearch;
import com.aipackagedelivery.code.Strategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class DeliveryController {

    @GetMapping("/generateGrid")
    public ResponseEntity<Map<String, Object>> generateGrid() {
        try {
            String initialState = DeliverySearch.GenGrid();
            String traffic = DeliverySearch.GenTraffic();
            Map<String, Object> response = new HashMap<>();
            response.put("grid", initialState);
            response.put("traffic", traffic);
            // Parse tunnels from initialState
            String[] parts = initialState.split(";");
            if (parts.length > 5) {
                String[] tunnelStrs = parts[5].split(",");
                List<String> tunnels = new ArrayList<>();
                for (int i = 0; i < tunnelStrs.length; i += 4) {
                    if (i + 3 < tunnelStrs.length) {
                        tunnels.add(tunnelStrs[i] + "," + tunnelStrs[i+1] + "," + tunnelStrs[i+2] + "," + tunnelStrs[i+3]);
                    }
                }
                response.put("tunnels", tunnels);
            } else {
                response.put("tunnels", new ArrayList<>());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate grid: " + e.getMessage()));
        }
    }

    @PostMapping("/planRoutes")
    public ResponseEntity<Map<String, Object>> planRoutes(@RequestBody Map<String, String> request) {
        try {
            String initialState = request.get("initialState");
            String traffic = request.get("traffic");
            String strategyStr = request.get("strategy");

            if (initialState == null || traffic == null || strategyStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters: initialState, traffic, strategy"));
            }

            Strategy strategy = Strategy.fromString(strategyStr);
            String result = DeliverySearch.solve(initialState, traffic, strategyStr, false);

            // Parse result into JSON
            List<Map<String, Object>> routes = new ArrayList<>();
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    Map<String, Object> route = new HashMap<>();
                    route.put("truck", parts[0]);
                    route.put("customer", parts[1]);
                    route.put("path", parts[2]);
                    route.put("cost", Integer.parseInt(parts[3]));
                    route.put("nodesExpanded", Integer.parseInt(parts[4]));
                    routes.add(route);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("routes", routes);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid strategy: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to plan routes: " + e.getMessage()));
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