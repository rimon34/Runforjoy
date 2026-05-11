package com.rimon.runforjoy;

public enum PowerUpType {
    SHIELD("🛡️", 0.15f),
    DOUBLE_SCORE("2x", 0.10f),
    SLOW_MO("🐌", 0.08f);

    public final String symbol;
    public final float spawnChance;

    PowerUpType(String symbol, float spawnChance) {
        this.symbol = symbol;
        this.spawnChance = spawnChance;
    }
}
