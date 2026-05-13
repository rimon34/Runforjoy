package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameObject {
    public float x, y;
    public MathOperation operation;
    public int value;
    public boolean isPowerUp = false;
    public PowerUpType powerUpType = null;
    public boolean isGolden = false;
    public int rowId = -1; // links paired objects in the same spawned row

    private float pulseTimer = 0;
    private GlyphLayout layout = new GlyphLayout();

    public GameObject(float x, float y, MathOperation operation, int value) {
        this.x = x;
        this.y = y;
        this.operation = operation;
        this.value = value;
    }

    public GameObject(float x, float y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.operation = MathOperation.ADD;
        this.value = 0;
        this.isPowerUp = true;
        this.powerUpType = type;
    }

    public void update(float delta) {
        float speed = RunForJoy.isSlowMoActive ? Constants.OBJECT_SPEED * 0.5f : Constants.OBJECT_SPEED;
        y -= speed * delta;
        pulseTimer += delta * 5;
    }

    public boolean isOffScreen() {
        return y < -100;
    }

    public boolean collidesWith(Player p) {
        return Math.abs(x - p.x) < Constants.CATCH_X_MARGIN &&
            y < Constants.CATCH_Y_TOP &&
            y > Constants.CATCH_Y_BOTTOM;
    }

    /** Only ADD and SUBTRACT — MULTIPLY and DIVIDE removed from game */
    public long getPointValue() {
        if (isPowerUp) return 0;
        long points = (operation == MathOperation.SUBTRACT) ? -value : value;
        if (isGolden) points *= 5;
        if (RunForJoy.isDoubleScoreActive) points = (long)(points * 1.5);
        return points;
    }

    public void drawShape(ShapeRenderer shape) {
        float pulse = (float)(Math.sin(pulseTimer) * 0.08f + 0.92f);
        float width  = 72 * pulse;
        float height = 62 * pulse;

        if (isPowerUp) {
            // Gold card — unmistakably different
            shape.setColor(1f, 0.75f, 0f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1f, 0.92f, 0.3f, 1);
            shape.rect(x - width/2 + 4, y + 4, width - 8, height - 8);
            // Pulsing inner glow
            shape.setColor(1f, 1f, 0.7f, 0.6f);
            shape.circle(x, y + height / 2f, 12 * pulse);

        } else if (isGolden) {
            shape.setColor(0.95f, 0.80f, 0f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1f, 0.95f, 0.4f, 1);
            shape.rect(x - width/2 + 3, y + 3, width - 6, height - 6);

        } else {
            // All regular cards — deep indigo, player reads the number
            shape.setColor(0.38f, 0.30f, 0.72f, 1f);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(0.22f, 0.16f, 0.56f, 1f);
            shape.rect(x - width/2 + 4, y + 4, width - 8, height - 8);
            // Glossy sheen on top edge
            shape.setColor(0.55f, 0.48f, 0.90f, 0.45f);
            shape.rect(x - width/2 + 4, y + height - 14, width - 8, 10);
        }
    }

    public void drawLabel(SpriteBatch batch, BitmapFont font) {
        String text;

        if (isPowerUp) {
            // Clean ASCII labels — no emoji, no unicode, no dots
            switch (powerUpType) {
                case SHIELD:       text = "SHLD"; break;
                case DOUBLE_SCORE: text = "x2";   break;
                case SLOW_MO:      text = "SLOW"; break;
                default:           text = "PWR";  break;
            }
        } else {
            text = (operation == MathOperation.SUBTRACT ? "-" : "+") + value;
        }

        font.getData().setScale(2.2f);
        layout.setText(font, text);
        float tx = x - layout.width / 2f;
        float ty = y + 44;

        // Thick black outline — readable at speed
        font.setColor(0f, 0f, 0f, 1f);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, text, tx + dx, ty + dy);
            }
        }

        // Power up: gold text. Regular card: white text
        if (isPowerUp) {
            font.setColor(1f, 1f, 0.3f, 1f);
        } else {
            font.setColor(1f, 1f, 1f, 1f);
        }
        font.draw(batch, text, tx, ty);
        font.getData().setScale(2.0f);
    }
}
