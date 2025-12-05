import { Store, User, Truck, X, Repeat } from "lucide-react";
import { GridCellData } from "./DeliveryGrid";
import { cn } from "@/lib/utils";

interface GridCellProps {
  cell: GridCellData;
  row: number;
  col: number;
}

export const GridCell = ({ cell, row, col }: GridCellProps) => {
  const getBackgroundColor = () => {
    if (cell.type === "store") return "bg-store";
    if (cell.type === "customer") return "bg-customer";
    if (cell.type === "tunnel") return "bg-purple-500";
    if (cell.type === "blocked") return "bg-blocked";
    if (cell.type === "road" && cell.traffic) {
      return `bg-traffic-${cell.traffic}`;
    }
    return "bg-muted";
  };

  const getIcon = () => {
    if (cell.hasTruck) return <Truck className="w-5 h-5 text-truck" />;
    if (cell.type === "store")
      return <Store className="w-6 h-6 text-background" />;
    if (cell.type === "customer")
      return <User className="w-6 h-6 text-background" />;
    if (cell.type === "tunnel")
      return <Repeat className="w-6 h-6 text-background" />;
    if (cell.type === "blocked")
      return <X className="w-6 h-6 text-foreground/50" />;
    return null;
  };

  const getBadge = () => {
    if (cell.type === "store" && cell.storeId) {
      return (
        <span className="absolute top-1 right-1 text-xs font-bold text-background">
          S{cell.storeId}
        </span>
      );
    }
    if (cell.type === "customer" && cell.customerId) {
      return (
        <span className="absolute top-1 right-1 text-xs font-bold text-background">
          C{cell.customerId}
        </span>
      );
    }
    if (cell.type === "tunnel" && cell.tunnelId) {
      return (
        <span className="absolute top-1 right-1 text-xs font-bold text-background">
          T{cell.tunnelId}
        </span>
      );
    }
    if (cell.type === "road" && cell.traffic) {
      return (
        <span className="absolute bottom-1 right-1 text-xs font-bold text-background/80">
          {cell.traffic}
        </span>
      );
    }
    return null;
  };

  return (
    <div
      className={cn(
        "relative w-20 h-20 rounded-lg border-2 border-border/50 flex items-center justify-center transition-all duration-300 hover:scale-105 hover:border-primary cursor-pointer",
        getBackgroundColor()
      )}
      style={{
        boxShadow:
          cell.type === "store" ||
          cell.type === "customer" ||
          cell.type === "tunnel"
            ? "0 0 20px currentColor"
            : "none",
      }}
    >
      {getIcon()}
      {getBadge()}
    </div>
  );
};
