import { useState } from 'react';

const Controls = ({ onGenerateGrid, onChooseStrategy, isLoading }) => {
  const [gridInputs, setGridInputs] = useState({
    rows: 5,
    columns: 5,
    numCustomers: 3,
    numStores: 2
  });

  const [strategy, setStrategy] = useState('bf');

  const handleGridInputChange = (e) => {
    const { name, value } = e.target;
    setGridInputs(prev => ({
      ...prev,
      [name]: parseInt(value)
    }));
  };

  const handleGenerateGrid = () => {
    onGenerateGrid(gridInputs);
  };

  const handleChooseStrategy = () => {
    onChooseStrategy({ strategy });
  };

  const strategies = [
    { value: 'bf', label: 'Breadth First' },
    { value: 'df', label: 'Depth First' },
    { value: 'id', label: 'Iterative Deepening' },
    { value: 'uc', label: 'Uniform Cost' },
    { value: 'gr1', label: 'Greedy 1' },
    { value: 'gr2', label: 'Greedy 2' },
    { value: 'as1', label: 'A* 1' },
    { value: 'as2', label: 'A* 2' },
    { value: 'all', label: 'All Strategies' }
  ];

  return (
    <div className="controls">
      <h2>Grid Generation</h2>
      <div className="grid-inputs">
        <label>
          Rows:
          <input
            type="number"
            name="rows"
            value={gridInputs.rows}
            onChange={handleGridInputChange}
            min="3"
            max="10"
          />
        </label>
        <label>
          Columns:
          <input
            type="number"
            name="columns"
            value={gridInputs.columns}
            onChange={handleGridInputChange}
            min="3"
            max="10"
          />
        </label>
        <label>
          Customers:
          <input
            type="number"
            name="numCustomers"
            value={gridInputs.numCustomers}
            onChange={handleGridInputChange}
            min="1"
            max="10"
          />
        </label>
        <label>
          Stores:
          <input
            type="number"
            name="numStores"
            value={gridInputs.numStores}
            onChange={handleGridInputChange}
            min="1"
            max="3"
          />
        </label>
        <button onClick={handleGenerateGrid} disabled={isLoading}>
          Generate Grid
        </button>
      </div>

      <h2>Strategy Selection</h2>
      <div className="strategy-selection">
        <label>
          Strategy:
          <select value={strategy} onChange={(e) => setStrategy(e.target.value)}>
            {strategies.map(s => (
              <option key={s.value} value={s.value}>{s.label}</option>
            ))}
          </select>
        </label>
        <button onClick={handleChooseStrategy} disabled={isLoading}>
          Run Strategy
        </button>
      </div>
    </div>
  );
};

export default Controls;