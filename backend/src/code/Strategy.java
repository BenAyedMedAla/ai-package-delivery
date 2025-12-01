package com.aipackagedelivery.code;

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
}
