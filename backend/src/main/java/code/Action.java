package code;

/**
 * Action enumeration for the delivery problem.
 * Represents possible movements a truck can make.
 */
public enum Action {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right"),
    TUNNEL("tunnel");

    private final String displayName;

    Action(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parse action from string representation.
     */
    public static Action fromString(String s) {
        switch (s.toLowerCase()) {
            case "up": return UP;
            case "down": return DOWN;
            case "left": return LEFT;
            case "right": return RIGHT;
            case "tunnel": return TUNNEL;
            default: throw new IllegalArgumentException("Unknown action: " + s);
        }
    }
}