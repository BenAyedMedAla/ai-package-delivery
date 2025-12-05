package code;

public class GridRequest {
    private int rows;
    private int columns;
    private int numStores;
    private int numCustomers;

    public GridRequest() {}

    public GridRequest(int rows, int columns, int numStores, int numCustomers) {
        this.rows = rows;
        this.columns = columns;
        this.numStores = numStores;
        this.numCustomers = numCustomers;
    }

    // Getters and setters
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }

    public int getNumStores() { return numStores; }
    public void setNumStores(int numStores) { this.numStores = numStores; }

    public int getNumCustomers() { return numCustomers; }
    public void setNumCustomers(int numCustomers) { this.numCustomers = numCustomers; }
}