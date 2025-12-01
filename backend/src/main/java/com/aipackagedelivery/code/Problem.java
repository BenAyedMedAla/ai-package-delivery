package com.aipackagedelivery.code;

import java.util.List;

public interface Problem<State, Action> {

    State initialState();

    boolean isGoal(State state);

    List<Action> actions(State state);

    State result(State state, Action action);

    double stepCost(State state, Action action, State nextState);
}