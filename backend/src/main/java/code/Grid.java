package code;

import java.util.List;
import java.util.Map;

public class Grid {
    private int rows;
    private int columns;
    private List<List<String>> grid;
    private List<Position> stores;
    private List<Position> customers;
    private List<TunnelDto> tunnels;
    private Map<String, Integer> traffic;

    public Grid() {}

    public Grid(int rows, int columns, List<List<String>> grid, List<Position> stores, List<Position> customers, List<TunnelDto> tunnels, Map<String, Integer> traffic) {
        this.rows = rows;
        this.columns = columns;
        this.grid = grid;
        this.stores = stores;
        this.customers = customers;
        this.tunnels = tunnels;
        this.traffic = traffic;
    }

    // Getters and setters
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }

    public List<List<String>> getGrid() { return grid; }
    public void setGrid(List<List<String>> grid) { this.grid = grid; }

    public List<Position> getStores() { return stores; }
    public void setStores(List<Position> stores) { this.stores = stores; }

    public List<Position> getCustomers() { return customers; }
    public void setCustomers(List<Position> customers) { this.customers = customers; }

    public List<TunnelDto> getTunnels() { return tunnels; }
    public void setTunnels(List<TunnelDto> tunnels) { this.tunnels = tunnels; }

    public Map<String, Integer> getTraffic() { return traffic; }
    public void setTraffic(Map<String, Integer> traffic) { this.traffic = traffic; }
}