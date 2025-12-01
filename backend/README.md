# 📦 AI Package Delivery – Search-Based Path Planning in Java

## Project Overview
This project implements an AI planning system for a package delivery company operating inside a grid-based city. The objective is to compute optimal delivery routes from stores to customers using classical state-space search algorithms.

## 🚀 Features

### Grid-Based World
- **City represented as an m×n grid**
- **Stores** (starting positions of trucks)
- **Customers** (delivery targets)
- **Tunnels** connecting two distant cells
- **Blocked roads** (traffic = 0)
- **Traffic levels** (1–4) affecting movement cost

### Search Algorithms Implemented
Both uninformed and informed algorithms are supported:

| Uninformed | Informed |
|------------|----------|
| BF – Breadth-First | GR1 – Greedy (heuristic 1) |
| DF – Depth-First | GR2 – Greedy (heuristic 2) |
| ID – Iterative Deepening | AS1 – A* (heuristic 1) |
| UC – Uniform Cost | AS2 – A* (heuristic 2) |

**Each algorithm returns:**
- Sequence of actions
- Path cost
- Number of nodes expanded

## 🧠 Core Components

### `GenericSearch`
A reusable search engine implementing all algorithms. Handles:
- Frontier management
- Node expansion
- Cost accumulation
- Heuristic evaluation
- Return of `SearchResult`

### `DeliverySearch`
Encodes the AI delivery problem as a search problem:
- State representation
- Action generation (UP, DOWN, LEFT, RIGHT, TUNNEL)
- Transition model
- Cost computation (traffic or Manhattan for tunnels)
- Parsing of input strings
- Integration with GenericSearch

**Provides required static methods:**
- `GenGrid()` – random initial state string generator
- `GenTraffic()` – random traffic string generator
- `path()` – runs a single search strategy
- `plan()` – assigns trucks to customers
- `solve()` – full project entry point

### `DeliveryPlanner`
Computes the best truck assignment for multiple customers by:
- Evaluating each (truck, customer) pair
- Selecting the minimal-cost route
- Returning formatted results

## 📝 Input Format

### Initial State (`GenGrid()` output)
m;n;P;S;customerX1,customerY1,...;tunnelX1,tunnelY1,tunnelX'1,tunnelY'1,...
**Example:**
5;5;2;1;4,4,2,2;0,0,4,4

### Traffic Format (`GenTraffic()` output)
Each edge is encoded as:
srcX,srcY,dstX,dstY,traffic;

**Example:**
0,0,1,0,1;0,0,0,1,1; ...

## 📤 Output Format (`solve()`)

For each (Truck, Customer) pair:

(TruckX,CustomerY);action1,action2,...;pathCost;nodes

**Example:**

(Truck0,Customer0);tunnel;8;8

(Truck1,Customer1);down,right;2;10

This output is compliant with the assignment requirements.

## 🧪 Tests
Located in the `tests` package.

**Included checks:**
- Proper `initialState` formatting
- Proper `traffic` string formatting
- `solve()` returns valid delivery pairs and correct structure

These tests validate the format and basic correctness of the system.


The project strictly follows the required two-package layout: `code` and `tests`.

## ▶️ Running the Project

**Compile:**
```bash
javac -cp . src/code/*.java

java -cp src code.Main