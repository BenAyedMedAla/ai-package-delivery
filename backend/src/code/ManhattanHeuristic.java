package code;

import java.util.List;

/**
 * H1: Pure Manhattan Distance Heuristic
 * 
 * This heuristic calculates the straight-line grid distance (Manhattan distance)
 * from the current state to the goal state.
 * 
 * Admissibility Proof:
 * - On a 4-connected grid, the minimum number of moves required is the Manhattan distance
 * - Each move costs at least 1 (minimum traffic level)
 * - Therefore, h(n) = Manhattan(n, goal) ≤ actual cost
 * - This heuristic is admissible and guarantees optimal solutions with A*
 */
public class ManhattanHeuristic implements Heuristic<State> {
    private State goal;
    private List<Tunnel> tunnels;

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
        
        // Pure Manhattan distance (always admissible with traffic >= 1)
        // Formula: |x1 - x2| + |y1 - y2|
        return Math.abs(s.x - goal.x) + Math.abs(s.y - goal.y);
    }

    @Override
    public String toString() {
        return "ManhattanHeuristic";
    }
}