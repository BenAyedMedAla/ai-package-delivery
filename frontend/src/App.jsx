import { useState } from "react";
import Controls from "./components/Controls";
import GridVisualization from "./components/GridVisualization";
import Metrics from "./components/Metrics";
import { generateGrid, chooseStrategy } from "./services/api";
import "./App.css";

function App() {
  const [gridData, setGridData] = useState(null);
  const [strategyResult, setStrategyResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGenerateGrid = async (gridRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await generateGrid(gridRequest);
      setGridData(data);
      setStrategyResult(null); // Reset strategy results when new grid is generated
    } catch (err) {
      setError("Failed to generate grid: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChooseStrategy = async (strategyRequest) => {
    if (!gridData) {
      setError("Please generate a grid first");
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const result = await chooseStrategy(strategyRequest);
      if (result.steps) {
        // Single strategy with steps
        setStrategyResult({ steps: result.steps, metrics: result.metrics });
        setGridData((prev) => ({ ...prev, steps: result.steps }));
      } else if (result.results) {
        // All strategies - backend returns { results: [...] }
        setStrategyResult({ allStrategies: result.results });
      }
    } catch (err) {
      setError("Failed to run strategy: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAnimationComplete = () => {
    console.log("Animation completed");
  };

  return (
    <div className="app">
      <header>
        <h1>AI Package Delivery Visualization</h1>
      </header>

      <div className="main-content">
        <div className="controls-panel">
          <Controls
            onGenerateGrid={handleGenerateGrid}
            onChooseStrategy={handleChooseStrategy}
            isLoading={isLoading}
          />
        </div>

        <div className="grid-panel">
          <GridVisualization
            gridData={gridData}
            onAnimationComplete={handleAnimationComplete}
          />
        </div>

        <div className="results-panel">
          <Metrics
            metrics={strategyResult?.metrics}
            steps={strategyResult?.steps}
          />
          {strategyResult?.allStrategies && (
            <div className="all-strategies">
              <h2>Strategy Comparison</h2>
              <div className="comparison-table-wrapper">
                <table className="comparison-table">
                  <thead>
                    <tr>
                      <th>Strategy</th>
                      <th>Time (ms)</th>
                      <th>Nodes</th>
                      <th>Cost</th>
                    </tr>
                  </thead>
                  <tbody>
                    {strategyResult.allStrategies.map((strat, index) => (
                      <tr key={index}>
                        <td className="strategy-name">{strat.strategy}</td>
                        <td>{strat.timeMs}</td>
                        <td>{strat.nodesExpanded}</td>
                        <td className="cost-cell">{strat.totalCost}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>

      {error && <div className="error">{error}</div>}
      {isLoading && <div className="loading">Loading...</div>}
    </div>
  );
}

export default App;
