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
            // TODO: add DF, ID, UC, GR1, GR2, AS1, AS2
            default:
                throw new UnsupportedOperationException("Strategy not implemented yet: " + strategy);
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
