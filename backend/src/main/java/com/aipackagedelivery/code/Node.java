package com.aipackagedelivery.code;

public class Node<State, Action> {

    public final State state;
    public final Node<State, Action> parent;
    public final Action action;
    public final int depth;
    public final double pathCost;  // g(n)

    public Node(State state,
                Node<State, Action> parent,
                Action action,
                int depth,
                double pathCost) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.depth = depth;
        this.pathCost = pathCost;
    }
}