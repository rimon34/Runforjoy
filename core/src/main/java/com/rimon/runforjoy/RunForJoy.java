package com.rimon.runforjoy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class RunForJoy extends ApplicationAdapter {
    private GameScreen gameScreen;

    public static int currentLevel = 1;
    public static boolean hasShield = false;
    public static boolean isDoubleScoreActive = false;
    public static boolean isSlowMoActive = false;
    public static boolean isInvincible = false;
    public static float doubleScoreTimer = 0;
    public static float slowMoTimer = 0;

    @Override
    public void create() {
        gameScreen = new GameScreen();
    }

    @Override
    public void render() {
        gameScreen.update(Gdx.graphics.getDeltaTime());
        gameScreen.render();
    }

    @Override
    public void dispose() {
        gameScreen.dispose();
    }

    public static void updatePowerUps(float delta) {
        if (doubleScoreTimer > 0) {
            doubleScoreTimer -= delta;
            if (doubleScoreTimer <= 0) isDoubleScoreActive = false;
        }
        if (slowMoTimer > 0) {
            slowMoTimer -= delta;
            if (slowMoTimer <= 0) isSlowMoActive = false;
        }
    }
}
