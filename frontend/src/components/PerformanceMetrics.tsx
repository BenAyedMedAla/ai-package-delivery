import { Card } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Activity, Clock, TrendingUp, Route } from "lucide-react";

// Sample performance data
const performanceData = [
  {
    strategy: "BFS",
    pathCost: 24,
    nodesExpanded: 45,
    timeTaken: "12ms",
    success: true,
  },
  {
    strategy: "A*",
    pathCost: 18,
    nodesExpanded: 28,
    timeTaken: "8ms",
    success: true,
  },
  {
    strategy: "Greedy",
    pathCost: 22,
    nodesExpanded: 31,
    timeTaken: "9ms",
    success: true,
  },
];

export const PerformanceMetrics = () => {
  return (
    <Card className="p-6 bg-gradient-to-br from-card to-card/80 border-border shadow-[var(--shadow-card)]">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground mb-1 flex items-center gap-2">
          <Activity className="w-6 h-6 text-primary" />
          Performance Metrics
        </h2>
        <p className="text-muted-foreground text-sm">Algorithm comparison and statistics</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-background/50 p-4 rounded-lg border border-border">
          <div className="flex items-center gap-2 mb-2">
            <Route className="w-5 h-5 text-route" />
            <span className="text-muted-foreground text-sm">Total Routes</span>
          </div>
          <p className="text-2xl font-bold text-foreground">3</p>
        </div>

        <div className="bg-background/50 p-4 rounded-lg border border-border">
          <div className="flex items-center gap-2 mb-2">
            <TrendingUp className="w-5 h-5 text-accent" />
            <span className="text-muted-foreground text-sm">Avg Path Cost</span>
          </div>
          <p className="text-2xl font-bold text-foreground">21.3</p>
        </div>

        <div className="bg-background/50 p-4 rounded-lg border border-border">
          <div className="flex items-center gap-2 mb-2">
            <Activity className="w-5 h-5 text-primary" />
            <span className="text-muted-foreground text-sm">Avg Nodes</span>
          </div>
          <p className="text-2xl font-bold text-foreground">34.7</p>
        </div>

        <div className="bg-background/50 p-4 rounded-lg border border-border">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="w-5 h-5 text-secondary" />
            <span className="text-muted-foreground text-sm">Avg Time</span>
          </div>
          <p className="text-2xl font-bold text-foreground">9.7ms</p>
        </div>
      </div>

      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow className="border-border hover:bg-muted/50">
              <TableHead className="text-foreground font-semibold">Strategy</TableHead>
              <TableHead className="text-foreground font-semibold">Path Cost</TableHead>
              <TableHead className="text-foreground font-semibold">Nodes Expanded</TableHead>
              <TableHead className="text-foreground font-semibold">Time Taken</TableHead>
              <TableHead className="text-foreground font-semibold">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {performanceData.map((data) => (
              <TableRow key={data.strategy} className="border-border hover:bg-muted/30">
                <TableCell className="font-medium text-foreground">{data.strategy}</TableCell>
                <TableCell className="text-accent font-semibold">{data.pathCost}</TableCell>
                <TableCell className="text-primary">{data.nodesExpanded}</TableCell>
                <TableCell className="text-muted-foreground">{data.timeTaken}</TableCell>
                <TableCell>
                  <span className={`inline-flex items-center px-2 py-1 rounded text-xs font-semibold ${
                    data.success 
                      ? "bg-accent/20 text-accent" 
                      : "bg-destructive/20 text-destructive"
                  }`}>
                    {data.success ? "Success" : "Failed"}
                  </span>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </Card>
  );
};
