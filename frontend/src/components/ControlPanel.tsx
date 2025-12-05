import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Slider } from "@/components/ui/slider";
import { Play, RotateCcw, Sparkles } from "lucide-react";

interface ControlPanelProps {
  selectedStrategy: string;
  onStrategyChange: (strategy: string) => void;
  numTrucks: number;
  onTrucksChange: (num: number) => void;
  numCustomers: number;
  onCustomersChange: (num: number) => void;
  gridRows: number;
  onGridRowsChange: (rows: number) => void;
  gridCols: number;
  onGridColsChange: (cols: number) => void;
  onGenerateGrid: () => void;
  onPlanRoutes: () => void;
  onReset: () => void;
  isRunning: boolean;
}

const strategies = [
  { value: "BFS", label: "Breadth-First Search" },
  { value: "DFS", label: "Depth-First Search" },
  { value: "A*", label: "A* Search" },
  { value: "Greedy", label: "Greedy Best-First" },
  { value: "UCS", label: "Uniform Cost Search" },
  { value: "all", label: "All Strategies" },
];

export const ControlPanel = ({
  selectedStrategy,
  onStrategyChange,
  numTrucks,
  onTrucksChange,
  numCustomers,
  onCustomersChange,
  gridRows,
  onGridRowsChange,
  gridCols,
  onGridColsChange,
  onGenerateGrid,
  onPlanRoutes,
  onReset,
  isRunning,
}: ControlPanelProps) => {
  return (
    <Card className="p-6 bg-gradient-to-br from-card to-card/80 border-border shadow-[var(--shadow-card)] sticky top-6">
      <h2 className="text-2xl font-bold text-foreground mb-6 flex items-center gap-2">
        <Sparkles className="w-6 h-6 text-primary" />
        Control Panel
      </h2>

      <div className="space-y-6">
        <div className="space-y-2">
          <Label htmlFor="strategy" className="text-foreground">
            Search Strategy
          </Label>
          <Select value={selectedStrategy} onValueChange={onStrategyChange}>
            <SelectTrigger
              id="strategy"
              className="bg-background border-border"
            >
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {strategies.map((strategy) => (
                <SelectItem key={strategy.value} value={strategy.value}>
                  {strategy.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-3">
          <div className="flex justify-between">
            <Label className="text-foreground">Number of Trucks</Label>
            <span className="text-primary font-bold">{numTrucks}</span>
          </div>
          <Slider
            value={[numTrucks]}
            onValueChange={([value]) => onTrucksChange(value)}
            min={1}
            max={5}
            step={1}
            className="cursor-pointer"
          />
        </div>

        <div className="space-y-3">
          <div className="flex justify-between">
            <Label className="text-foreground">Number of Customers</Label>
            <span className="text-primary font-bold">{numCustomers}</span>
          </div>
          <Slider
            value={[numCustomers]}
            onValueChange={([value]) => onCustomersChange(value)}
            min={1}
            max={10}
            step={1}
            className="cursor-pointer"
          />
        </div>

        <div className="space-y-3">
          <div className="flex justify-between">
            <Label className="text-foreground">Grid Rows</Label>
            <span className="text-primary font-bold">{gridRows}</span>
          </div>
          <Slider
            value={[gridRows]}
            onValueChange={([value]) => onGridRowsChange(value)}
            min={3}
            max={15}
            step={1}
            className="cursor-pointer"
          />
        </div>

        <div className="space-y-3">
          <div className="flex justify-between">
            <Label className="text-foreground">Grid Columns</Label>
            <span className="text-primary font-bold">{gridCols}</span>
          </div>
          <Slider
            value={[gridCols]}
            onValueChange={([value]) => onGridColsChange(value)}
            min={3}
            max={15}
            step={1}
            className="cursor-pointer"
          />
        </div>

        <div className="pt-4 space-y-3">
          <Button
            onClick={onGenerateGrid}
            className="w-full bg-secondary hover:bg-secondary/80 text-secondary-foreground font-semibold"
            disabled={isRunning}
          >
            <RotateCcw className="w-4 h-4 mr-2" />
            Generate New Grid
          </Button>

          <Button
            onClick={onPlanRoutes}
            className="w-full bg-primary hover:bg-primary/80 text-primary-foreground font-semibold shadow-[var(--shadow-glow)]"
            disabled={isRunning}
          >
            <Play className="w-4 h-4 mr-2" />
            {isRunning ? "Planning Routes..." : "Plan Routes"}
          </Button>

          <Button
            onClick={onReset}
            variant="outline"
            className="w-full border-border hover:bg-muted"
            disabled={isRunning}
          >
            Reset
          </Button>
        </div>
      </div>
    </Card>
  );
};
