package com.rimon.runforjoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class GameScreen {
    private ShapeRenderer shape;
    private SpriteBatch batch;
    private BitmapFont font;
    private Player player;
    private List<GameObject> objects;
    private List<Obstacle> obstacles;
    private Spawner spawner;
    private Hud hud;

    private int score;
    private int lives;
    private boolean gameOver;
    private float roadOffset = 0;

    // ✅ Pause variables
    private boolean paused = false;
    private boolean showPauseMenu = false;

    public GameScreen() {
        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.2f);

        player = new Player(Constants.LEFT_LANE, Constants.PLAYER_Y);
        objects = new ArrayList<>();
        obstacles = new ArrayList<>();
        spawner = new Spawner(Constants.SPAWN_DELAY);
        hud = new Hud(font);

        score = Constants.START_SCORE;
        lives = Constants.START_LIVES;
        gameOver = false;
    }

    public void update(float delta) {
        // ✅ Pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
            showPauseMenu = paused;
        }

        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset();
            return;
        }

        if (!paused) {
            handleInput();
            spawner.update(delta, objects, obstacles);

            roadOffset -= Constants.OBJECT_SPEED * delta;
            if (roadOffset <= -100) roadOffset = 0;

            for (int i = objects.size() - 1; i >= 0; i--) {
                GameObject obj = objects.get(i);
                obj.update(delta);
                if (obj.isOffScreen()) {
                    objects.remove(i);
                } else if (obj.collidesWith(player)) {
                    if (obj.type.equals("+")) score += obj.value;
                    else score -= obj.value;
                    objects.remove(i);
                }
            }

            for (int i = obstacles.size() - 1; i >= 0; i--) {
                Obstacle obs = obstacles.get(i);
                obs.update(delta);
                if (obs.isOffScreen()) {
                    obstacles.remove(i);
                } else if (obs.collidesWith(player)) {
                    lives--;
                    obstacles.remove(i);
                }
            }

            if (lives <= 0 || score <= 0) gameOver = true;
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) player.moveTo(Constants.LEFT_LANE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) player.moveTo(Constants.RIGHT_LANE);
        if (Gdx.input.justTouched()) {
            player.moveTo(Gdx.input.getX() < Gdx.graphics.getWidth()/2f ? Constants.LEFT_LANE : Constants.RIGHT_LANE);
        }
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.15f, 0.15f, 0.2f, 1);
        shape.rect(100, 0, 400, Constants.SCREEN_HEIGHT);
        shape.setColor(0.3f, 0.2f, 0.15f, 1);
        shape.rect(0, 0, 100, Constants.SCREEN_HEIGHT);
        shape.rect(500, 0, 100, Constants.SCREEN_HEIGHT);
        shape.setColor(1, 1, 1, 0.6f);
        for (float ly = roadOffset; ly < Constants.SCREEN_HEIGHT; ly += 100) {
            shape.rect(295, ly + 25, 10, 50);
        }

        if (!gameOver) {
            player.draw(shape);
            for (GameObject obj : objects) obj.drawShape(shape);
            for (Obstacle obs : obstacles) obs.drawShape(shape);
            hud.drawButtons(shape);
        }
        shape.end();

        batch.begin();
        if (gameOver) {
            font.draw(batch, "GAME OVER - Press R", 150, 400);
        } else {
            for (GameObject obj : objects) obj.drawLabel(batch, font);
            hud.drawText(batch, score, lives);

            // ✅ Paused text
            if (paused) {
                font.draw(batch, "PAUSED", 230, 360);
            }
        }
        batch.end();

        // ✅ Pause Menu UI
        if (showPauseMenu) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shape.rect(Constants.SCREEN_WIDTH / 2f - 100, Constants.SCREEN_HEIGHT / 2f - 100, 200, 200);
            shape.end();

            batch.begin();
            font.draw(batch, "Resume (R)", Constants.SCREEN_WIDTH / 2f - 60, Constants.SCREEN_HEIGHT / 2f + 40);
            font.draw(batch, "Restart (T)", Constants.SCREEN_WIDTH / 2f - 60, Constants.SCREEN_HEIGHT / 2f);
            font.draw(batch, "Exit (E)", Constants.SCREEN_WIDTH / 2f - 60, Constants.SCREEN_HEIGHT / 2f - 40);
            batch.end();

            // ✅ Keyboard shortcuts
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                paused = false;
                showPauseMenu = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
                reset();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                Gdx.app.exit();
            }

            // ✅ Mouse click detection
            if (Gdx.input.justTouched()) {
                float mx = Gdx.input.getX();
                float my = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip Y

                // Resume button area
                if (mx > Constants.SCREEN_WIDTH/2f - 80 && mx < Constants.SCREEN_WIDTH/2f + 80 &&
                    my > Constants.SCREEN_HEIGHT/2f + 20 && my < Constants.SCREEN_HEIGHT/2f + 60) {
                    paused = false;
                    showPauseMenu = false;
                }

                // Restart button area
                if (mx > Constants.SCREEN_WIDTH/2f - 80 && mx < Constants.SCREEN_WIDTH/2f + 80 &&
                    my > Constants.SCREEN_HEIGHT/2f - 20 && my < Constants.SCREEN_HEIGHT/2f + 20) {
                    reset();
                }

                // Exit button area
                if (mx > Constants.SCREEN_WIDTH/2f - 80 && mx < Constants.SCREEN_WIDTH/2f + 80 &&
                    my > Constants.SCREEN_HEIGHT/2f - 60 && my < Constants.SCREEN_HEIGHT/2f - 20) {
                    Gdx.app.exit();
                }
            }
        }
    }

    private void reset() {
        score = Constants.START_SCORE;
        lives = Constants.START_LIVES;
        Constants.OBJECT_SPEED = 300f;
        gameOver = false;
        objects.clear();
        obstacles.clear();
        paused = false;
        showPauseMenu = false;
    }

    public void dispose() { shape.dispose(); batch.dispose(); font.dispose(); }
}
