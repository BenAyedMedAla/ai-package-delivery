package code;

import java.util.List;

public class StrategyResults {
    private List<StrategyResult> results;

    public StrategyResults() {}

    public StrategyResults(List<StrategyResult> results) {
        this.results = results;
    }

    public List<StrategyResult> getResults() { return results; }
    public void setResults(List<StrategyResult> results) { this.results = results; }
}