package com.rimon.runforjoy;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Obstacle {
    float x, y;
    public Obstacle(float x, float y) { this.x = x; this.y = y; }
    public void update(float delta) { y -= Constants.OBJECT_SPEED * delta; }
    public boolean isOffScreen() { return y < -100; }
    public boolean collidesWith(Player p) {
        if (p == null) return false;
        return Math.abs(x - p.x) < Constants.CATCH_X_MARGIN && y < Constants.CATCH_Y_TOP && y > Constants.CATCH_Y_BOTTOM;
    }
    public void drawShape(ShapeRenderer shape) {
        shape.setColor(1, 0, 0, 1);
        shape.rect(x - 35, y, 70, 70);
    }
}
