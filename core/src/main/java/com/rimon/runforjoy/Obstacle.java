package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Obstacle {
    public float x, y;
    private float pulseTimer = 0;
    private ObstacleType type;
    private float eyeGlow = 0;

    public Obstacle(float x, float y, ObstacleType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update(float delta) {
        float speed = RunForJoy.isSlowMoActive ? Constants.OBJECT_SPEED * 0.5f : Constants.OBJECT_SPEED;
        y -= speed * delta;
        pulseTimer += delta * 8;
        if (type == ObstacleType.EVIL) {
            eyeGlow += delta * 10;
        }
    }

    public boolean isOffScreen() {
        return y < -50;
    }

    public boolean collidesWith(Player player) {
        if (RunForJoy.isInvincible) return false;
        return Math.abs(player.x - x) < 40 && Math.abs(player.y - y) < 40;
    }

    public ObstacleType getType() {
        return type;
    }

    public void drawShape(ShapeRenderer shape) {
        float pulse = (float)(Math.sin(pulseTimer) * 0.1f + 0.9f);

        if (type == ObstacleType.EVIL) {
            float glow = (float)(Math.sin(eyeGlow) * 0.3f + 0.7f);
            shape.setColor(0.3f, 0.3f, 0.4f, 1);
            shape.circle(x, y, 28 * pulse);
            shape.setColor(0.5f, 0.5f, 0.6f, 1);
            shape.circle(x, y, 25 * pulse);
            shape.setColor(0.9f, 0.2f, 0.2f, 1);
            shape.circle(x, y, 23 * pulse);
            shape.setColor(1, 0.1f, 0.1f, 1);
            shape.circle(x - 10, y + 5, 6 * pulse * glow);
            shape.circle(x + 10, y + 5, 6 * pulse * glow);
            shape.setColor(0, 0, 0, 1);
            shape.circle(x - 10, y + 5, 2.5f * pulse);
            shape.circle(x + 10, y + 5, 2.5f * pulse);
        } else {
            shape.setColor(0.4f, 0.4f, 0.45f, 1);
            shape.circle(x, y, 26 * pulse);
            shape.setColor(0.6f, 0.6f, 0.65f, 1);
            shape.circle(x, y, 22 * pulse);
            shape.setColor(0.8f, 0.8f, 0.85f, 1);
            shape.circle(x, y, 18 * pulse);
        }
    }
}
