import { useState } from "react";
import { DeliveryGrid } from "@/components/DeliveryGrid";
import { ControlPanel } from "@/components/ControlPanel";
import { PerformanceMetrics } from "@/components/PerformanceMetrics";
import { Package, Truck } from "lucide-react";

interface GridData {
  grid: string;
  traffic: string;
  tunnels: string[];
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

const Index = () => {
  const [selectedStrategy, setSelectedStrategy] = useState("BFS");
  const [numTrucks, setNumTrucks] = useState(2);
  const [numCustomers, setNumCustomers] = useState(3);
  const [isRunning, setIsRunning] = useState(false);
  const [gridData, setGridData] = useState<GridData | null>(null);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [performanceData, setPerformanceData] = useState<PerformanceEntry[]>([]);

  const strategyMap = {
    "BFS": "BF",
    "DFS": "DF",
    "A*": "AS1",
    "Greedy": "GR1",
    "UCS": "UC"
  };

  const handleGenerateGrid = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/generateGrid?numTrucks=${numTrucks}&numCustomers=${numCustomers}`);
      const data = await response.json();
      setGridData(data);
      setRoutes([]);
      setPerformanceData([]);
    } catch (error) {
      console.error('Error generating grid:', error);
    }
  };

  const handlePlanRoutes = async () => {
    if (!gridData) return;

    setIsRunning(true);
    try {
      const response = await fetch('http://localhost:8080/api/planRoutes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          initialState: gridData.grid.replace(/\n/g, '\\n'),
          traffic: gridData.traffic,
          strategy: strategyMap[selectedStrategy]
        })
      });
      const data: PlanResponse = await response.json();

      // Parse routes
      const parsedRoutes: Route[] = data.routes.map(route => {
        // Parse assignment like "(Truck0,Customer0)" or similar
        const assign = route.assignment.replace(/[()]/g, '');
        const parts = assign.split(',');
        const truck = parts[0].trim();
        const customer = parts[1].trim();
        return {
          truck,
          customer,
          path: route.path,
          cost: route.totalCost, // use totalCost as cost
          nodesExpanded: 0 // not provided, set to 0
        };
      });
      setRoutes(parsedRoutes);

      // Parse performance data
      const parsedPerformance: PerformanceEntry[] = data.performanceTable.map(entry => ({
        strategy: entry.strategy,
        pathCost: entry.pathCost,
        nodesExpanded: entry.nodesExpanded,
        timeTaken: entry.timeTaken,
        success: entry.status === "Success"
      }));
      setPerformanceData(parsedPerformance);

    } catch (error) {
      console.error('Error planning routes:', error);
    } finally {
      setIsRunning(false);
    }
  };

  const handleReset = () => {
    setGridData(null);
    setRoutes([]);
    setPerformanceData([]);
    setIsRunning(false);
  };

  return (
    <div className="min-h-screen bg-background p-6">
      <header className="mb-8">
        <div className="flex items-center gap-3 mb-2">
          <div className="bg-gradient-to-br from-primary to-route p-3 rounded-lg shadow-[var(--shadow-glow)]">
            <Package className="w-8 h-8 text-background" />
          </div>
          <h1 className="text-4xl font-bold text-foreground">AI Delivery Planner</h1>
        </div>
        <p className="text-muted-foreground text-lg">
          Optimal route planning with advanced pathfinding algorithms
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <DeliveryGrid gridData={gridData} routes={routes} />
          <PerformanceMetrics performanceData={performanceData} />
        </div>

        <div>
          <ControlPanel
            selectedStrategy={selectedStrategy}
            onStrategyChange={setSelectedStrategy}
            numTrucks={numTrucks}
            onTrucksChange={setNumTrucks}
            numCustomers={numCustomers}
            onCustomersChange={setNumCustomers}
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
