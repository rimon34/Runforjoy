package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Obstacle {
    public float x, y;
    private float pulseTimer = 0;
    private ObstacleType type;
    private float eyeGlow = 0;
    private int fakeValue; // FAKER shows a fake + number
    private GlyphLayout layout = new GlyphLayout();

    public Obstacle(float x, float y, ObstacleType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        // Faker shows a believable but fake + value
        this.fakeValue = 3 + (int)(Math.random() * 8); // +3 to +10
    }

    public void update(float delta) {
        float speed = RunForJoy.isSlowMoActive
            ? Constants.OBJECT_SPEED * 0.5f
            : Constants.OBJECT_SPEED;

        // SPEEDER moves 1.7x faster — less reaction time
        if (type == ObstacleType.SPEEDER) speed *= 1.7f;

        y -= speed * delta;
        pulseTimer += delta * 8;

        if (type == ObstacleType.EVIL) {
            eyeGlow += delta * 10;
        }
    }

    public boolean isOffScreen() {
        return y < -80;
    }

    public boolean collidesWith(Player player) {
        if (RunForJoy.isInvincible) return false;
        float margin = (type == ObstacleType.FAKER) ? 35f : 40f;
        return Math.abs(player.x - x) < margin && Math.abs(player.y - y) < margin;
    }

    public ObstacleType getType() { return type; }

    public void drawShape(ShapeRenderer shape) {
        float pulse = (float)(Math.sin(pulseTimer) * 0.1f + 0.9f);

        switch (type) {
            case NORMAL:
                // Classic grey boulder
                shape.setColor(0.35f, 0.35f, 0.40f, 1);
                shape.circle(x, y, 28 * pulse);
                shape.setColor(0.55f, 0.55f, 0.60f, 1);
                shape.circle(x, y, 22 * pulse);
                shape.setColor(0.75f, 0.75f, 0.80f, 1);
                shape.circle(x, y, 16 * pulse);
                // Crack detail
                shape.setColor(0.25f, 0.25f, 0.30f, 1);
                shape.rectLine(x - 8, y + 10, x + 4, y - 8, 2);
                break;

            case EVIL:
                // Red-eyed menace
                float glow = (float)(Math.sin(eyeGlow) * 0.3f + 0.7f);
                shape.setColor(0.25f, 0.05f, 0.05f, 1);
                shape.circle(x, y, 30 * pulse);
                shape.setColor(0.6f, 0.1f, 0.1f, 1);
                shape.circle(x, y, 25 * pulse);
                shape.setColor(0.85f, 0.15f, 0.15f, 1);
                shape.circle(x, y, 20 * pulse);
                // Glowing eyes
                shape.setColor(1f, 0.05f, 0.05f, glow);
                shape.circle(x - 9, y + 6, 7 * pulse * glow);
                shape.circle(x + 9, y + 6, 7 * pulse * glow);
                shape.setColor(0f, 0f, 0f, 1);
                shape.circle(x - 9, y + 6, 3 * pulse);
                shape.circle(x + 9, y + 6, 3 * pulse);
                // Angry brow lines
                shape.setColor(0.9f, 0.1f, 0.1f, 1);
                shape.rectLine(x - 15, y + 14, x - 4, y + 10, 2.5f);
                shape.rectLine(x + 4,  y + 10, x + 15, y + 14, 2.5f);
                break;

            case SPEEDER:
                // Orange rocket — visually signals speed/danger
                float sp = (float)(Math.sin(pulseTimer * 1.5f) * 0.12f + 0.88f);
                // Body
                shape.setColor(0.9f, 0.45f, 0.05f, 1);
                shape.circle(x, y, 26 * sp);
                shape.setColor(1f, 0.65f, 0.1f, 1);
                shape.circle(x, y, 20 * sp);
                // Speed streaks below
                shape.setColor(1f, 0.8f, 0.2f, 0.7f);
                shape.rectLine(x - 10, y - 20, x - 10, y - 40, 3);
                shape.rectLine(x,      y - 22, x,      y - 45, 4);
                shape.rectLine(x + 10, y - 20, x + 10, y - 40, 3);
                // Warning exclamation
                shape.setColor(1f, 1f, 1f, 1);
                shape.rect(x - 3, y - 8, 6, 18);
                shape.circle(x, y - 14, 3);
                break;

            case FAKER:
                // Looks like an indigo + card — same colour as real cards!
                float fw = 72 * pulse;
                float fh = 62 * pulse;
                shape.setColor(0.38f, 0.30f, 0.72f, 1f);
                shape.rect(x - fw/2, y - 10, fw, fh);
                shape.setColor(0.22f, 0.16f, 0.56f, 1f);
                shape.rect(x - fw/2 + 4, y - 6, fw - 8, fh - 8);
                shape.setColor(0.55f, 0.48f, 0.90f, 0.45f);
                shape.rect(x - fw/2 + 4, y - 10 + fh - 14, fw - 8, 10);
                break;
        }
    }

    /** FAKER needs to draw its fake label via SpriteBatch */
    public void drawLabel(SpriteBatch batch, BitmapFont font) {
        if (type != ObstacleType.FAKER) return;

        String text = "+" + fakeValue;
        font.getData().setScale(2.2f);
        layout.setText(font, text);
        float tx = x - layout.width / 2f;
        float ty = y + 34;

        // Black outline
        font.setColor(0f, 0f, 0f, 1f);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, text, tx + dx, ty + dy);
            }
        }
        // White text — identical to real card, that's the trick
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, text, tx, ty);
        font.getData().setScale(2.0f);
    }
}
