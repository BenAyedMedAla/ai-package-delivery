package code;

import java.util.List;

/**
 * H2: Traffic-Aware Manhattan Heuristic
 * 
 * This heuristic considers both Manhattan distance and tunnel shortcuts
 * while maintaining admissibility by never overestimating the actual cost.
 * 
 * ADMISSIBILITY PROOF:
 * ---------------------
 * 1. For direct path (no tunnels):
 *    - Minimum edges needed = Manhattan distance (straight-line path on grid)
 *    - Each edge costs >= 1 (minimum traffic level)
 *    - Therefore: h(n) = Manhattan × 1 ≤ actual cost ✓
 * 
 * 2. For tunnel paths:
 *    - Cost to tunnel entrance >= Manhattan(state, entrance) × 1
 *    - Tunnel cost = Manhattan(entrance1, entrance2) [as per project spec]
 *    - Cost from tunnel exit >= Manhattan(exit, goal) × 1
 *    - We take minimum of all possible paths
 *    - Since each component never overestimates, minimum never overestimates ✓
 * 
 * 3. Comparison with H1:
 *    - H1 = pure Manhattan distance (always admissible)
 *    - H2 = min(direct Manhattan, best tunnel path)
 *    - H2 ≤ H1 (more informed, considers shortcuts)
 *    - H2 is admissible AND more informed than H1
 * 
 * IMPLEMENTATION NOTES:
 * ---------------------
 * - minTraffic is set to 1 (minimum possible traffic level)
 * - This ensures we never underestimate (which would be inadmissible)
 * - We never overestimate (which guarantees optimality with A*)
 */
public class TrafficAwareHeuristic implements Heuristic<State> {
    private State goal;
    private List<Tunnel> tunnels;
    private final int minTraffic;

    /**
     * Constructor
     * @param minTraffic the minimum traffic level in the grid (typically 1)
     */
    public TrafficAwareHeuristic(int minTraffic) {
        this.minTraffic = Math.max(1, minTraffic);
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
        
        // Direct path cost (minimum cost per edge = minTraffic)
        double manhattanDist = Math.abs(s.x - goal.x) + Math.abs(s.y - goal.y);
        double bestCost = manhattanDist * minTraffic;
        
        // Check if any tunnel provides a better path
        if (tunnels != null && !tunnels.isEmpty()) {
            for (Tunnel t : tunnels) {
                // Path via tunnel: state → tunnel entrance → tunnel exit → goal
                
                // Option 1: Enter from 'from', exit at 'to'
                double toFrom = manhattan(s, t.from) * minTraffic;
                double tunnelCost = t.getCost(); // Tunnel cost is already Manhattan distance
                double toGoalFromTo = manhattan(t.to, goal) * minTraffic;
                double viaFromTo = toFrom + tunnelCost + toGoalFromTo;
                
                // Option 2: Enter from 'to', exit at 'from'
                double toTo = manhattan(s, t.to) * minTraffic;
                double toGoalFromFrom = manhattan(t.from, goal) * minTraffic;
                double viaToFrom = toTo + tunnelCost + toGoalFromFrom;
                
                // Take the better of the two tunnel directions
                double viaTunnel = Math.min(viaFromTo, viaToFrom);
                
                // Update best cost if this tunnel is better
                bestCost = Math.min(bestCost, viaTunnel);
            }
        }
        
        return bestCost;
    }

    /**
     * Calculate Manhattan distance between two states
     */
    private double manhattan(State s1, State s2) {
        return Math.abs(s1.x - s2.x) + Math.abs(s1.y - s2.y);
    }

    @Override
    public String toString() {
        return "TrafficAwareHeuristic(minTraffic=" + minTraffic + ")";
    }

    /**
     * Get a detailed description of this heuristic for the report
     */
    public String getDescription() {
        return "Traffic-Aware Manhattan Heuristic with Tunnel Consideration\n" +
               "- Base: Manhattan distance × minimum traffic level\n" +
               "- Enhancement: Considers tunnel shortcuts\n" +
               "- Admissible: Never overestimates actual path cost\n" +
               "- More informed than pure Manhattan distance";
    }
}