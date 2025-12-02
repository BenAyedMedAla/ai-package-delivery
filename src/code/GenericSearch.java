package code;

import java.util.*;

/**
 * Generic search algorithms for any problem implementing Problem<State, Action>.
 */
public class GenericSearch {

    public static class SearchResult<State, Action> {
        public final List<Action> actions;
        public final double cost;
        public final int nodesExpanded;

        public SearchResult(List<Action> actions, double cost, int nodesExpanded) {
            this.actions = actions;
            this.cost = cost;
            this.nodesExpanded = nodesExpanded;
        }
    }

    public static <State, Action> SearchResult<State, Action> search(
            Problem<State, Action> problem,
            Strategy strategy,
            Heuristic<State> h1,
            Heuristic<State> h2) {

        switch (strategy) {
            case BF:
                return breadthFirstSearch(problem);
            case DF:
                return depthFirstSearch(problem);
            case ID:
                return iterativeDeepeningSearch(problem);
            case UC:
                return uniformCostSearch(problem);
            case GR1:
                return greedySearch(problem, h1);
            case GR2:
                return greedySearch(problem, h2);
            case AS1:
                return aStarSearch(problem, h1);
            case AS2:
                return aStarSearch(problem, h2);
            default:
                throw new UnsupportedOperationException("Unknown strategy: " + strategy);
        }
    }

    // ================== BASIC BFS (Breadth-First Search) ==================

    private static <State, Action> SearchResult<State, Action> breadthFirstSearch(
            Problem<State, Action> problem) {

        Node<State, Action> root =
                new Node<>(problem.initialState(), null, null, 0, 0.0);

        Queue<Node<State, Action>> frontier = new ArrayDeque<>();
        frontier.add(root);

        Set<State> explored = new HashSet<>();
        explored.add(root.state);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node<State, Action> node = frontier.remove();

            if (problem.isGoal(node.state)) {
                List<Action> plan = extractPlan(node);
                return new SearchResult<>(plan, node.pathCost, nodesExpanded);
            }

            nodesExpanded++;

            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);

                if (!explored.contains(childState)) {
                    explored.add(childState);
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            node.pathCost + stepCost
                    );
                    frontier.add(child);
                }
            }
        }

        // No solution found
        return new SearchResult<>(Collections.emptyList(),
                                  Double.POSITIVE_INFINITY,
                                  nodesExpanded);
    }

    // ================== Depth-First Search ==================

    private static <State, Action> SearchResult<State, Action> depthFirstSearch(
            Problem<State, Action> problem) {

        Node<State, Action> root =
                new Node<>(problem.initialState(), null, null, 0, 0.0);

        Deque<Node<State, Action>> frontier = new ArrayDeque<>();
        frontier.push(root);

        Set<State> explored = new HashSet<>();
        explored.add(root.state);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node<State, Action> node = frontier.pop();

            if (problem.isGoal(node.state)) {
                List<Action> plan = extractPlan(node);
                return new SearchResult<>(plan, node.pathCost, nodesExpanded);
            }

            nodesExpanded++;

            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);

                if (!explored.contains(childState)) {
                    explored.add(childState);
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            node.pathCost + stepCost
                    );
                    frontier.push(child);
                }
            }
        }

        // No solution found
        return new SearchResult<>(Collections.emptyList(),
                                  Double.POSITIVE_INFINITY,
                                  nodesExpanded);
    }

    // ================== Iterative Deepening Search ==================

// ================== Iterative Deepening Search ==================

private static <State, Action> SearchResult<State, Action> iterativeDeepeningSearch(
        Problem<State, Action> problem) {

    int depth = 0;
    int totalNodesExpanded = 0;
    
    while (true) {
        SearchResult<State, Action> result = depthLimitedSearch(problem, depth);
        totalNodesExpanded += result.nodesExpanded;
        
        if (result.cost != Double.POSITIVE_INFINITY) {
            // Found solution - return with total nodes expanded
            return new SearchResult<>(result.actions, result.cost, totalNodesExpanded);
        }
        depth++;
        
        // Safety limit to prevent infinite loop
        if (depth > 1000) {
            return new SearchResult<>(Collections.emptyList(), 
                                    Double.POSITIVE_INFINITY, 
                                    totalNodesExpanded);
        }
    }
}

private static <State, Action> SearchResult<State, Action> depthLimitedSearch(
        Problem<State, Action> problem, int limit) {

    Node<State, Action> root = new Node<>(problem.initialState(), null, null, 0, 0.0);
    Deque<Node<State, Action>> frontier = new ArrayDeque<>();
    frontier.push(root);

    // NO explored set for ID - only check for cycles in current path
    int nodesExpanded = 0;

    while (!frontier.isEmpty()) {
        Node<State, Action> node = frontier.pop();

        // Goal test BEFORE expansion
        if (problem.isGoal(node.state)) {
            List<Action> plan = extractPlan(node);
            return new SearchResult<>(plan, node.pathCost, nodesExpanded);
        }

        nodesExpanded++;

        // Only expand if we haven't reached depth limit
        if (node.depth < limit) {
            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);
                
                // Only check if childState is in current path (prevent cycles)
                if (!isInPath(node, childState)) {
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            node.pathCost + stepCost
                    );
                    frontier.push(child);
                }
            }
        }
    }

    // No solution found at this depth
    return new SearchResult<>(Collections.emptyList(),
                              Double.POSITIVE_INFINITY,
                              nodesExpanded);
}

// Helper: Check if state is already in the path from root to node (cycle detection)
private static <State, Action> boolean isInPath(Node<State, Action> node, State state) {
    Node<State, Action> current = node;
    while (current != null) {
        if (current.state.equals(state)) {
            return true;
        }
        current = current.parent;
    }
    return false;
}

    // ================== Uniform Cost Search ==================

    private static <State, Action> SearchResult<State, Action> uniformCostSearch(
            Problem<State, Action> problem) {

        Node<State, Action> root =
                new Node<>(problem.initialState(), null, null, 0, 0.0);

        PriorityQueue<Node<State, Action>> frontier = new PriorityQueue<>(
                Comparator.comparingDouble(n -> n.pathCost));
        frontier.add(root);

        Map<State, Double> explored = new HashMap<>();
        explored.put(root.state, root.pathCost);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node<State, Action> node = frontier.poll();

            if (problem.isGoal(node.state)) {
                List<Action> plan = extractPlan(node);
                return new SearchResult<>(plan, node.pathCost, nodesExpanded);
            }

            nodesExpanded++;

            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);
                double stepCost = problem.stepCost(node.state, action, childState);
                double newCost = node.pathCost + stepCost;

                if (!explored.containsKey(childState) || newCost < explored.get(childState)) {
                    explored.put(childState, newCost);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            newCost
                    );
                    frontier.add(child);
                }
            }
        }

        // No solution found
        return new SearchResult<>(Collections.emptyList(),
                                  Double.POSITIVE_INFINITY,
                                  nodesExpanded);
    }

    // ================== Greedy Search ==================

    private static <State, Action> SearchResult<State, Action> greedySearch(
            Problem<State, Action> problem, Heuristic<State> heuristic) {

        Node<State, Action> root =
                new Node<>(problem.initialState(), null, null, 0, 0.0);

        PriorityQueue<Node<State, Action>> frontier = new PriorityQueue<>(
                Comparator.comparingDouble(n -> heuristic.h(n.state)));
        frontier.add(root);

        Set<State> explored = new HashSet<>();
        explored.add(root.state);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node<State, Action> node = frontier.poll();

            if (problem.isGoal(node.state)) {
                List<Action> plan = extractPlan(node);
                return new SearchResult<>(plan, node.pathCost, nodesExpanded);
            }

            nodesExpanded++;

            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);

                if (!explored.contains(childState)) {
                    explored.add(childState);
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            node.pathCost + stepCost
                    );
                    frontier.add(child);
                }
            }
        }

        // No solution found
        return new SearchResult<>(Collections.emptyList(),
                                  Double.POSITIVE_INFINITY,
                                  nodesExpanded);
    }

    // ================== A* Search ==================

    private static <State, Action> SearchResult<State, Action> aStarSearch(
            Problem<State, Action> problem, Heuristic<State> heuristic) {

        Node<State, Action> root =
                new Node<>(problem.initialState(), null, null, 0, 0.0);

        PriorityQueue<Node<State, Action>> frontier = new PriorityQueue<>(
                Comparator.comparingDouble(n -> n.pathCost + heuristic.h(n.state)));
        frontier.add(root);

        Map<State, Double> explored = new HashMap<>();
        explored.put(root.state, root.pathCost);

        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            Node<State, Action> node = frontier.poll();

            if (problem.isGoal(node.state)) {
                List<Action> plan = extractPlan(node);
                return new SearchResult<>(plan, node.pathCost, nodesExpanded);
            }

            nodesExpanded++;

            for (Action action : problem.actions(node.state)) {
                State childState = problem.result(node.state, action);
                double stepCost = problem.stepCost(node.state, action, childState);
                double newCost = node.pathCost + stepCost;

                if (!explored.containsKey(childState) || newCost < explored.get(childState)) {
                    explored.put(childState, newCost);
                    Node<State, Action> child = new Node<>(
                            childState,
                            node,
                            action,
                            node.depth + 1,
                            newCost
                    );
                    frontier.add(child);
                }
            }
        }

        // No solution found
        return new SearchResult<>(Collections.emptyList(),
                                  Double.POSITIVE_INFINITY,
                                  nodesExpanded);
    }

    private static <State, Action> List<Action> extractPlan(Node<State, Action> goalNode) {
        List<Action> actions = new ArrayList<>();
        Node<State, Action> current = goalNode;

        while (current != null && current.action != null) {
            actions.add(current.action);
            current = current.parent;
        }

        Collections.reverse(actions);
        return actions;
    }
}
