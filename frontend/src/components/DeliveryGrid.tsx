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

// Parse grid data from backend
const parseGridData = (gridData: GridData | null, routes: Route[]): GridCellData[][] => {
  if (!gridData) {
    return Array(5).fill(null).map(() =>
      Array(5).fill(null).map(() => ({ type: "empty" as CellType }))
    );
  }

  // Parse the grid string: "5 5\n1 1 1 1 1\n...\n0\n\n1\n0 0\n1\n4 4\n1\n0 0\n"
  const lines = gridData.grid.trim().split('\n');
  let idx = 0;
  const mn = lines[idx++].split(' ');
  const m = parseInt(mn[0]);
  const n = parseInt(mn[1]);

  const grid: GridCellData[][] = Array(m).fill(null).map(() =>
    Array(n).fill(null).map(() => ({ type: "road" as CellType, traffic: 1 }))
  );

  // Parse traffic matrix
  for (let i = 0; i < m; i++) {
    const row = lines[idx++].split(' ').map(Number);
    for (let j = 0; j < n; j++) {
      grid[i][j].traffic = row[j];
      if (row[j] === 0) {
        grid[i][j].type = "blocked";
      }
    }
  }

  // Skip tunnels for now
  const numTunnels = parseInt(lines[idx++]);
  idx += numTunnels;

  // Parse stores
  const numStores = parseInt(lines[idx++]);
  for (let i = 0; i < numStores; i++) {
    const [x, y] = lines[idx++].split(' ').map(Number);
    if (x >= 0 && x < m && y >= 0 && y < n) {
      grid[x][y] = { type: "store", storeId: i + 1 };
    }
  }

  // Parse customers
  const numCustomers = parseInt(lines[idx++]);
  for (let i = 0; i < numCustomers; i++) {
    const [x, y] = lines[idx++].split(' ').map(Number);
    if (x >= 0 && x < m && y >= 0 && y < n) {
      grid[x][y] = { type: "customer", customerId: i + 1 };
    }
  }

  // Parse trucks
  const numTrucks = parseInt(lines[idx++]);
  for (let i = 0; i < numTrucks; i++) {
    const [x, y] = lines[idx++].split(' ').map(Number);
    if (x >= 0 && x < m && y >= 0 && y < n) {
      grid[x][y].hasTruck = true;
      grid[x][y].truckId = i;
    }
  }

  // Add routes visualization (could highlight paths)
  routes.forEach(route => {
    // For now, just ensure trucks are shown
  });

  return grid;
};

interface Route {
  truck: string;
  customer: string;
  path: string;
  cost: number;
  nodesExpanded: number;
}

interface GridData {
  grid: string;
  traffic: string;
  tunnels: string[];
}

interface DeliveryGridProps {
  gridData: GridData | null;
  routes: Route[];
}

export const DeliveryGrid = ({ gridData, routes }: DeliveryGridProps) => {
  const parsedGridData = parseGridData(gridData, routes);

  const gridSize = parsedGridData.length > 0 ? parsedGridData[0].length : 5;

  return (
    <Card className="p-6 bg-gradient-to-br from-card to-card/80 border-border shadow-[var(--shadow-card)]">
      <div className="mb-4">
        <h2 className="text-2xl font-bold text-foreground mb-1">City Grid</h2>
        <p className="text-muted-foreground text-sm">{gridSize}×{gridSize} grid with stores, customers, and traffic levels</p>
      </div>

      <div className={`inline-grid gap-2 p-4 bg-background/50 rounded-lg`} style={{ gridTemplateColumns: `repeat(${gridSize}, minmax(0, 1fr))` }}>
        {parsedGridData.map((row, rowIndex) =>
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
