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
  const [resultText, setResultText] = useState<string>("");

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
      setResultText("");
    } catch (error) {
      console.error('Error generating grid:', error);
    }
  };

  const handlePlanRoutes = async () => {
    if (!gridData) return;

    setIsRunning(true);
    try {
      const formData = new FormData();
      formData.append('initialState', gridData.grid.replace(/\n/g, '\\n'));
      formData.append('traffic', gridData.traffic);
      formData.append('strategy', strategyMap[selectedStrategy]);

      const response = await fetch('http://localhost:8080/api/planRoutes', {
        method: 'POST',
        body: formData
      });
      if (!response.ok) {
        const errorText = await response.text();
        console.error('API Error:', response.status, errorText);
        throw new Error(`API Error: ${response.status} ${errorText}`);
      }
      const data = await response.text();
      console.log('API Response:', data);
      setResultText(data);
      setRoutes([]);
      setPerformanceData([]);

    } catch (error) {
      console.error('Error planning routes:', error);
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

      {resultText && (
        <div className="bg-card p-6 rounded-lg border shadow-sm mb-6">
          <h3 className="text-lg font-semibold mb-4">Route Planning Results</h3>
          <pre className="text-sm font-mono bg-muted p-4 rounded overflow-x-auto whitespace-pre-wrap">
            {resultText}
          </pre>
        </div>
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
