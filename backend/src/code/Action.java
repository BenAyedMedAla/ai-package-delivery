package com.aipackagedelivery.code;

public enum Action {
    UP, DOWN, LEFT, RIGHT, TUNNEL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
