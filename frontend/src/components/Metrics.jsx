const Metrics = ({ metrics, steps }) => {
  if (!metrics) return null;

  return (
    <div className="metrics">
      <h2>Strategy Results</h2>
      <div className="metrics-grid">
        <div className="metric">
          <span className="label">Time (ms):</span>
          <span className="value">{metrics.timeMs}</span>
        </div>
        <div className="metric">
          <span className="label">Deliveries:</span>
          <span className="value">{metrics.deliveries}</span>
        </div>
        <div className="metric">
          <span className="label">Total Cost:</span>
          <span className="value">{metrics.totalCost}</span>
        </div>
        <div className="metric">
          <span className="label">Nodes Expanded:</span>
          <span className="value">{metrics.nodesExpanded}</span>
        </div>
        <div className="metric">
          <span className="label">Memory (KB):</span>
          <span className="value">{metrics.memoryKB}</span>
        </div>
        <div className="metric">
          <span className="label">CPU (ms):</span>
          <span className="value">{metrics.cpuMs}</span>
        </div>
      </div>

      {steps && steps.length > 0 && (
        <div className="steps">
          <h3>Execution Steps</h3>
          <div className="steps-list">
            {steps.map((step, index) => (
              <div key={index} className="step">
                {step}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Metrics;