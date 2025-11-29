# AI Package Delivery – Search-Based Planner

This project implements an AI planner for a package delivery company operating in a grid-based city.
Each road segment has a traffic level that affects travel time, some roads are blocked, and tunnels
can connect distant parts of the city. Our goal is to assign trucks to customers and compute the
cheapest delivery routes using different search strategies.

The system is written in Java and provides:
- A generic search framework (Problem, Node, GenericSearch)
- Multiple search strategies:
  - Uninformed: Breadth-First (BF), Depth-First (DF), Iterative Deepening (ID)
  - Informed: Uniform Cost (UC), Greedy (GR1, GR2), A* (AS1, AS2)
- A delivery problem model:
  - Grid with traffic levels and blocked roads
  - Stores, customers, and tunnels
  - Cost based on traffic and distance
- A planner that:
  - Chooses which truck delivers which package
  - Computes the path for each trip
  - Compares algorithms by cost, expanded nodes, and performance

The repository is structured around two main packages:

- `code` – core implementation (search framework, delivery model, planner, main program)
- `tests` – unit tests and experiments for comparing search strategies

This project is developed as a team assignment for an AI course, focusing on state-space search,
heuristic design, and experimental evaluation.
