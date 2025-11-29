package tests;

import code.*;
import java.util.List;

public class PlaceholderTest {

    public static void main(String[] args) {
        testGenGrid();
        testPathFinding();
        testSolve();
        System.out.println("All tests passed!");
    }

    public static void testGenGrid() {
        String grid = DeliverySearch.GenGrid();
        if (grid == null || grid.length() == 0) {
            throw new AssertionError("Grid is null or empty");
        }
        // Basic check: starts with m n
        String[] lines = grid.split("\n");
        String[] mn = lines[0].split(" ");
        int m = Integer.parseInt(mn[0]);
        int n = Integer.parseInt(mn[1]);
        if (m <= 0 || n <= 0) {
            throw new AssertionError("Invalid m or n");
        }
    }

    public static void testPathFinding() {
        // Simple 2x2 grid
        int m = 2, n = 2;
        int[][] traffic = {{1,1},{1,1}};
        List<DeliverySearch.Tunnel> tunnels = List.of();
        List<State> stores = List.of(new State(0,0));
        List<State> customers = List.of(new State(1,1));
        List<State> trucks = List.of(new State(0,0));

        DeliverySearch ds = new DeliverySearch(m, n, traffic, tunnels, stores, customers, trucks);
        GenericSearch.SearchResult<State, Action> result = ds.path(new State(0,0), new State(1,1), Strategy.BF);
        if (result == null || result.cost <= 0 || result.actions.size() == 0) {
            throw new AssertionError("Invalid path result");
        }
    }

    public static void testSolve() {
        String grid = DeliverySearch.GenGrid();
        String result = DeliverySearch.solve(grid, "", Strategy.BF, false);
        if (result == null || !result.contains("Total cost")) {
            throw new AssertionError("Invalid solve result");
        }
    }
}
