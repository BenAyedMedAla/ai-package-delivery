package code;

import java.util.List;

public class Grid {
    private int rows;
    private int columns;
    private List<Position> grid;
    private List<Position> stores;
    private List<Position> customers;
    private List<TunnelDto> tunnels;
    private String traffic;

    public Grid() {}

    public Grid(int rows, int columns, List<Position> grid, List<Position> stores, List<Position> customers, List<TunnelDto> tunnels, String traffic) {
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

    public List<Position> getGrid() { return grid; }
    public void setGrid(List<Position> grid) { this.grid = grid; }

    public List<Position> getStores() { return stores; }
    public void setStores(List<Position> stores) { this.stores = stores; }

    public List<Position> getCustomers() { return customers; }
    public void setCustomers(List<Position> customers) { this.customers = customers; }

    public List<TunnelDto> getTunnels() { return tunnels; }
    public void setTunnels(List<TunnelDto> tunnels) { this.tunnels = tunnels; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }
}