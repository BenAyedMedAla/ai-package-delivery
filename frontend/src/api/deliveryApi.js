// api/deliveryApi.js - API service for React frontend

const API_BASE_URL = "http://localhost:8080/api/delivery";

class DeliveryAPI {
  /**
   * Generate a new random grid
   */
  static async generateGrid(
    customers,
    stores,
    gridWidth = 10,
    gridHeight = 10
  ) {
    try {
      const response = await fetch(`${API_BASE_URL}/generate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          customers,
          stores,
          gridWidth,
          gridHeight,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to generate grid");
      }

      return await response.json();
    } catch (error) {
      console.error("Error generating grid:", error);
      throw error;
    }
  }

  /**
   * Plan delivery routes for all customers
   */
  static async planRoutes(
    initialState,
    traffic,
    strategy = "AS1",
    visualize = false
  ) {
    try {
      const response = await fetch(`${API_BASE_URL}/plan`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          initialState,
          traffic,
          strategy,
          visualize,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to plan routes");
      }

      return await response.json();
    } catch (error) {
      console.error("Error planning routes:", error);
      throw error;
    }
  }

  /**
   * Get available search strategies
   */
  static async getStrategies() {
    try {
      const response = await fetch(`${API_BASE_URL}/strategies`);

      if (!response.ok) {
        throw new Error("Failed to fetch strategies");
      }

      return await response.json();
    } catch (error) {
      console.error("Error fetching strategies:", error);
      throw error;
    }
  }

  /**
   * Find a single path between two locations
   */
  static async findPath(initialState, traffic, start, goal, strategy = "AS1") {
    try {
      const response = await fetch(`${API_BASE_URL}/path`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          initialState,
          traffic,
          start,
          goal,
          strategy,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to find path");
      }

      return await response.json();
    } catch (error) {
      console.error("Error finding path:", error);
      throw error;
    }
  }
}

export default DeliveryAPI;

// // ============================================
// // Example React Component Usage
// // ============================================

// // DeliveryDashboard.jsx
// import React, { useState, useEffect } from 'react';
// import DeliveryAPI from './api/deliveryApi';

// function DeliveryDashboard() {
//   const [numCustomers, setNumCustomers] = useState(5);
//   const [numTrucks, setNumTrucks] = useState(2);
//   const [gridData, setGridData] = useState(null);
//   const [routes, setRoutes] = useState(null);
//   const [loading, setLoading] = useState(false);
//   const [strategy, setStrategy] = useState('AS1');
//   const [strategies, setStrategies] = useState([]);

//   // Load available strategies on mount
//   useEffect(() => {
//     DeliveryAPI.getStrategies().then(data => {
//       setStrategies(data.strategies);
//     });
//   }, []);

//   const handleGenerateGrid = async () => {
//     setLoading(true);
//     try {
//       const data = await DeliveryAPI.generateGrid(numCustomers, numTrucks);
//       setGridData(data);
//       setRoutes(null); // Clear previous routes
//       console.log('Grid generated:', data);
//     } catch (error) {
//       alert('Error generating grid: ' + error.message);
//     } finally {
//       setLoading(false);
//     }
//   };

//   const handlePlanRoutes = async () => {
//     if (!gridData) {
//       alert('Please generate a grid first');
//       return;
//     }

//     setLoading(true);
//     try {
//       const data = await DeliveryAPI.planRoutes(
//         gridData.initialState,
//         gridData.traffic,
//         strategy,
//         false
//       );
//       setRoutes(data);
//       console.log('Routes planned:', data);
//     } catch (error) {
//       alert('Error planning routes: ' + error.message);
//     } finally {
//       setLoading(false);
//     }
//   };

//   const handleReset = () => {
//     setGridData(null);
//     setRoutes(null);
//     setNumCustomers(5);
//     setNumTrucks(2);
//   };

//   return (
//     <div className="p-6">
//       <h1 className="text-3xl font-bold mb-6">Delivery Route Planner</h1>

//       {/* Configuration Panel */}
//       <div className="bg-white p-4 rounded shadow mb-4">
//         <h2 className="text-xl font-semibold mb-4">Configuration</h2>

//         <div className="grid grid-cols-2 gap-4 mb-4">
//           <div>
//             <label className="block mb-2">Number of Customers:</label>
//             <input
//               type="number"
//               min="1"
//               max="10"
//               value={numCustomers}
//               onChange={(e) => setNumCustomers(parseInt(e.target.value))}
//               className="border p-2 rounded w-full"
//             />
//           </div>

//           <div>
//             <label className="block mb-2">Number of Trucks:</label>
//             <input
//               type="number"
//               min="1"
//               max="3"
//               value={numTrucks}
//               onChange={(e) => setNumTrucks(parseInt(e.target.value))}
//               className="border p-2 rounded w-full"
//             />
//           </div>
//         </div>

//         <div className="mb-4">
//           <label className="block mb-2">Search Strategy:</label>
//           <select
//             value={strategy}
//             onChange={(e) => setStrategy(e.target.value)}
//             className="border p-2 rounded w-full"
//           >
//             {strategies.map(s => (
//               <option key={s.code} value={s.code}>{s.name}</option>
//             ))}
//           </select>
//         </div>

//         <div className="flex gap-2">
//           <button
//             onClick={handleGenerateGrid}
//             disabled={loading}
//             className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:bg-gray-400"
//           >
//             {loading ? 'Generating...' : 'Generate New Grid'}
//           </button>

//           <button
//             onClick={handlePlanRoutes}
//             disabled={loading || !gridData}
//             className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 disabled:bg-gray-400"
//           >
//             {loading ? 'Planning...' : 'Plan Routes'}
//           </button>

//           <button
//             onClick={handleReset}
//             className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
//           >
//             Reset
//           </button>
//         </div>
//       </div>

//       {/* Grid Information */}
//       {gridData && (
//         <div className="bg-white p-4 rounded shadow mb-4">
//           <h2 className="text-xl font-semibold mb-2">Grid Information</h2>
//           <p>Grid Size: {gridData.gridWidth} x {gridData.gridHeight}</p>
//           <p>Customers: {gridData.numCustomers}</p>
//           <p>Stores: {gridData.numStores}</p>
//           <p>Tunnels: {gridData.tunnels?.length || 0}</p>
//         </div>
//       )}

//       {/* Routes Display */}
//       {routes && (
//         <div className="bg-white p-4 rounded shadow">
//           <h2 className="text-xl font-semibold mb-4">Planned Routes</h2>

//           <div className="mb-4">
//             <p className="font-semibold">Total Cost: {routes.totalCost}</p>
//             <p className="font-semibold">Total Routes: {routes.totalRoutes}</p>
//             <p className="font-semibold">Nodes Expanded: {routes.totalNodesExpanded}</p>
//           </div>

//           {routes.routes?.map((route, idx) => (
//             <div key={idx} className="border-l-4 border-blue-500 pl-4 mb-3">
//               <p className="font-semibold">
//                 {route.truck} → {route.customer}
//               </p>
//               <p className="text-sm text-gray-600">
//                 Cost: {route.cost} | Nodes: {route.nodesExpanded}
//               </p>
//               <p className="text-sm">
//                 Path: {route.actions.join(' → ')}
//               </p>
//             </div>
//           ))}
//         </div>
//       )}
//     </div>
//   );
// }

// export default DeliveryDashboard;
