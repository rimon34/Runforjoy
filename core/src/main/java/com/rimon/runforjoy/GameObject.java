package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

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
        if (isPowerUp || isGolden) pulseTimer += delta * 5;
    }

    public boolean isOffScreen() {
        return y < -100;
    }

    public boolean collidesWith(Player p) {
        return Math.abs(x - p.x) < Constants.CATCH_X_MARGIN &&
            y < Constants.CATCH_Y_TOP &&
            y > Constants.CATCH_Y_BOTTOM;
    }

    public long getPointValue() {
        if (isPowerUp) return 0;

        long points;
        if (operation == MathOperation.SUBTRACT) {
            // Subtract: card says "-N", player loses N points
            points = -value;
        } else {
            // ADD: +value, MULTIPLY: value * pointMultiplier, DIVIDE: value * pointMultiplier
            points = (long) value * operation.pointMultiplier;
        }

        if (isGolden) points *= 5;
        if (RunForJoy.isDoubleScoreActive) points = (long) (points * 1.5);
        return points;
    }

    public void drawShape(ShapeRenderer shape) {
        float pulse = (float)(Math.sin(pulseTimer) * 0.1f + 0.9f);
        float width = 70 * pulse;
        float height = 60 * pulse;

        if (isPowerUp) {
            shape.setColor(0.7f, 0.3f, 0.9f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(0.9f, 0.5f, 1f, 1);
            shape.rect(x - width/2 + 4, y + 4, width - 8, height - 8);
            shape.setColor(1, 1, 1, 1);
            shape.circle(x, y + height/2, 18 * pulse);
        } else if (isGolden) {
            shape.setColor(1f, 0.85f, 0f, 1);
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1f, 0.95f, 0.3f, 1);
            shape.rect(x - width/2 + 3, y + 3, width - 6, height - 6);
        } else {
            switch (operation) {
                case ADD:
                    shape.setColor(0.1f, 0.8f, 0.2f, 1);
                    break;
                case SUBTRACT:
                    shape.setColor(0.9f, 0.2f, 0.2f, 1);
                    break;
                case MULTIPLY:
                    shape.setColor(0.7f, 0.3f, 0.9f, 1);
                    break;
                case DIVIDE:
                    shape.setColor(0.2f, 0.6f, 0.9f, 1);
                    break;
            }
            shape.rect(x - width/2, y, width, height);
            shape.setColor(1, 1, 1, 0.2f);
            shape.rect(x - width/2 + 2, y + 2, width - 4, 8);
        }
    }

    public void drawLabel(SpriteBatch batch, BitmapFont font) {
        if (isPowerUp) {
            font.setColor(1, 1, 1, 1);
            font.getData().setScale(2.2f);
            layout.setText(font, powerUpType.symbol);
            font.draw(batch, powerUpType.symbol, x - layout.width/2, y + 45);
            font.getData().setScale(1.6f);
            return;
        }

        String text = operation.symbol + value;
        if (operation == MathOperation.ADD && value > 0) text = "+" + value;

        font.getData().setScale(1.8f);

        for (int offset = -2; offset <= 2; offset++) {
            for (int offset2 = -2; offset2 <= 2; offset2++) {
                if (offset == 0 && offset2 == 0) continue;
                font.setColor(0, 0, 0, 0.8f);
                font.draw(batch, text, x - 25 + offset, y + 45 + offset2);
            }
        }

        font.setColor(1, 1, 1, 1);
        font.draw(batch, text, x - 25, y + 45);
        font.getData().setScale(1.6f);
    }
}
