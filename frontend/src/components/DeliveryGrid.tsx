import { Card } from "@/components/ui/card";
import { GridCell } from "./GridCell";

export type CellType =
  | "empty"
  | "store"
  | "customer"
  | "truck"
  | "blocked"
  | "road"
  | "tunnel";

export interface GridCellData {
  type: CellType;
  traffic?: number; // 0-4, 0 = blocked
  hasTruck?: boolean;
  truckId?: number;
  customerId?: number;
  storeId?: number;
  isTunnelEntrance?: boolean;
  tunnelId?: number;
}

// Parse grid data from backend
const parseGridData = (
  gridData: GridData | null,
  routes: Route[]
): GridCellData[][] => {
  if (!gridData) {
    return Array(5)
      .fill(null)
      .map(() =>
        Array(5)
          .fill(null)
          .map(() => ({ type: "empty" as CellType }))
      );
  }

  const m = gridData.rows;
  const n = gridData.columns;

  // Initialize grid with all cells as roads
  const grid: GridCellData[][] = Array(m)
    .fill(null)
    .map(() =>
      Array(n)
        .fill(null)
        .map(() => ({ type: "road" as CellType, traffic: 1 }))
    );

  // Mark stores
  gridData.stores.forEach((store, index) => {
    if (store.x >= 0 && store.x < m && store.y >= 0 && store.y < n) {
      grid[store.x][store.y] = { type: "store", storeId: index + 1 };
    }
  });

  // Mark customers
  gridData.customers.forEach((customer, index) => {
    if (
      customer.x >= 0 &&
      customer.x < m &&
      customer.y >= 0 &&
      customer.y < n
    ) {
      grid[customer.x][customer.y] = {
        type: "customer",
        customerId: index + 1,
      };
    }
  });

  // Mark tunnels
  gridData.tunnels.forEach((tunnel, index) => {
    if (
      tunnel.from.x >= 0 &&
      tunnel.from.x < m &&
      tunnel.from.y >= 0 &&
      tunnel.from.y < n
    ) {
      // Only mark as tunnel if not already a store or customer
      if (grid[tunnel.from.x][tunnel.from.y].type === "road") {
        grid[tunnel.from.x][tunnel.from.y] = {
          type: "tunnel",
          isTunnelEntrance: true,
          tunnelId: index + 1,
        };
      }
    }
    if (
      tunnel.to.x >= 0 &&
      tunnel.to.x < m &&
      tunnel.to.y >= 0 &&
      tunnel.to.y < n
    ) {
      if (grid[tunnel.to.x][tunnel.to.y].type === "road") {
        grid[tunnel.to.x][tunnel.to.y] = {
          type: "tunnel",
          isTunnelEntrance: true,
          tunnelId: index + 1,
        };
      }
    }
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

interface DeliveryGridProps {
  gridData: GridData | null;
  routes: Route[];
}

export const DeliveryGrid = ({ gridData, routes }: DeliveryGridProps) => {
  const parsedGridData = parseGridData(gridData, routes);

  const gridRows = parsedGridData.length;
  const gridCols = parsedGridData.length > 0 ? parsedGridData[0].length : 0;

  return (
    <Card className="p-6 bg-gradient-to-br from-card to-card/80 border-border shadow-[var(--shadow-card)]">
      <div className="mb-4">
        <h2 className="text-2xl font-bold text-foreground mb-1">City Grid</h2>
        <p className="text-muted-foreground text-sm">
          {gridRows}×{gridCols} grid with stores, customers, and traffic levels
        </p>
      </div>

      <div
        className={`inline-grid gap-2 p-4 bg-background/50 rounded-lg`}
        style={{ gridTemplateColumns: `repeat(${gridCols}, minmax(0, 1fr))` }}
      >
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
          <div className="w-4 h-4 bg-purple-500 rounded"></div>
          <span className="text-foreground">Tunnel</span>
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
