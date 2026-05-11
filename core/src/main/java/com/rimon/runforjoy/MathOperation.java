package com.rimon.runforjoy;

public enum MathOperation {
    ADD("+", 1, 15, 1, false),      // +1 to +15
    SUBTRACT("-", 1, 10, -1, false), // -1 to -10
    MULTIPLY("×", 2, 5, 10, true),   // ×2 to ×5 = ×10 points
    DIVIDE("÷", 2, 10, 5, true);      // ÷2 to ÷10 = ×5 points

    public final String symbol;
    public final int minValue;
    public final int maxValue;
    public final int pointMultiplier;
    public final boolean isRare;

    MathOperation(String symbol, int minValue, int maxValue, int pointMultiplier, boolean isRare) {
        this.symbol = symbol;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.pointMultiplier = pointMultiplier;
        this.isRare = isRare;
    }
}
