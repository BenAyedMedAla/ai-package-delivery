import { useEffect, useRef, useState } from 'react';

const GridVisualization = ({ gridData, onAnimationComplete }) => {
  const svgRef = useRef(null);
  const [truckPositions, setTruckPositions] = useState([]);
  const [currentStep, setCurrentStep] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);

  const cellSize = 50;
  const margin = 20;

  useEffect(() => {
    if (gridData && gridData.steps) {
      animateTrucks(gridData.steps);
    }
  }, [gridData]);

  const parseTraffic = (trafficStr) => {
    if (!trafficStr) return [];
    const edges = [];
    const parts = trafficStr.split(';');
    parts.forEach(part => {
      if (part.trim()) {
        const [x1, y1, x2, y2, cost] = part.split(',').map(Number);
        edges.push({ from: { x: x1, y: y1 }, to: { x: x2, y: y2 }, cost });
      }
    });
    return edges;
  };

  const animateTrucks = (steps) => {
    setIsAnimating(true);
    setCurrentStep(0);
    const initialPositions = gridData.stores.map(store => ({ ...store, truckId: gridData.stores.indexOf(store) }));
    setTruckPositions(initialPositions);

    let stepIndex = 0;
    const interval = setInterval(() => {
      if (stepIndex < steps.length) {
        const step = steps[stepIndex];
        // Parse step to update truck positions
        if (step.includes('moved to')) {
          const match = step.match(/Truck (\d+) moved to \((\d+),(\d+)\)/);
          if (match) {
            const truckId = parseInt(match[1]);
            const x = parseInt(match[2]);
            const y = parseInt(match[3]);
            setTruckPositions(prev => prev.map(truck =>
              truck.truckId === truckId ? { ...truck, x, y } : truck
            ));
          }
        }
        setCurrentStep(stepIndex);
        stepIndex++;
      } else {
        clearInterval(interval);
        setIsAnimating(false);
        if (onAnimationComplete) onAnimationComplete();
      }
    }, 1000); // 1 second per step
  };

  if (!gridData) {
    return <div className="grid-placeholder">Generate a grid to start</div>;
  }

  const { rows, columns, grid, stores, customers, tunnels, traffic } = gridData;
  const edges = parseTraffic(traffic);

  const width = columns * cellSize + 2 * margin;
  const height = rows * cellSize + 2 * margin;

  return (
    <div className="grid-visualization">
      <svg ref={svgRef} width={width} height={height} className="grid-svg">
        {/* Grid background */}
        <rect x={margin} y={margin} width={columns * cellSize} height={rows * cellSize} fill="#f0f0f0" stroke="#ccc" />

        {/* Grid lines */}
        {Array.from({ length: rows + 1 }, (_, i) => (
          <line
            key={`h-${i}`}
            x1={margin}
            y1={margin + i * cellSize}
            x2={margin + columns * cellSize}
            y2={margin + i * cellSize}
            stroke="#ddd"
          />
        ))}
        {Array.from({ length: columns + 1 }, (_, i) => (
          <line
            key={`v-${i}`}
            x1={margin + i * cellSize}
            y1={margin}
            x2={margin + i * cellSize}
            y2={margin + rows * cellSize}
            stroke="#ddd"
          />
        ))}

        {/* Traffic edges */}
        {edges.map((edge, index) => {
          const x1 = margin + edge.from.y * cellSize + cellSize / 2;
          const y1 = margin + edge.from.x * cellSize + cellSize / 2;
          const x2 = margin + edge.to.y * cellSize + cellSize / 2;
          const y2 = margin + edge.to.x * cellSize + cellSize / 2;
          const color = edge.cost === 0 ? '#000' : `hsl(${120 - edge.cost * 20}, 70%, 50%)`;
          const strokeWidth = Math.max(1, edge.cost / 2);

          return (
            <line
              key={`edge-${index}`}
              x1={x1}
              y1={y1}
              x2={x2}
              y2={y2}
              stroke={color}
              strokeWidth={strokeWidth}
            />
          );
        })}

        {/* Tunnels */}
        {tunnels.map((tunnel, index) => {
          const x1 = margin + tunnel.from.y * cellSize + cellSize / 2;
          const y1 = margin + tunnel.from.x * cellSize + cellSize / 2;
          const x2 = margin + tunnel.to.y * cellSize + cellSize / 2;
          const y2 = margin + tunnel.to.x * cellSize + cellSize / 2;

          return (
            <g key={`tunnel-${index}`}>
              <line
                x1={x1}
                y1={y1}
                x2={x2}
                y2={y2}
                stroke="#ff00ff"
                strokeWidth="3"
                strokeDasharray="5,5"
              />
              <circle cx={x1} cy={y1} r="5" fill="#ff00ff" />
              <circle cx={x2} cy={y2} r="5" fill="#ff00ff" />
            </g>
          );
        })}

        {/* Stores */}
        {stores.map((store, index) => (
          <g key={`store-${index}`}>
            <rect
              x={margin + store.y * cellSize + 5}
              y={margin + store.x * cellSize + 5}
              width={cellSize - 10}
              height={cellSize - 10}
              fill="#4CAF50"
              rx="5"
            />
            <text
              x={margin + store.y * cellSize + cellSize / 2}
              y={margin + store.x * cellSize + cellSize / 2 + 5}
              textAnchor="middle"
              fill="white"
              fontSize="12"
            >
              S{index}
            </text>
          </g>
        ))}

        {/* Customers */}
        {customers.map((customer, index) => (
          <g key={`customer-${index}`}>
            <circle
              cx={margin + customer.y * cellSize + cellSize / 2}
              cy={margin + customer.x * cellSize + cellSize / 2}
              r="15"
              fill="#2196F3"
            />
            <text
              x={margin + customer.y * cellSize + cellSize / 2}
              y={margin + customer.x * cellSize + cellSize / 2 + 5}
              textAnchor="middle"
              fill="white"
              fontSize="12"
            >
              C{index}
            </text>
          </g>
        ))}

        {/* Trucks */}
        {truckPositions.map((truck, index) => (
          <g key={`truck-${index}`}>
            <circle
              cx={margin + truck.y * cellSize + cellSize / 2}
              cy={margin + truck.x * cellSize + cellSize / 2}
              r="12"
              fill="#FF5722"
              stroke="#fff"
              strokeWidth="2"
            />
            <text
              x={margin + truck.y * cellSize + cellSize / 2}
              y={margin + truck.x * cellSize + cellSize / 2 + 5}
              textAnchor="middle"
              fill="white"
              fontSize="10"
            >
              T{index}
            </text>
          </g>
        ))}
      </svg>

      {isAnimating && (
        <div className="animation-controls">
          <p>Step {currentStep + 1} of {gridData.steps.length}</p>
          <p>{gridData.steps[currentStep]}</p>
        </div>
      )}
    </div>
  );
};

export default GridVisualization;