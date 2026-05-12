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

    private float pulseTimer = 0;
    private float wobbleOffset = 0;
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
        wobbleOffset += delta * 8;
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

    /**
     * Returns the plain signed point delta for ADD and SUBTRACT only.
     * MULTIPLY and DIVIDE are handled separately in GameScreen (they act on total score).
     */
    public long getPointValue() {
        if (isPowerUp) return 0;
        long points;
        if (operation == MathOperation.SUBTRACT) {
            points = -value;
        } else if (operation == MathOperation.ADD) {
            points = value;
        } else {
            // MULTIPLY / DIVIDE — GameScreen handles these directly; return 0 here
            return 0;
        }
        if (isGolden) points *= 5;
        if (RunForJoy.isDoubleScoreActive) points = (long)(points * 1.5);
        return points;
    }

    public void drawShape(ShapeRenderer shape) {
        float pulse = (float)(Math.sin(pulseTimer) * 0.08f + 0.92f);
        float width  = 72 * pulse;
        float height = 62 * pulse;

        if (isPowerUp) {
            // Gold — unmistakably different from regular cards
            shape.setColor(1f, 0.75f, 0f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1f, 0.92f, 0.3f, 1);
            shape.rect(x - width/2 + 4, y + 4, width - 8, height - 8);
            shape.setColor(1f, 1f, 0.7f, 1);
            shape.circle(x, y + height / 2f, 14 * pulse);

        } else if (isGolden) {
            shape.setColor(0.95f, 0.80f, 0f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1f, 0.95f, 0.4f, 1);
            shape.rect(x - width/2 + 3, y + 3, width - 6, height - 6);

        } else {
            // ALL regular cards — same deep indigo
            shape.setColor(0.38f, 0.30f, 0.72f, 1f);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(0.22f, 0.16f, 0.56f, 1f);
            shape.rect(x - width/2 + 4, y + 4, width - 8, height - 8);
            // Glossy sheen
            shape.setColor(0.55f, 0.48f, 0.90f, 0.45f);
            shape.rect(x - width/2 + 4, y + height - 14, width - 8, 10);
        }
    }

    public void drawLabel(SpriteBatch batch, BitmapFont font) {
        if (isPowerUp) {
            font.getData().setScale(2.4f);
            layout.setText(font, powerUpType.symbol);
            font.setColor(0, 0, 0, 1);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    font.draw(batch, powerUpType.symbol, x - layout.width / 2f + dx, y + 42 + dy);
                }
            }
            font.setColor(1f, 1f, 0.5f, 1f);
            font.draw(batch, powerUpType.symbol, x - layout.width / 2f, y + 42);
            font.getData().setScale(2.0f);
            return;
        }

        // Correct readable symbols — no * or /
        String text;
        switch (operation) {
            case ADD:      text = "+" + value;  break;
            case SUBTRACT: text = "-" + value;  break;
            case MULTIPLY: text = "x" + value;  break;  // lowercase x, renders cleanly in LibGDX BitmapFont
            case DIVIDE:   text = "/" + value;  break;  // kept as / since BitmapFont cant render unicode ÷ reliably
            default:       text = operation.symbol + value; break;
        }

        font.getData().setScale(2.4f);
        layout.setText(font, text);
        float tx = x - layout.width / 2f;
        float ty = y + 44;

        // Thick black outline
        font.setColor(0f, 0f, 0f, 1f);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, text, tx + dx, ty + dy);
            }
        }

        // Bright white on top
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, text, tx, ty);
        font.getData().setScale(2.0f);
    }
}
