package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Hud {
    private BitmapFont font;

    public Hud(BitmapFont font) {
        this.font = font;
    }

    public void drawButtons(ShapeRenderer shape, float rx, float ry, float ex, float ey, float w, float h) {
        shape.setColor(0.2f, 0.2f, 0.3f, 0.9f);
        shape.rect(rx, ry, w, h);
        shape.rect(ex, ey, w, h);
        shape.setColor(0.4f, 0.4f, 0.5f, 0.9f);
        shape.rect(rx + 3, ry + 3, w - 6, h - 6);
        shape.rect(ex + 3, ey + 3, w - 6, h - 6);        shape.rect(ex + 3, ey + 3, w - 6, h - 6
    }
}
