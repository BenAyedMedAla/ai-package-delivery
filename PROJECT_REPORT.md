# AI Package Delivery System - Project Report

**Course:** Artificial Intelligence (GL4 - Semester 1)  
**Project:** Search Algorithms for Package Delivery Planning  
**Date:** December 5, 2025

---

## Table of Contents

1. [Class Diagram](#1-class-diagram)
2. [Search Algorithms Implementation](#2-search-algorithms-implementation)
3. [Heuristic Functions & Admissibility](#3-heuristic-functions--admissibility)
4. [Performance Comparison](#4-performance-comparison)
5. [References](#5-references)

---

## 1. Class Diagram

### Main Classes and Their Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                         Core ADT Classes                        │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────┐
│   Node<State, Action>│
├──────────────────────┤
│ - state: State       │
│ - parent: Node       │
│ - action: Action     │
│ - depth: int         │
│ - pathCost: double   │
└──────────────────────┘
         △
         │ uses
         │
┌──────────────────────────────────────────┐
│    Problem<State, Action> (Interface)    │
├──────────────────────────────────────────┤
│ + initialState(): State                  │
│ + isGoal(State): boolean                 │
│ + actions(State): List<Action>           │
│ + result(State, Action): State           │
│ + stepCost(State, Action, State): double │
└──────────────────────────────────────────┘
         △
         │ implements
         │
┌──────────────────────────────────────────┐
│          GenericSearch (Abstract)        │
├──────────────────────────────────────────┤
│ + search(Problem, Strategy,              │
│          Heuristic, Heuristic):          │
│          SearchResult                    │
│ - breadthFirstSearch()                   │
│ - depthFirstSearch()                     │
│ - iterativeDeepeningSearch()             │
│ - uniformCostSearch()                    │
│ - greedySearch()                         │
│ - aStarSearch()                          │
└──────────────────────────────────────────┘
         △
         │ extends & implements
         │
┌──────────────────────────────────────────┐
│           DeliverySearch                 │
├──────────────────────────────────────────┤
│ - m, n: int (grid dimensions)            │
│ - edgeTraffic: Map                       │
│ - tunnels: List<Tunnel>                  │
│ - stores, customers, trucks: List<State> │
│ - h1, h2: Heuristic<State>               │
│ - start, goal: State                     │
├──────────────────────────────────────────┤
│ + setPath(State, State): void            │
│ + initialState(): State                  │
│ + isGoal(State): boolean                 │
│ + actions(State): List<Action>           │
│ + result(State, Action): State           │
│ + stepCost(State, Action, State): double │
│ + static solve(String, String,           │
│               String, boolean): String   │
└──────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Supporting Classes                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐      ┌──────────────────┐      ┌──────────────┐
│      State       │      │   Action (Enum)  │      │   Strategy   │
├──────────────────┤      ├──────────────────┤      │    (Enum)    │
│ + x: int         │      │ UP, DOWN         │      ├──────────────┤
│ + y: int         │      │ LEFT, RIGHT      │      │ BF, DF, ID   │
│ + equals()       │      │ TUNNEL           │      │ UC, GR1, GR2 │
│ + hashCode()     │      └──────────────────┘      │ AS1, AS2     │
└──────────────────┘                                └──────────────┘

┌──────────────────────────────────────────┐
│     Heuristic<State> (Interface)         │
├──────────────────────────────────────────┤
│ + h(State): double                       │
└──────────────────────────────────────────┘
         △                △
         │                │
         │                │
┌────────────────┐  ┌─────────────────────────┐
│   Manhattan    │  │  TrafficAware           │
│   Heuristic    │  │  Heuristic              │
├────────────────┤  ├─────────────────────────┤
│ - goal: State  │  │ - goal: State           │
│ - tunnels      │  │ - tunnels: List         │
│                │  │ - minTraffic: int       │
├────────────────┤  ├─────────────────────────┤
│ + h(State)     │  │ + h(State)              │
│   returns      │  │   returns               │
│   Manhattan    │  │   min(direct, tunnels)  │
│   distance     │  │   × minTraffic          │
└────────────────┘  └─────────────────────────┘

┌──────────────────┐
│     Tunnel       │
├──────────────────┤
│ - from: State    │
│ - to: State      │
│ - cost: double   │
├──────────────────┤
│ + isEntrance()   │
│ + getExit()      │
│ + getCost()      │
└──────────────────┘
```

### Key Design Patterns Used:

- **Generic Programming:** `Node<State, Action>`, `Problem<State, Action>`
- **Strategy Pattern:** Different search algorithms as strategies
- **Template Method:** GenericSearch provides framework, DeliverySearch specializes
- **Interface-based Design:** Problem, Heuristic interfaces for extensibility

---

## 2. Search Algorithms Implementation

### 2.1 Overview

All search algorithms are implemented in `GenericSearch.java` following the generic search procedure from lecture. The implementation emphasizes:

- **Redundancy elimination** to minimize repeated states
- **Optimal memory management** using appropriate data structures
- **Termination guarantees** within reasonable time limits

### 2.2 Breadth-First Search (BFS)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> breadthFirstSearch(Problem<S, A> problem) {
    Queue<Node<S, A>> frontier = new LinkedList<>();  // FIFO queue
    Set<S> explored = new HashSet<>();                // Visited states
    Set<S> inFrontier = new HashSet<>();              // Frontier tracking
    // ...
}
```

**Key Features:**

- Uses FIFO (First-In-First-Out) queue
- Explores nodes level by level
- **Redundancy Elimination:** Maintains both `explored` set and `inFrontier` set
- Checks if state is already explored before adding to frontier

**Properties:**

- **Complete:** Yes (finds solution if one exists)
- **Optimal:** Yes (for uniform/unit costs)
- **Time Complexity:** O(b^d) where b = branching factor, d = depth
- **Space Complexity:** O(b^d) - stores entire frontier level

### 2.3 Depth-First Search (DFS)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> depthFirstSearch(Problem<S, A> problem) {
    Stack<Node<S, A>> frontier = new Stack<>();  // LIFO stack
    Set<S> explored = new HashSet<>();
    Set<S> inFrontier = new HashSet<>();
    // ...
}
```

**Key Features:**

- Uses LIFO (Last-In-First-Out) stack
- Explores deepest nodes first
- **Redundancy Elimination:** Tracks explored states to prevent infinite loops
- Efficient memory usage compared to BFS

**Properties:**

- **Complete:** No (can get stuck in infinite branches)
- **Optimal:** No (may find suboptimal solutions)
- **Time Complexity:** O(b^m) where m = maximum depth
- **Space Complexity:** O(bm) - stores single path + frontier

### 2.4 Iterative Deepening Search (ID)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> iterativeDeepeningSearch(Problem<S, A> problem) {
    for (int depthLimit = 0; depthLimit < Integer.MAX_VALUE; depthLimit++) {
        SearchResult<S, A> result = depthLimitedSearch(problem, depthLimit);
        if (result.actions != null && !result.actions.isEmpty()) {
            return result;
        }
    }
}
```

**Key Features:**

- Combines benefits of BFS and DFS
- Repeatedly performs depth-limited DFS with increasing limits
- **Redundancy Handling:** Each iteration is independent depth-limited search

**Properties:**

- **Complete:** Yes (like BFS)
- **Optimal:** Yes (for uniform costs)
- **Time Complexity:** O(b^d) - same asymptotic complexity as BFS
- **Space Complexity:** O(bd) - linear space like DFS

### 2.5 Uniform Cost Search (UCS)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> uniformCostSearch(Problem<S, A> problem) {
    PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
        Comparator.comparingDouble(n -> n.pathCost)  // Order by g(n)
    );
    Map<S, Double> bestCost = new HashMap<>();  // Track best path costs
    // ...
}
```

**Key Features:**

- Uses priority queue ordered by path cost g(n)
- Always expands lowest-cost node first
- **Advanced Redundancy Elimination:** Uses `bestCost` map to track optimal path to each state
- Only adds child if it offers better path cost

**Properties:**

- **Complete:** Yes (with finite branching & positive costs)
- **Optimal:** Yes (always finds minimum-cost solution)
- **Time Complexity:** O(b^(C*/ε)) where C* = optimal cost, ε = minimum cost
- **Space Complexity:** O(b^(C\*/ε))

### 2.6 Greedy Best-First Search (GR1, GR2)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> greedySearch(
        Problem<S, A> problem, Heuristic<S> heuristic) {
    PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
        Comparator.comparingDouble(n -> heuristic.h(n.state))  // Order by h(n)
    );
    Set<S> explored = new HashSet<>();
    // ...
}
```

**Key Features:**

- Uses priority queue ordered by heuristic value h(n) only
- Expands nodes that appear closest to goal
- **Redundancy Elimination:** Simple explored set
- Two variants: GR1 (Manhattan) and GR2 (Traffic-Aware)

**Properties:**

- **Complete:** No (can get stuck in local optima)
- **Optimal:** No (ignores path cost)
- **Time Complexity:** O(b^m) worst case
- **Space Complexity:** O(b^m)
- **Advantage:** Often very fast in practice

### 2.7 A\* Search (AS1, AS2)

**Implementation Details:**

```java
private static <S, A> SearchResult<S, A> aStarSearch(
        Problem<S, A> problem, Heuristic<S> heuristic) {
    PriorityQueue<Node<S, A>> frontier = new PriorityQueue<>(
        Comparator.comparingDouble(n -> n.pathCost + heuristic.h(n.state)) // f(n) = g(n) + h(n)
    );
    Map<S, Double> bestCost = new HashMap<>();
    // ...
}
```

**Key Features:**

- Uses priority queue ordered by f(n) = g(n) + h(n)
- Balances actual cost and estimated remaining cost
- **Advanced Redundancy Elimination:** Tracks best known cost to each state
- Two variants: AS1 (Manhattan) and AS2 (Traffic-Aware)

**Properties:**

- **Complete:** Yes (with admissible heuristic)
- **Optimal:** Yes (with admissible & consistent heuristic)
- **Time Complexity:** O(b^d) but typically much better
- **Space Complexity:** O(b^d)
- **Advantage:** Optimal with best performance in practice

### 2.8 Redundancy Elimination Summary

| Algorithm | Redundancy Prevention Method                       |
| --------- | -------------------------------------------------- |
| BFS/DFS   | `explored` set + `inFrontier` set                  |
| ID        | Fresh search at each depth level                   |
| UCS       | `bestCost` map - only expand if better path found  |
| Greedy    | `explored` set (simple tracking)                   |
| A\*       | `bestCost` map - maintains optimal costs per state |

**Benefits:**

- Reduces memory consumption
- Prevents infinite loops
- Improves CPU efficiency by avoiding redundant expansions
- Guarantees termination for complete algorithms

---

## 3. Heuristic Functions & Admissibility

### 3.1 Heuristic 1: Manhattan Distance (Pure)

**File:** `ManhattanHeuristic.java`

**Formula:**

```
h₁(s) = |s.x - goal.x| + |s.y - goal.y|
```

**Description:**
This heuristic calculates the straight-line grid distance (Manhattan distance or L1 norm) from the current state to the goal state without considering any obstacles or traffic.

**Admissibility Proof:**

**Theorem:** h₁ is admissible for the delivery problem.

**Proof:**

1. **Definition of admissibility:** A heuristic h is admissible if h(n) ≤ h*(n) for all nodes n, where h*(n) is the true optimal cost from n to the goal.

2. **Grid structure:** Our problem uses a 4-connected grid where moves are limited to UP, DOWN, LEFT, RIGHT.

3. **Minimum path length:** On a 4-connected grid, the minimum number of moves required to go from state s to goal is exactly the Manhattan distance:

   ```
   min_moves = |s.x - goal.x| + |s.y - goal.y|
   ```

4. **Minimum cost per move:** Each move has a cost equal to the traffic level of the destination cell. The minimum traffic level in our system is 1.

5. **Lower bound on cost:**

   ```
   h*(s) ≥ min_moves × min_traffic
   h*(s) ≥ manhattan_distance × 1
   h*(s) ≥ |s.x - goal.x| + |s.y - goal.y|
   h*(s) ≥ h₁(s)
   ```

6. **Conclusion:** Since h₁(s) ≤ h\*(s) for all states s, h₁ is admissible. ∎

**Consistency:**
h₁ is also consistent (monotonic):

```
h₁(s) ≤ c(s, a, s') + h₁(s')
```

where c(s, a, s') is the cost of moving from s to s' via action a.

**Usage in A\*:** Since h₁ is admissible and consistent, A\* with h₁ (AS1) is guaranteed to find optimal solutions.

---

### 3.2 Heuristic 2: Traffic-Aware with Tunnel Consideration

**File:** `TrafficAwareHeuristic.java`

**Formula:**

```
h₂(s) = min(
    direct_path_cost,
    min_tunnel_path_cost_for_all_tunnels
)

where:
    direct_path_cost = manhattan(s, goal) × minTraffic

    tunnel_path_cost = min(
        manhattan(s, tunnel.entrance1) × minTraffic +
        tunnel.cost +
        manhattan(tunnel.exit1, goal) × minTraffic,

        manhattan(s, tunnel.entrance2) × minTraffic +
        tunnel.cost +
        manhattan(tunnel.exit2, goal) × minTraffic
    )
```

**Description:**
This heuristic considers both direct paths and potential tunnel shortcuts. It:

1. Calculates the direct path cost using Manhattan distance multiplied by minimum traffic
2. For each tunnel, calculates the cost of going through it (both directions)
3. Returns the minimum of all possible paths

**Admissibility Proof:**

**Theorem:** h₂ is admissible for the delivery problem.

**Proof:**

**Part 1: Direct path component is admissible**

```
direct_path = manhattan(s, goal) × minTraffic
```

- Manhattan distance provides minimum number of grid moves
- minTraffic (= 1) is the minimum cost per move
- Therefore: direct_path ≤ actual cost of direct path ✓

**Part 2: Tunnel path component is admissible**

For any tunnel path from s to goal via tunnel T:

```
tunnel_path = manhattan(s, T.entrance) × minTraffic +
              T.cost +
              manhattan(T.exit, goal) × minTraffic
```

Breaking this down:

1. **Cost to reach tunnel entrance:**

   - Lower bound: manhattan(s, T.entrance) × minTraffic
   - Actual cost: ≥ manhattan(s, T.entrance) × minTraffic (could have higher traffic)
   - Therefore: estimate ≤ actual ✓

2. **Tunnel traversal cost:**

   - By project specification: tunnel cost = Manhattan distance between entrances
   - This is the actual cost of using the tunnel
   - Therefore: estimate = actual ✓

3. **Cost from tunnel exit to goal:**
   - Lower bound: manhattan(T.exit, goal) × minTraffic
   - Actual cost: ≥ manhattan(T.exit, goal) × minTraffic
   - Therefore: estimate ≤ actual ✓

Since each component never overestimates, their sum never overestimates.

**Part 3: Minimum operation preserves admissibility**

```
h₂(s) = min(direct_path, tunnel_path₁, tunnel_path₂, ...)
```

- Each path estimate is admissible (≤ actual cost of that path)
- The minimum of admissible estimates is admissible
- Therefore: h₂(s) ≤ optimal actual cost ✓

**Mathematical formulation:**

```
Let P₁, P₂, ..., Pₙ be all possible paths from s to goal
Let h₁, h₂, ..., hₙ be their heuristic estimates

Given: hᵢ ≤ cost(Pᵢ) for all i (each is admissible)

h₂(s) = min(h₁, h₂, ..., hₙ)
      ≤ min(cost(P₁), cost(P₂), ..., cost(Pₙ))
      = optimal cost

Therefore h₂ is admissible. ∎
```

**Domination Property:**

```
h₂(s) ≤ h₁(s) for all states s
```

This means h₂ is more informed than h₁, leading to fewer node expansions while maintaining optimality.

**Usage in A\*:** Since h₂ is admissible, A\* with h₂ (AS2) is guaranteed to find optimal solutions, typically with better performance than AS1.

---

### 3.3 Comparison of Heuristics

| Property              | h₁ (Manhattan) | h₂ (Traffic-Aware)       |
| --------------------- | -------------- | ------------------------ |
| **Admissible**        | ✓ Yes          | ✓ Yes                    |
| **Consistent**        | ✓ Yes          | ✓ Yes                    |
| **Computation Cost**  | O(1)           | O(k) where k = # tunnels |
| **Informativeness**   | Lower          | Higher (h₂ ≥ h₁)         |
| **Nodes Expanded**    | More           | Fewer                    |
| **Considers Tunnels** | ✗ No           | ✓ Yes                    |
| **Optimal with A\***  | ✓ Yes          | ✓ Yes                    |

**Expected Performance:**

- **AS2 (A\* with h₂)** should expand fewer nodes than AS1 due to more informed heuristic
- Both guarantee optimal solutions
- h₂ has slightly higher computation cost per node but reduces total expansions

---

## 4. Performance Comparison

### 4.1 Theoretical Analysis

| Algorithm | Complete | Optimal | Time Complexity | Space Complexity | Notes                    |
| --------- | -------- | ------- | --------------- | ---------------- | ------------------------ |
| **BFS**   | ✓ Yes    | ✓ Yes\* | O(b^d)          | O(b^d)           | \*Only for uniform costs |
| **DFS**   | ✗ No     | ✗ No    | O(b^m)          | O(bm)            | Can loop, not optimal    |
| **ID**    | ✓ Yes    | ✓ Yes\* | O(b^d)          | O(bd)            | \*Only for uniform costs |
| **UCS**   | ✓ Yes    | ✓ Yes   | O(b^(C\*/ε))    | O(b^(C\*/ε))     | Always finds optimal     |
| **GR1**   | ✗ No     | ✗ No    | O(b^m)          | O(b^m)           | Fast but not guaranteed  |
| **GR2**   | ✗ No     | ✗ No    | O(b^m)          | O(b^m)           | Better than GR1          |
| **AS1**   | ✓ Yes    | ✓ Yes   | O(b^d)          | O(b^d)           | Optimal with h₁          |
| **AS2**   | ✓ Yes    | ✓ Yes   | O(b^d)          | O(b^d)           | Best performance         |

**Legend:**

- b = branching factor (typically 4-5 in our grid)
- d = optimal solution depth
- m = maximum depth (potentially infinite)
- C\* = optimal solution cost
- ε = minimum edge cost

### 4.2 RAM Usage Comparison

**Memory Consumption Characteristics:**

1. **Lowest RAM Usage:**
   - **DFS:** O(bm) - Only stores current path + siblings
   - **ID:** O(bd) - Linear space, combines DFS benefits
2. **Moderate RAM Usage:**
   - **GR1, GR2:** O(b^m) - Priority queue + explored set
   - Depends on how quickly they find goal
3. **Highest RAM Usage:**
   - **BFS:** O(b^d) - Stores entire frontier level
   - **UCS, AS1, AS2:** O(b^d) - Priority queue + cost tracking map
   - BFS typically uses most memory in practice

**RAM Optimization Techniques Used:**

- **Explored sets:** Prevent storing duplicate states
- **bestCost maps:** Replace explored set with cost tracking (UCS, A\*)
- **inFrontier sets:** Prevent duplicate entries in queue (BFS, DFS)

**Expected Results:**

```
DFS < ID < Greedy < A*/UCS < BFS
(in terms of peak memory usage)
```

### 4.3 CPU Utilization Comparison

**Computational Efficiency:**

1. **Lowest CPU Usage (Fewest Nodes Expanded):**

   - **AS2:** Best heuristic guidance → fewest expansions
   - **AS1:** Good heuristic guidance
   - **GR2, GR1:** Fast but may expand more if misguided

2. **Moderate CPU Usage:**

   - **UCS:** Explores by cost but no heuristic guidance
   - **BFS:** Systematic but explores unnecessary nodes

3. **Highest CPU Usage:**
   - **DFS:** May explore deep paths unnecessarily
   - **ID:** Redundant re-expansions at each depth level

**Per-Node Computation Cost:**

```
BFS ≈ DFS ≈ ID < UCS ≈ GR1 < GR2 < AS1 < AS2
```

- Simple algorithms (BFS, DFS) have minimal per-node cost
- A\* algorithms add heuristic computation overhead
- AS2 has highest per-node cost (tunnel evaluation)

**Total CPU Time = (Nodes Expanded) × (Cost per Node)**

Expected ranking for total CPU time:

```
AS2 < AS1 < GR2 < UCS < GR1 < BFS < ID < DFS
```

### 4.4 Nodes Expanded Comparison

**Expected Ordering (Best to Worst):**

1. **AS2 (A\* with Traffic-Aware):**

   - Most informed heuristic
   - Directly heads toward goal considering tunnels
   - **Expected:** 50-200 nodes for typical problems

2. **AS1 (A\* with Manhattan):**

   - Good heuristic guidance
   - Balances cost and distance
   - **Expected:** 100-300 nodes

3. **GR2 (Greedy with Traffic-Aware):**

   - Fast but may need corrections
   - No optimality guarantee
   - **Expected:** 80-400 nodes (high variance)

4. **UCS (Uniform Cost):**

   - Explores uniformly by cost
   - No directional guidance
   - **Expected:** 300-800 nodes

5. **GR1 (Greedy with Manhattan):**

   - Fast but can be misled
   - **Expected:** 100-500 nodes (high variance)

6. **BFS:**

   - Explores all nodes at each level
   - **Expected:** 500-2000 nodes

7. **ID (Iterative Deepening):**

   - Redundant re-expansions
   - **Expected:** 1000-5000 nodes

8. **DFS:**
   - Can explore wrong paths deeply
   - **Expected:** Highly variable, 100-10000+ nodes

**Factors Affecting Node Expansions:**

- Grid size (m × n)
- Number of obstacles (blocked cells)
- Distance from start to goal
- Availability and position of tunnels
- Traffic distribution

### 4.5 Optimality Guarantee

**Algorithms that ALWAYS find optimal solution:**

- ✓ BFS (if costs are uniform)
- ✓ UCS (for any positive costs)
- ✓ ID (if costs are uniform)
- ✓ AS1 (with admissible h₁)
- ✓ AS2 (with admissible h₂)

**Algorithms that may find suboptimal solutions:**

- ✗ DFS (not systematic)
- ✗ GR1, GR2 (ignore path cost)

### 4.6 Key Observations and Trade-offs

**1. Completeness vs. Optimality:**

- DFS, Greedy: Fast but may fail or return suboptimal solutions
- BFS, UCS, A\*: Guaranteed correct but use more resources

**2. Memory vs. Time:**

- DFS/ID: Low memory but potentially high time
- BFS: High memory but predictable time
- A\*: Balanced - good memory and time with proper heuristic

**3. Heuristic Quality Impact:**

- Better heuristic (h₂) → Fewer nodes expanded
- But: Higher per-node computation cost
- Net effect: AS2 typically fastest overall

**4. Problem-Specific Performance:**

- **Dense grids with obstacles:** A\* excels
- **Simple open spaces:** Greedy may be sufficient
- **Unknown solution depth:** BFS/ID safer than DFS

### 4.7 Recommendations

**Use AS2 (A\* with Traffic-Aware) when:**

- Optimal solution is required
- Grid is complex with obstacles and tunnels
- Sufficient memory is available
- Best overall performance is needed

**Use GR2 (Greedy with Traffic-Aware) when:**

- Speed is critical
- Suboptimal solution is acceptable
- Quick approximation is needed

**Use UCS when:**

- Optimal solution is required
- No good heuristic is available
- Costs vary significantly

**Avoid DFS for:**

- Production systems (completeness issues)
- Large search spaces (may not terminate)

---

## 5. References

### 5.1 Course Materials

1. **Lecture 2:** Generic Search Algorithms

   - Generic search procedure implementation
   - Search strategies (BFS, DFS, UCS, Greedy, A\*)
   - Node and Problem abstract data types

2. **Course Textbook:** Artificial Intelligence: A Modern Approach (Russell & Norvig)
   - Chapter 3: Solving Problems by Searching
   - Sections 3.3-3.5: Uninformed Search Strategies
   - Sections 3.5-3.6: Informed Search Strategies
   - Section 3.6: Heuristic Functions and Admissibility

### 5.2 Implementation References

**Java Standard Library:**

- `java.util.Queue`, `LinkedList`: BFS frontier implementation
- `java.util.Stack`: DFS frontier implementation
- `java.util.PriorityQueue`: UCS, Greedy, and A\* frontier
- `java.util.HashSet`: Explored state tracking
- `java.util.HashMap`: Best cost tracking for UCS/A\*

**Design Patterns:**

- Generic programming with Java generics (`<State, Action>`)
- Interface-based design (Strategy pattern)
- Enum pattern for Actions and Strategies

### 5.3 Algorithm Analysis

**Complexity Analysis References:**

- Russell & Norvig, AIMA: Time and space complexity formulas
- Lecture notes: Big-O notation for search algorithms
- Standard algorithm analysis techniques for graph search

**Heuristic Design:**

- Manhattan Distance: Classic admissible heuristic for grid problems
- Minimum spanning tree heuristics: Inspiration for traffic-aware design
- Relaxed problem approach: Ignoring obstacles/traffic for admissible estimates

### 5.4 Original Contributions

**All implementation code is original work:**

- `Node.java`: Original ADT implementation
- `Problem.java`: Interface designed per lecture specification
- `GenericSearch.java`: Complete original implementation of 8 search strategies
- `DeliverySearch.java`: Original problem-specific implementation
- `ManhattanHeuristic.java`: Standard heuristic, original implementation
- `TrafficAwareHeuristic.java`: Original design considering tunnels and traffic

**Novel aspects:**

- Traffic-aware heuristic with tunnel consideration
- Redundancy elimination techniques (inFrontier tracking)
- bestCost map optimization for UCS/A\*
- Integration with Spring Boot for web visualization

### 5.5 Testing and Validation

**Verification Methods:**

- Unit tests for individual components
- Integration tests for complete search scenarios
- Performance benchmarking across algorithms
- Optimality verification using multiple strategies

---

## 6. Conclusion

This project successfully implements a complete search-based AI system for package delivery planning. The implementation:

✅ **Follows academic standards:**

- Proper ADT design (Node, Problem)
- Generic search framework
- All required search strategies

✅ **Demonstrates deep understanding:**

- Admissible heuristic design with proofs
- Redundancy elimination techniques
- Performance trade-off analysis

✅ **Achieves practical goals:**

- Web-based visualization
- Real-time performance comparison
- Scalable to various grid sizes

✅ **Provides educational value:**

- Clear code documentation
- Comprehensive performance analysis
- Theoretical foundations with practical implementation

The A\* algorithm with traffic-aware heuristic (AS2) emerges as the best overall choice, providing optimal solutions with superior performance compared to other strategies.

---

**End of Report**
