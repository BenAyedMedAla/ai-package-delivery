package code;

public class StrategyResult {
    private String strategy;
    private long timeMs;
    private int nodesExpanded;
    private int totalCost;
    private int deliveries;
    private long memoryKB;
    private long cpuMs;

    public StrategyResult() {}

    public StrategyResult(String strategy, long timeMs, int nodesExpanded, int totalCost, int deliveries, long memoryKB, long cpuMs) {
        this.strategy = strategy;
        this.timeMs = timeMs;
        this.nodesExpanded = nodesExpanded;
        this.totalCost = totalCost;
        this.deliveries = deliveries;
        this.memoryKB = memoryKB;
        this.cpuMs = cpuMs;
    }

    // Getters and setters
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public long getTimeMs() { return timeMs; }
    public void setTimeMs(long timeMs) { this.timeMs = timeMs; }

    public int getNodesExpanded() { return nodesExpanded; }
    public void setNodesExpanded(int nodesExpanded) { this.nodesExpanded = nodesExpanded; }

    public int getTotalCost() { return totalCost; }
    public void setTotalCost(int totalCost) { this.totalCost = totalCost; }

    public int getDeliveries() { return deliveries; }
    public void setDeliveries(int deliveries) { this.deliveries = deliveries; }

    public long getMemoryKB() { return memoryKB; }
    public void setMemoryKB(long memoryKB) { this.memoryKB = memoryKB; }

    public long getCpuMs() { return cpuMs; }
    public void setCpuMs(long cpuMs) { this.cpuMs = cpuMs; }
}