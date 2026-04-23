package com.rimon.runforjoy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class Runforjoy extends ApplicationAdapter {
    private GameScreen gameScreen;

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
}
