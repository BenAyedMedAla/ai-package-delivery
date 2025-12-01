import { useState } from "react";
import { DeliveryGrid } from "@/components/DeliveryGrid";
import { ControlPanel } from "@/components/ControlPanel";
import { PerformanceMetrics } from "@/components/PerformanceMetrics";
import { Package, Truck } from "lucide-react";

const Index = () => {
  const [selectedStrategy, setSelectedStrategy] = useState("BFS");
  const [numTrucks, setNumTrucks] = useState(2);
  const [numCustomers, setNumCustomers] = useState(3);
  const [isRunning, setIsRunning] = useState(false);

  const handleGenerateGrid = () => {
    console.log("Generating new grid...");
    // TODO: Call API to generate grid
  };

  const handlePlanRoutes = () => {
    setIsRunning(true);
    console.log("Planning routes with:", { selectedStrategy, numTrucks, numCustomers });
    // TODO: Call API to plan routes
    setTimeout(() => setIsRunning(false), 2000); // Simulated delay
  };

  const handleReset = () => {
    console.log("Resetting grid...");
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
          <DeliveryGrid />
          <PerformanceMetrics />
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
