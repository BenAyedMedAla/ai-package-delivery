import { Card } from "@/components/ui/card";
import { GridCell } from "./GridCell";

export type CellType = "empty" | "store" | "customer" | "truck" | "blocked" | "road";

export interface GridCellData {
  type: CellType;
  traffic?: number; // 0-4, 0 = blocked
  hasTruck?: boolean;
  truckId?: number;
  customerId?: number;
  storeId?: number;
}

// Sample grid data for demonstration
const createSampleGrid = (): GridCellData[][] => {
  const grid: GridCellData[][] = Array(5).fill(null).map(() =>
    Array(5).fill(null).map(() => ({ type: "road" as CellType, traffic: Math.floor(Math.random() * 4) + 1 }))
  );
  
  // Add stores
  grid[0][0] = { type: "store", storeId: 1 };
  grid[4][4] = { type: "store", storeId: 2 };
  
  // Add customers
  grid[1][3] = { type: "customer", customerId: 1 };
  grid[3][1] = { type: "customer", customerId: 2 };
  grid[2][2] = { type: "customer", customerId: 3 };
  
  // Add blocked roads
  grid[2][0] = { type: "blocked", traffic: 0 };
  grid[1][2] = { type: "blocked", traffic: 0 };
  
  return grid;
};

export const DeliveryGrid = () => {
  const gridData = createSampleGrid();

  return (
    <Card className="p-6 bg-gradient-to-br from-card to-card/80 border-border shadow-[var(--shadow-card)]">
      <div className="mb-4">
        <h2 className="text-2xl font-bold text-foreground mb-1">City Grid</h2>
        <p className="text-muted-foreground text-sm">5×5 grid with stores, customers, and traffic levels</p>
      </div>
      
      <div className="inline-grid grid-cols-5 gap-2 p-4 bg-background/50 rounded-lg">
        {gridData.map((row, rowIndex) =>
          row.map((cell, colIndex) => (
            <GridCell
              key={`${rowIndex}-${colIndex}`}
              cell={cell}
              row={rowIndex}
              col={colIndex}
            />
          ))
        )}
      </div>

      <div className="mt-6 flex flex-wrap gap-4 text-sm">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-store rounded"></div>
          <span className="text-foreground">Store</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-customer rounded"></div>
          <span className="text-foreground">Customer</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-truck rounded"></div>
          <span className="text-foreground">Truck</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-blocked rounded"></div>
          <span className="text-foreground">Blocked</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-gradient-to-r from-traffic-1 to-traffic-4 rounded"></div>
          <span className="text-foreground">Traffic (1-4)</span>
        </div>
      </div>
    </Card>
  );
};
