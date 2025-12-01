package tests;

import code.*;

public class PlaceholderTest {

    public static void main(String[] args) {
        testInitialStateFormat();
        testTrafficFormat();
        testSolveSingle();
        System.out.println("All tests passed!");
    }

    public static void testInitialStateFormat() {
        String initialState = DeliverySearch.GenGrid();
        if (initialState == null || initialState.isEmpty())
            throw new AssertionError("initialState empty");

        String[] parts = initialState.split(";");
        if (parts.length < 5)
            throw new AssertionError("initialState wrong format");
    }

    public static void testTrafficFormat() {
        String traffic = DeliverySearch.GenTraffic();
        if (traffic == null || traffic.isEmpty())
            throw new AssertionError("traffic empty");

        for (String e : traffic.split(";")) {
            String[] p = e.split(",");
            if (p.length != 5)
                throw new AssertionError("traffic edge invalid: " + e);
        }
    }

    public static void testSolveSingle() {
        String init = "5;5;1;1;4,4;";
        String traffic = DeliverySearch.GenTraffic();

        String result = DeliverySearch.solve(init, traffic, "BF", false);

        if (result == null || result.isEmpty())
            throw new AssertionError("Solve returned null or empty");

        if (!result.contains("(Truck") || !result.contains("Customer"))
            throw new AssertionError("Solve missing pair");

        String[] lines = result.split("\n");
        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length < 4)
                throw new AssertionError("Solve output malformed: " + line);
        }
    }
}
