package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Player {
    public float x, y;
    private float hitFlash = 0;
    private float bounceOffset = 0;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void moveTo(float newX) {
        this.x = newX;
    }

    public void update(float delta) {
        bounceOffset += delta * 12;
        if (hitFlash > 0) hitFlash -= delta;
    }

    public void hit() {
        hitFlash = 0.3f;
    }

    public void draw(ShapeRenderer shape) {
        float bounce = MathUtils.sin(bounceOffset) * 3;
        float currentY = y + bounce;

        if (hitFlash > 0) {
            shape.setColor(1, 1, 1, hitFlash);
            shape.circle(x, currentY, 28);
        }

        if (RunForJoy.hasShield) {
            shape.setColor(0.3f, 0.5f, 1f, 1);
            shape.circle(x, currentY, 32);
            shape.setColor(0.6f, 0.8f, 1f, 0.5f);
            shape.circle(x, currentY, 36);
        }

        shape.setColor(0.2f, 0.4f, 0.9f, 1);
        shape.circle(x, currentY, 25);
        shape.setColor(0.2f, 0.4f, 0.9f, 1);
        shape.circle(x, currentY + 30, 20);

        shape.setColor(1, 1, 1, 1);
        shape.circle(x - 12, currentY + 35, 6);
        shape.circle(x + 12, currentY + 35, 6);

        shape.setColor(0, 0, 0, 1);
        shape.circle(x - 12, currentY + 37, 3);
        shape.circle(x + 12, currentY + 37, 3);

        shape.setColor(0.8f, 0.4f, 0.2f, 1);
        shape.rect(x - 10, currentY + 22, 20, 5);

        shape.setColor(1, 0.2f, 0.2f, 1);
        shape.rect(x - 18, currentY + 50, 36, 8);
        shape.rect(x - 12, currentY + 42, 24, 12);
    }
}
