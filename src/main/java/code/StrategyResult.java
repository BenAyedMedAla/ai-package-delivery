package code;

public class StrategyResult {
    private String strategy;
    private long timeMs;
    private int nodesExpanded;
    private int totalCost;

    public StrategyResult() {}

    public StrategyResult(String strategy, long timeMs, int nodesExpanded, int totalCost) {
        this.strategy = strategy;
        this.timeMs = timeMs;
        this.nodesExpanded = nodesExpanded;
        this.totalCost = totalCost;
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
}