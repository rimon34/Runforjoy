package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Scenery {
    public float x, y;
    public int type; // 0=tree, 1=building, 2=lamppost, 3=bush
    public float scrollOffset;

    public Scenery(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.scrollOffset = 0;
    }

    public void update(float delta) {
        float speed = Constants.OBJECT_SPEED * delta * 0.35f;
        y -= speed;
        scrollOffset += delta * 2;
    }

    public boolean isOffScreen() {
        return y < -160;
    }

    /**
     * Draw the scenery element. x is the LEFT EDGE of the 80px strip.
     * Left strip: x=10 → draws in 10..90
     * Right strip: x=510 → draws in 510..590
     */
    public void draw(ShapeRenderer shape) {
        switch (type) {
            case 0: // Tree — fits in 40px wide footprint centered at x+20
                // Trunk
                shape.setColor(0.55f, 0.33f, 0.1f, 1);
                shape.rect(x + 17, y, 10, 45);
                // Shadow under canopy
                shape.setColor(0.08f, 0.35f, 0.08f, 1);
                shape.circle(x + 22, y + 55, 22);
                // Main canopy
                shape.setColor(0.12f, 0.72f, 0.12f, 1);
                shape.circle(x + 22, y + 65, 20);
                // Side puffs
                shape.circle(x + 8,  y + 56, 15);
                shape.circle(x + 36, y + 56, 15);
                // Highlight puff
                shape.setColor(0.25f, 0.85f, 0.25f, 1);
                shape.circle(x + 22, y + 75, 14);
                break;

            case 1: // Building — fits in 50px wide
                // Building body
                shape.setColor(0.42f, 0.42f, 0.52f, 1);
                shape.rect(x + 5, y, 50, 75);
                // Dark face
                shape.setColor(0.28f, 0.28f, 0.38f, 1);
                shape.rect(x + 8, y + 5, 44, 65);
                // Windows (2 columns x 3 rows)
                float winOn = (scrollOffset % 2 < 1) ? 0.9f : 0.4f;
                for (int col = 0; col < 2; col++) {
                    for (int row = 0; row < 3; row++) {
                        float wx = x + 14 + col * 20;
                        float wy = y + 12 + row * 22;
                        // Random-looking lit/dark per window
                        float lit = ((col + row + (int)(x / 10)) % 3 == 0) ? 0.9f : 0.3f;
                        shape.setColor(lit, lit * 0.85f, 0.2f, 1);
                        shape.rect(wx, wy, 10, 12);
                    }
                }
                // Rooftop
                shape.setColor(0.55f, 0.55f, 0.65f, 1);
                shape.rect(x + 3, y + 73, 54, 6);
                // Antenna
                shape.setColor(0.6f, 0.6f, 0.6f, 1);
                shape.rect(x + 27, y + 79, 3, 18);
                shape.setColor(1f, 0.3f, 0.3f, 1);
                shape.circle(x + 28, y + 97, 3);
                break;

            case 2: // Lamppost — slim, centered
                // Post
                shape.setColor(0.55f, 0.55f, 0.6f, 1);
                shape.rect(x + 19, y, 6, 70);
                // Crossbar
                shape.setColor(0.65f, 0.65f, 0.7f, 1);
                shape.rect(x + 8, y + 68, 28, 5);
                // Lamp housing
                shape.setColor(0.8f, 0.8f, 0.7f, 1);
                shape.rect(x + 9, y + 66, 10, 8);
                shape.rect(x + 25, y + 66, 10, 8);
                // Glow
                float glow = (float)(Math.sin(scrollOffset * 3) * 0.15 + 0.75);
                shape.setColor(1f, 1f, 0.55f, glow);
                shape.circle(x + 14, y + 74, 7);
                shape.circle(x + 30, y + 74, 7);
                // Soft glow halo
                shape.setColor(1f, 1f, 0.4f, 0.25f);
                shape.circle(x + 14, y + 74, 13);
                shape.circle(x + 30, y + 74, 13);
                // Base
                shape.setColor(0.4f, 0.4f, 0.45f, 1);
                shape.rect(x + 15, y, 14, 6);
                break;

            case 3: // Bush — wide, low
                // Dark base shadow
                shape.setColor(0.1f, 0.4f, 0.08f, 1);
                shape.circle(x + 22, y + 10, 17);
                // Main bush body
                shape.setColor(0.18f, 0.62f, 0.1f, 1);
                shape.circle(x + 14, y + 16, 14);
                shape.circle(x + 30, y + 14, 15);
                shape.circle(x + 22, y + 20, 12);
                shape.circle(x + 8,  y + 10, 10);
                shape.circle(x + 38, y + 10, 10);
                // Highlight puffs
                shape.setColor(0.3f, 0.8f, 0.2f, 1);
                shape.circle(x + 14, y + 22, 8);
                shape.circle(x + 30, y + 20, 9);
                break;
        }
    }
}
