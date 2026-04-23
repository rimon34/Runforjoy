package com.rimon.runforjoy;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameObject {
    float x, y;
    String type;
    int value;
    boolean isFake;
    boolean hasTransformed = false;

    public GameObject(float x, float y, String type, int value, boolean isFake) {
        this.x = x; this.y = y;
        this.type = (type == null) ? "+" : type; // Safety check
        this.value = value;
        this.isFake = isFake;
    }

    public void update(float delta) {
        y -= Constants.OBJECT_SPEED * delta;
        if (isFake && !hasTransformed && y < Constants.SCREEN_HEIGHT * 0.55f) {
            hasTransformed = true;
            type = type.equals("+") ? "-" : "+";
            value = type.equals("-") ? (value * 2) : 50;
        }
    }

    public boolean isOffScreen() { return y < -100; }

    public boolean collidesWith(Player p) {
        if (p == null) return false;
        return Math.abs(x - p.x) < Constants.CATCH_X_MARGIN && y < Constants.CATCH_Y_TOP && y > Constants.CATCH_Y_BOTTOM;
    }

    public void drawShape(ShapeRenderer shape) {
        if (!shape.isDrawing()) return; // Safety check
        if (type.equals("+")) shape.setColor(0.2f, 0.8f, 0.3f, 1);
        else shape.setColor(0.9f, 0.2f, 0.2f, 1);
        shape.rect(x - 35, y, 70, 60);
    }

    public void drawLabel(SpriteBatch batch, BitmapFont font) {
        if (font == null || !batch.isDrawing()) return; // Safety check
        font.draw(batch, type + value, x - 25, y + 45);
    }
}
