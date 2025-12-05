package code;

/**
 * Represents a bidirectional tunnel connecting two points on the grid.
 * Tunnels allow trucks to travel between distant points while avoiding traffic.
 * The cost of using a tunnel is the Manhattan distance between its entrances.
 */
public class Tunnel {
    public final State from;
    public final State to;

    public Tunnel(State from, State to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Calculate the cost of traversing this tunnel.
     * Cost = Manhattan distance between entrances
     */
    public double getCost() {
        return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }

    /**
     * Get the other end of the tunnel from a given state
     * @param state current state
     * @return the opposite end, or null if state is not a tunnel entrance
     */
    public State getOtherEnd(State state) {
        if (state.equals(from)) return to;
        if (state.equals(to)) return from;
        return null;
    }

    /**
     * Check if a state is an entrance to this tunnel
     */
    public boolean isEntrance(State state) {
        return state.equals(from) || state.equals(to);
    }

    @Override
    public String toString() {
        return "Tunnel[" + from + " <-> " + to + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tunnel)) return false;
        Tunnel other = (Tunnel) obj;
        return (from.equals(other.from) && to.equals(other.to)) ||
               (from.equals(other.to) && to.equals(other.from));
    }

    @Override
    public int hashCode() {
        // Ensure bidirectional equality
        return from.hashCode() + to.hashCode();
    }
}