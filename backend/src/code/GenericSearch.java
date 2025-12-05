package code;

import java.util.*;

/**
 * Generic search framework implementing various search strategies.
 * Based on the algorithm from Lecture 2.
 * 
 * OPTIMIZATIONS:
 * - BFS/DFS: Check explored before adding to frontier (reduces redundancy)
 * - UCS/A*: Use bestCost map to avoid re-expanding with worse paths
 * - Greedy: Simple explored set (not optimal, but efficient)
 * 
 * NOTE: This class contains only static methods and is extended for structure.
 */
public abstract class GenericSearch {
    
    /**
     * Default constructor for subclasses
     */
    public GenericSearch() {
        // Empty constructor - subclasses can extend
    }

    /**
     * Result container for search operations.
     */
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

    /**
     * Main search algorithm - generic implementation.
     * 
     * @param problem The problem to solve
     * @param strategy The search strategy to use
     * @param h1 First heuristic (for GR1, AS1)
     * @param h2 Second heuristic (for GR2, AS2)
     * @return SearchResult containing path, cost, and nodes expanded
     */
    public static <S, A> SearchResult<S, A> search(
            Problem<S, A> problem,
            Strategy strategy,
            Heuristic<S> h1,
            Heuristic<S> h2) {

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
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    /**
     * Breadth-First Search (BFS)
     * Uses FIFO queue, explores level by level.
     * Complete and optimal for unit costs.
     * Time: O(b^d), Space: O(b^d) where b=branching factor, d=depth
     */
    private static <S, A> SearchResult<S, A> breadthFirstSearch(Problem<S, A> problem) {
        Queue<Node<S, A>> frontier = new LinkedList<>();
        Set<S> explored = new HashSet<>();
        Set<S> inFrontier = new HashSet<>(); // Track states in frontier
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.add(root);
        inFrontier.add(root.state);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.poll();
            inFrontier.remove(node.state);

            // Goal test AFTER removing from frontier
            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            // Skip if already explored
            if (explored.contains(node.state)) {
                continue;
            }

            explored.add(node.state);
            nodesExpanded++;

            // Expand node
            for (A action : problem.actions(node.state)) {
                S childState = problem.result(node.state, action);
                
                // Only add if not explored and not already in frontier
                if (!explored.contains(childState) && !inFrontier.contains(childState)) {
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<S, A> child = new Node<>(
                        childState,
                        node,
                        action,
                        node.depth + 1,
                        node.pathCost + stepCost
                    );
                    frontier.add(child);
                    inFrontier.add(childState);
                }
            }
        }

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * Depth-First Search (DFS)
     * Uses LIFO stack, explores deepest nodes first.
     * Not optimal, may get stuck in infinite paths.
     * Time: O(b^m), Space: O(bm) where b=branching factor, m=max depth
     */
    private static <S, A> SearchResult<S, A> depthFirstSearch(Problem<S, A> problem) {
        Stack<Node<S, A>> frontier = new Stack<>();
        Set<S> explored = new HashSet<>();
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.push(root);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.pop();

            // Goal test
            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            // Skip if already explored
            if (explored.contains(node.state)) {
                continue;
            }

            explored.add(node.state);
            nodesExpanded++;

            // Add children in reverse order for consistent left-to-right expansion
            List<A> actions = problem.actions(node.state);
            for (int i = actions.size() - 1; i >= 0; i--) {
                A action = actions.get(i);
                S childState = problem.result(node.state, action);
                
                if (!explored.contains(childState)) {
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<S, A> child = new Node<>(
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

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * Iterative Deepening Search (IDS)
     * Combines benefits of BFS and DFS.
     * Complete and optimal for unit costs.
     * Time: O(b^d), Space: O(bd) where b=branching factor, d=depth
     */
    private static <S, A> SearchResult<S, A> iterativeDeepeningSearch(Problem<S, A> problem) {
        int totalNodesExpanded = 0;

        for (int depthLimit = 0; depthLimit < Integer.MAX_VALUE; depthLimit++) {
            SearchResult<S, A> result = depthLimitedSearch(problem, depthLimit);
            totalNodesExpanded += result.nodesExpanded;

            if (result.cost != Double.POSITIVE_INFINITY) {
                return new SearchResult<>(result.actions, result.cost, totalNodesExpanded);
            }

            // Safety: Stop if depth gets unreasonably large
            if (depthLimit > 1000) {
                break;
            }
        }

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, totalNodesExpanded);
    }

    /**
     * Depth-Limited Search (helper for IDS)
     */
    private static <S, A> SearchResult<S, A> depthLimitedSearch(
            Problem<S, A> problem, 
            int depthLimit) {
        
        Stack<Node<S, A>> frontier = new Stack<>();
        Set<String> explored = new HashSet<>(); // Use path-based cycle detection
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.push(root);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.pop();

            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            if (node.depth >= depthLimit) {
                continue;
            }

            // Create unique key for this state at this depth
            String key = node.state.toString() + "@" + node.depth;
            if (explored.contains(key)) {
                continue;
            }

            explored.add(key);
            nodesExpanded++;

            List<A> actions = problem.actions(node.state);
            for (int i = actions.size() - 1; i >= 0; i--) {
                A action = actions.get(i);
                S childState = problem.result(node.state, action);
                
                double stepCost = problem.stepCost(node.state, action, childState);
                Node<S, A> child = new Node<>(
                    childState,
                    node,
                    action,
                    node.depth + 1,
                    node.pathCost + stepCost
                );
                frontier.push(child);
            }
        }

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * Uniform Cost Search (UCS)
     * Expands node with lowest path cost g(n).
     * Guarantees optimal solution.
     * Time complexity: Exponential in path cost
     * Space complexity: Exponential in path cost
     */
    private static <S, A> SearchResult<S, A> uniformCostSearch(Problem<S, A> problem) {
        PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.pathCost)
        );
        Map<S, Double> bestCost = new HashMap<>();
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.add(root);
        bestCost.put(root.state, 0.0);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.poll();

            // Goal test
            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            // Skip if we've found a better path to this state
            if (bestCost.containsKey(node.state) && node.pathCost > bestCost.get(node.state)) {
                continue;
            }

            nodesExpanded++;

            for (A action : problem.actions(node.state)) {
                S childState = problem.result(node.state, action);
                double stepCost = problem.stepCost(node.state, action, childState);
                double newCost = node.pathCost + stepCost;

                // Only add if this is a better path
                if (!bestCost.containsKey(childState) || newCost < bestCost.get(childState)) {
                    bestCost.put(childState, newCost);
                    Node<S, A> child = new Node<>(
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

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * Greedy Best-First Search
     * Expands node with lowest heuristic value h(n).
     * Not optimal, but can be very fast.
     * Time and Space: O(b^m) where b=branching factor, m=max depth
     */
    private static <S, A> SearchResult<S, A> greedySearch(
            Problem<S, A> problem,
            Heuristic<S> heuristic) {
        
        PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(n -> heuristic.h(n.state))
        );
        Set<S> explored = new HashSet<>();
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.add(root);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.poll();

            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            if (explored.contains(node.state)) {
                continue;
            }

            explored.add(node.state);
            nodesExpanded++;

            for (A action : problem.actions(node.state)) {
                S childState = problem.result(node.state, action);
                
                if (!explored.contains(childState)) {
                    double stepCost = problem.stepCost(node.state, action, childState);
                    Node<S, A> child = new Node<>(
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

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * A* Search
     * Expands node with lowest f(n) = g(n) + h(n).
     * Optimal if heuristic is admissible.
     * Time and Space: Exponential in worst case, but efficient with good heuristic
     */
    private static <S, A> SearchResult<S, A> aStarSearch(
            Problem<S, A> problem,
            Heuristic<S> heuristic) {
        
        PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.pathCost + heuristic.h(n.state))
        );
        Map<S, Double> bestCost = new HashMap<>();
        int nodesExpanded = 0;

        Node<S, A> root = new Node<>(problem.initialState(), null, null, 0, 0);
        frontier.add(root);
        bestCost.put(root.state, 0.0);

        while (!frontier.isEmpty()) {
            Node<S, A> node = frontier.poll();

            if (problem.isGoal(node.state)) {
                return new SearchResult<>(extractPath(node), node.pathCost, nodesExpanded);
            }

            // Skip if we've found a better path
            if (bestCost.containsKey(node.state) && node.pathCost > bestCost.get(node.state)) {
                continue;
            }

            nodesExpanded++;

            for (A action : problem.actions(node.state)) {
                S childState = problem.result(node.state, action);
                double stepCost = problem.stepCost(node.state, action, childState);
                double newCost = node.pathCost + stepCost;

                if (!bestCost.containsKey(childState) || newCost < bestCost.get(childState)) {
                    bestCost.put(childState, newCost);
                    Node<S, A> child = new Node<>(
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

        return new SearchResult<>(new ArrayList<>(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    /**
     * Extract path from goal node by tracing back to root.
     */
    private static <S, A> List<A> extractPath(Node<S, A> goalNode) {
        List<A> path = new ArrayList<>();
        Node<S, A> current = goalNode;

        while (current.parent != null) {
            path.add(current.action);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }
}