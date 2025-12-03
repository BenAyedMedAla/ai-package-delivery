package code;

import java.util.List;

/**
 * H2: Traffic-Aware Manhattan Heuristic
 * 
 * This heuristic considers the minimum traffic level when estimating the cost
 * to reach the goal. It also considers tunnel shortcuts for more accurate estimates.
 * 
 * Admissibility Proof:
 * 1. Actual path cost = Σ(traffic_i) for each edge i in the path
 * 2. Since traffic_i ≥ minTraffic for all edges
 * 3. Actual cost ≥ minTraffic × number_of_edges
 * 4. Number of edges ≥ Manhattan distance (shortest path on 4-connected grid)
 * 5. Therefore: h(n) = minTraffic × Manhattan ≤ actual cost
 * 
 * Tunnel Consideration:
 * - We calculate the cost via each tunnel: (distance to entrance) + tunnel_cost + (distance from exit)
 * - Tunnel cost = Manhattan distance between entrances (as per project spec)
 * - We take the minimum of direct path and all tunnel paths
 * - Since we never overestimate any component, the minimum never overestimates
 * - This heuristic is admissible and more informed than pure Manhattan
 */
public class TrafficAwareHeuristic implements Heuristic<State> {
    private State goal;
    private List<Tunnel> tunnels;
    private final int minTraffic; // Minimum traffic level in the grid

    /**
     * Constructor
     * @param minTraffic the minimum traffic level in the grid (usually 1)
     */
    public TrafficAwareHeuristic(int minTraffic) {
        this.minTraffic = Math.max(1, minTraffic); // At least 1
    }

    /**
     * Set the goal state for heuristic calculations
     */
    public void setGoal(State goal, List<Tunnel> tunnels) {
        this.goal = goal;
        this.tunnels = tunnels;
    }

    @Override
    public double h(State s) {
        if (goal == null) return 0;
        
        // Manhattan distance weighted by minimum traffic
        // This is admissible because actual cost >= minTraffic * distance
        double manhattanDist = Math.abs(s.x - goal.x) + Math.abs(s.y - goal.y);
        double directCost = manhattanDist * minTraffic;
        
        double minCost = directCost;
        
        // Consider tunnels (tunnel cost = Manhattan between entrances)
        if (tunnels != null) {
            for (Tunnel t : tunnels) {
                // Cost to reach first entrance
                double toEntrance1 = (Math.abs(s.x - t.from.x) + Math.abs(s.y - t.from.y)) * minTraffic;
                // Tunnel traversal cost (Manhattan distance between entrances)
                double tunnelCost = Math.abs(t.from.x - t.to.x) + Math.abs(t.from.y - t.to.y);
                // Cost from tunnel exit to goal
                double fromExit1 = (Math.abs(t.to.x - goal.x) + Math.abs(t.to.y - goal.y)) * minTraffic;
                double viaFrom = toEntrance1 + tunnelCost + fromExit1;
                
                // Same calculation for other direction
                double toEntrance2 = (Math.abs(s.x - t.to.x) + Math.abs(s.y - t.to.y)) * minTraffic;
                double fromExit2 = (Math.abs(t.from.x - goal.x) + Math.abs(t.from.y - goal.y)) * minTraffic;
                double viaTo = toEntrance2 + tunnelCost + fromExit2;
                
                minCost = Math.min(minCost, Math.min(viaFrom, viaTo));
            }
        }
        
        return minCost;
    }

    @Override
    public String toString() {
        return "TrafficAwareHeuristic(minTraffic=" + minTraffic + ")";
    }
}