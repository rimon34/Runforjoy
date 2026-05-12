package com.rimon.runforjoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

//Renders the user interface, specifically drawing the score, lives, and the white navigation buttons.
public class Hud {
    private BitmapFont font;

    public Hud(BitmapFont font) {
        this.font = font;
    }

    public void drawButtons(ShapeRenderer shape) {
        shape.setColor(0.2f, 0.3f, 0.6f, 1);
        shape.rect(30, 10, 200, 70);
        shape.rect(370, 10, 200, 70);
    }

    public void drawText(SpriteBatch batch, int score, int lives) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        font.setColor(Color.RED);

        float xPos = screenWidth - 250;
        float yPos = screenHeight - 50;

        font.draw(batch, "Score: " + score, xPos, yPos);
        font.draw(batch, "Lives: " + lives, xPos, yPos - 50);

        font.setColor(Color.WHITE);
        font.draw(batch, "LEFT", 90, 55);
        font.draw(batch, "RIGHT", 410, 55);
    }
}
