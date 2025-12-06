package code;

import java.util.List;
import java.util.Map;

public class StrategyExecutionResult {
    private List<String> steps;
    private Map<String, Object> metrics;
    private List<String> warnings;

    public StrategyExecutionResult() {}

    public StrategyExecutionResult(List<String> steps, Map<String, Object> metrics, List<String> warnings) {
        this.steps = steps;
        this.metrics = metrics;
        this.warnings = warnings;
    }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}