package com.rimon.runforjoy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Particle {
    public float x, y, vx, vy, life, maxLife, size, r, g, b;

    public Particle(float x, float y, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.vx = MathUtils.random(-120, 120);
        this.vy = MathUtils.random(60, 200);
        this.life = this.maxLife = 0.6f;
        this.size = MathUtils.random(3, 8);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public boolean update(float delta) {
        x += vx * delta;
        y += vy * delta;
        life -= delta;
        return life > 0;
    }

    public void draw(ShapeRenderer shape) {
        shape.setColor(r, g, b, life / maxLife);
        shape.circle(x, y, size);
    }
}
