package code;

public enum Strategy {
    BF,   // Breadth-First
    DF,   // Depth-First
    ID,   // Iterative Deepening
    UC,   // Uniform Cost
    GR1,  // Greedy with heuristic 1
    GR2,  // Greedy with heuristic 2
    AS1,  // A* with heuristic 1
    AS2;  // A* with heuristic 2

    public static Strategy fromString(String s) {
        return Strategy.valueOf(s.toUpperCase());
    }

    public String getDisplayName() {
        switch (this) {
            case BF: return "BFS";
            case DF: return "DFS";
            case ID: return "Iterative Deepening";
            case UC: return "Uniform Cost";
            case GR1: return "Greedy (H1)";
            case GR2: return "Greedy (H2)";
            case AS1: return "A* (H1)";
            case AS2: return "A* (H2)";
            default: return this.name();
        }
    }
}
