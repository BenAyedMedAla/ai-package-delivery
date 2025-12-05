import { useState } from "react";
import { DeliveryGrid } from "@/components/DeliveryGrid";
import { ControlPanel } from "@/components/ControlPanel";
import { PerformanceMetrics } from "@/components/PerformanceMetrics";
import { Package, Truck } from "lucide-react";
import { Card } from "@/components/ui/card";

interface Position {
  x: number;
  y: number;
}

interface Tunnel {
  from: Position;
  to: Position;
}

interface GridData {
  rows: number;
  columns: number;
  grid: string[][];
  stores: Position[];
  customers: Position[];
  tunnels: Tunnel[];
}

interface Route {
  truck: string;
  customer: string;
  path: string;
  cost: number;
  nodesExpanded: number;
}

interface PerformanceEntry {
  strategy: string;
  pathCost: number;
  nodesExpanded: number;
  timeTaken: string;
  success: boolean;
}

interface BackendRoute {
  assignment: string;
  path: string;
  cost: number;
  totalCost: number;
}

interface BackendPerformanceEntry {
  strategy: string;
  pathCost: number;
  nodesExpanded: number;
  timeTaken: string;
  status: string;
}

interface PlanResponse {
  routes: BackendRoute[];
  totalRoutes: number;
  avgPathCost: number;
  avgNodesExpanded: number;
  avgTime: number;
  performanceTable: BackendPerformanceEntry[];
}

interface SingleStrategyMetrics {
  cpuMs: number;
  nodesExpanded: number;
  totalCost: number;
  timeMs: number;
  deliveries: number;
  memoryKB: number;
}

interface SingleStrategyResponse {
  steps: string[];
  metrics: SingleStrategyMetrics;
}

interface AllStrategiesResult {
  strategy: string;
  timeMs: number;
  nodesExpanded: number;
  totalCost: number;
}

interface AllStrategiesResponse {
  results: AllStrategiesResult[];
}

const Index = () => {
  const [selectedStrategy, setSelectedStrategy] = useState("BFS");
  const [numTrucks, setNumTrucks] = useState(2);
  const [numCustomers, setNumCustomers] = useState(3);
  const [gridRows, setGridRows] = useState(5);
  const [gridCols, setGridCols] = useState(5);
  const [isRunning, setIsRunning] = useState(false);
  const [gridData, setGridData] = useState<GridData | null>(null);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [performanceData, setPerformanceData] = useState<PerformanceEntry[]>(
    []
  );
  const [resultText, setResultText] = useState<string>("");
  const [singleStrategyResult, setSingleStrategyResult] =
    useState<SingleStrategyResponse | null>(null);
  const [allStrategiesResult, setAllStrategiesResult] =
    useState<AllStrategiesResponse | null>(null);

  const strategyMap = {
    BFS: "BF",
    DFS: "DF",
    "A*": "AS1",
    Greedy: "GR1",
    UCS: "UC",
  };

  const handleGenerateGrid = async () => {
    try {
      console.log("Sending request to generate grid:", {
        rows: gridRows,
        columns: gridCols,
        numStores: 2,
        numCustomers: numCustomers,
      });

      const response = await fetch("http://localhost:8080/api/generate-grid", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          rows: gridRows,
          columns: gridCols,
          numStores: 2,
          numCustomers: numCustomers,
        }),
      });

      console.log("Response status:", response.status);
      console.log(
        "Response headers:",
        Object.fromEntries(response.headers.entries())
      );

      const data = await response.json();
      console.log("Grid data received from backend:", data);

      setGridData(data);
      setRoutes([]);
      setPerformanceData([]);
      setResultText("");
    } catch (error) {
      console.error("Error generating grid:", error);
    }
  };

  const handlePlanRoutes = async () => {
    if (!gridData) return;

    setIsRunning(true);
    try {
      const response = await fetch(
        "http://localhost:8080/api/choose-strategy",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            strategy:
              selectedStrategy === "all"
                ? "all"
                : strategyMap[selectedStrategy],
          }),
        }
      );
      if (!response.ok) {
        const errorText = await response.text();
        console.error("API Error:", response.status, errorText);
        throw new Error(`API Error: ${response.status} ${errorText}`);
      }
      const data = await response.json();
      console.log("API Response:", data);

      // Clear previous results
      setSingleStrategyResult(null);
      setAllStrategiesResult(null);
      setResultText("");

      // Check if it's all strategies or single strategy
      if (data.results) {
        // All strategies response
        setAllStrategiesResult(data as AllStrategiesResponse);
      } else if (data.steps && data.metrics) {
        // Single strategy response
        setSingleStrategyResult(data as SingleStrategyResponse);
      }

      setRoutes([]);
      setPerformanceData([]);
    } catch (error) {
      console.error("Error planning routes:", error);
      setResultText(`Error: ${error.message}`);
    } finally {
      setIsRunning(false);
    }
  };

  const handleReset = () => {
    setGridData(null);
    setRoutes([]);
    setPerformanceData([]);
    setResultText("");
    setSingleStrategyResult(null);
    setAllStrategiesResult(null);
    setIsRunning(false);
  };

  return (
    <div className="min-h-screen bg-background p-6">
      <header className="mb-8">
        <div className="flex items-center gap-3 mb-2">
          <div className="bg-gradient-to-br from-primary to-route p-3 rounded-lg shadow-[var(--shadow-glow)]">
            <Package className="w-8 h-8 text-background" />
          </div>
          <h1 className="text-4xl font-bold text-foreground">
            AI Delivery Planner
          </h1>
        </div>
        <p className="text-muted-foreground text-lg">
          Optimal route planning with advanced pathfinding algorithms
        </p>
      </header>

      {resultText && (
        <div className="bg-card p-6 rounded-lg border shadow-sm mb-6">
          <h3 className="text-lg font-semibold mb-4">Route Planning Results</h3>
          <pre className="text-sm font-mono bg-muted p-4 rounded overflow-x-auto whitespace-pre-wrap">
            {resultText}
          </pre>
        </div>
      )}

      {/* Single Strategy Result */}
      {singleStrategyResult && (
        <div className="mb-6 space-y-6">
          {/* Steps Section */}
          <Card className="p-6">
            <h3 className="text-xl font-bold text-foreground mb-4">
              Delivery Steps
            </h3>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {singleStrategyResult.steps.map((step, index) => (
                <div
                  key={index}
                  className={`p-3 rounded-lg ${
                    step.includes("Complete") || step.includes("Initial")
                      ? "bg-primary/10 border border-primary/20 font-semibold"
                      : step.includes("assigned")
                      ? "bg-secondary/10 border border-secondary/20"
                      : "bg-muted"
                  }`}
                >
                  <p className="text-sm">{step}</p>
                </div>
              ))}
            </div>
          </Card>

          {/* Metrics Section */}
          <Card className="p-6">
            <h3 className="text-xl font-bold text-foreground mb-4">
              Performance Metrics
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Total Cost</p>
                <p className="text-2xl font-bold text-primary">
                  {singleStrategyResult.metrics.totalCost}
                </p>
              </div>
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">
                  Nodes Expanded
                </p>
                <p className="text-2xl font-bold text-secondary">
                  {singleStrategyResult.metrics.nodesExpanded}
                </p>
              </div>
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Time (ms)</p>
                <p className="text-2xl font-bold text-route">
                  {singleStrategyResult.metrics.timeMs}
                </p>
              </div>
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">
                  CPU Time (ms)
                </p>
                <p className="text-2xl font-bold">
                  {singleStrategyResult.metrics.cpuMs}
                </p>
              </div>
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Deliveries</p>
                <p className="text-2xl font-bold">
                  {singleStrategyResult.metrics.deliveries}
                </p>
              </div>
              <div className="bg-muted p-4 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">
                  Memory (KB)
                </p>
                <p className="text-2xl font-bold">
                  {singleStrategyResult.metrics.memoryKB}
                </p>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* All Strategies Comparison */}
      {allStrategiesResult && (
        <Card className="p-6 mb-6">
          <h3 className="text-xl font-bold text-foreground mb-4">
            Strategy Comparison
          </h3>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left p-3 font-semibold">Strategy</th>
                  <th className="text-right p-3 font-semibold">Time (ms)</th>
                  <th className="text-right p-3 font-semibold">
                    Nodes Expanded
                  </th>
                  <th className="text-right p-3 font-semibold">Total Cost</th>
                </tr>
              </thead>
              <tbody>
                {allStrategiesResult.results.map((result, index) => (
                  <tr
                    key={index}
                    className="border-b border-border/50 hover:bg-muted/50 transition-colors"
                  >
                    <td className="p-3 font-medium">{result.strategy}</td>
                    <td className="p-3 text-right">{result.timeMs}</td>
                    <td className="p-3 text-right">{result.nodesExpanded}</td>
                    <td className="p-3 text-right font-semibold text-primary">
                      {result.totalCost}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Best Strategy Summary */}
          <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card className="p-4 bg-primary/10 border-primary/20">
              <p className="text-sm text-muted-foreground mb-1">
                Fastest Strategy
              </p>
              <p className="text-lg font-bold">
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.timeMs < min.timeMs ? r : min
                  ).strategy
                }
              </p>
              <p className="text-sm text-muted-foreground">
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.timeMs < min.timeMs ? r : min
                  ).timeMs
                }{" "}
                ms
              </p>
            </Card>
            <Card className="p-4 bg-secondary/10 border-secondary/20">
              <p className="text-sm text-muted-foreground mb-1">Fewest Nodes</p>
              <p className="text-lg font-bold">
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.nodesExpanded < min.nodesExpanded ? r : min
                  ).strategy
                }
              </p>
              <p className="text-sm text-muted-foreground">
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.nodesExpanded < min.nodesExpanded ? r : min
                  ).nodesExpanded
                }{" "}
                nodes
              </p>
            </Card>
            <Card className="p-4 bg-route/10 border-route/20">
              <p className="text-sm text-muted-foreground mb-1">Lowest Cost</p>
              <p className="text-lg font-bold">
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.totalCost < min.totalCost ? r : min
                  ).strategy
                }
              </p>
              <p className="text-sm text-muted-foreground">
                Cost:{" "}
                {
                  allStrategiesResult.results.reduce((min, r) =>
                    r.totalCost < min.totalCost ? r : min
                  ).totalCost
                }
              </p>
            </Card>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <DeliveryGrid gridData={gridData} routes={routes} />
        </div>

        <div>
          <ControlPanel
            selectedStrategy={selectedStrategy}
            onStrategyChange={setSelectedStrategy}
            numTrucks={numTrucks}
            onTrucksChange={setNumTrucks}
            numCustomers={numCustomers}
            onCustomersChange={setNumCustomers}
            gridRows={gridRows}
            onGridRowsChange={setGridRows}
            gridCols={gridCols}
            onGridColsChange={setGridCols}
            onGenerateGrid={handleGenerateGrid}
            onPlanRoutes={handlePlanRoutes}
            onReset={handleReset}
            isRunning={isRunning}
          />
        </div>
      </div>
    </div>
  );
};

export default Index;
