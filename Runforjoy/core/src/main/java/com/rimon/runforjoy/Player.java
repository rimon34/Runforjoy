package com.rimon.runforjoy;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Player {
    float x, y;
    public Player(float x, float y) { this.x = x; this.y = y; }
    public void moveTo(float laneX) { this.x = laneX; }

    public void draw(ShapeRenderer shape) {
        // Body Glow
        shape.setColor(0.2f, 0.7f, 1f, 0.3f);
        shape.rect(x - 25, y - 5, 50, 90);

        // Main Body
        shape.setColor(0.2f, 0.7f, 1f, 1);
        shape.rect(x - 20, y, 40, 80);

        // Visor
        shape.setColor(1, 1, 0, 1);
        shape.rect(x - 15, y + 60, 30, 10);

        // Fancy Thruster Flicker
        shape.setColor(1, 0.5f, 0, 1);
        float flicker = MathUtils.random(5, 15);
        shape.rect(x - 15, y - flicker, 10, flicker);
        shape.rect(x + 5, y - flicker, 10, flicker);
    }
}
