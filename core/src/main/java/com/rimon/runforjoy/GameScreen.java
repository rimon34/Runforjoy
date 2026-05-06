package com.rimon.runforjoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen {
    private ShapeRenderer shape;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private Player player;
    private List<GameObject> objects;
    private List<Obstacle> obstacles;
    private List<Particle> particles;
    private Spawner spawner;

    private long score;
    private int lives, combo, highScore;
    private boolean gameOver, paused;
    private float screenShake, screenFlash, levelUpTimer;
    private float gameTimer = 0;

    private float resumeX = 200, resumeY = 350, resumeW = 200, resumeH = 50;
    private float exitX = 200, exitY = 280, exitW = 200, exitH = 50;
    private float roadOffset = 0;
    private float treeOffset = 0;

    private List<ScorePopup> scorePopups = new ArrayList<>();

    public GameScreen() {
        shape = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();
        font.getData().setScale(2.0f);

        player = new Player(Constants.LEFT_LANE, Constants.PLAYER_Y);
        objects = new ArrayList<>();
        obstacles = new ArrayList<>();
        particles = new ArrayList<>();
        spawner = new Spawner();

        loadHighScore();
        resetGame();
    }

    private void loadHighScore() {
        Preferences prefs = Gdx.app.getPreferences("runforjoy");
        highScore = prefs.getInteger("highscore", 0);
    }

    private void saveHighScore() {
        if (score > highScore) {
            highScore = (int)score;
            Preferences prefs = Gdx.app.getPreferences("runforjoy");
            prefs.putInteger("highscore", highScore);
            prefs.flush();
        }
    }

    private void resetGame() {
        score = 0;
        lives = Constants.START_LIVES;
        combo = 0;
        gameOver = false;
        paused = false;
        screenShake = screenFlash = levelUpTimer = 0;
        gameTimer = 0;
        scorePopups.clear();

        RunForJoy.currentLevel = 1;
        RunForJoy.hasShield = false;
        RunForJoy.isDoubleScoreActive = false;
        RunForJoy.isSlowMoActive = false;
        RunForJoy.isInvincible = false;
        RunForJoy.doubleScoreTimer = 0;
        RunForJoy.slowMoTimer = 0;

        Constants.OBJECT_SPEED = Constants.BASE_OBJECT_SPEED;

        objects.clear();
        obstacles.clear();
        particles.clear();
        player.moveTo(Constants.LEFT_LANE);
    }

    private void updateDifficulty() {
        gameTimer += Gdx.graphics.getDeltaTime();

        if (gameTimer < 30) {
            Constants.OBJECT_SPEED = Constants.BASE_OBJECT_SPEED;
            Constants.SPAWN_DELAY = Constants.BASE_SPAWN_DELAY;
        } else if (gameTimer < 60) {
            Constants.OBJECT_SPEED = 380f;
            Constants.SPAWN_DELAY = 0.9f;
        } else if (gameTimer < 90) {
            Constants.OBJECT_SPEED = 520f;
            Constants.SPAWN_DELAY = 0.7f;
        } else {
            Constants.OBJECT_SPEED = Constants.MAX_OBJECT_SPEED;
            Constants.SPAWN_DELAY = 0.5f;
        }
    }

    private void updateLevel() {
        int newLevel = (int)(score / Constants.LEVEL_UP_SCORE) + 1;
        if (newLevel != RunForJoy.currentLevel) {
            RunForJoy.currentLevel = newLevel;
            levelUpTimer = 2f;
            for (int i = 0; i < 60; i++) {
                particles.add(new Particle(Constants.SCREEN_WIDTH/2f + (float)(Math.random() * 300 - 150),
                    Constants.SCREEN_HEIGHT/2f, 1, 0.8f, 0));
            }
        }
    }

    public void update(float delta) {
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) resetGame();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) paused = !paused;
        if (paused) {
            handlePauseInput();
            return;
        }

        updateDifficulty();

        screenShake = Math.max(0, screenShake - delta);
        screenFlash = Math.max(0, screenFlash - delta);
        levelUpTimer = Math.max(0, levelUpTimer - delta);

        Iterator<ScorePopup> popupIter = scorePopups.iterator();
        while (popupIter.hasNext()) {
            if (!popupIter.next().update(delta)) popupIter.remove();
        }

        RunForJoy.updatePowerUps(delta);
        player.update(delta);
        handleInput();
        spawner.update(delta, objects, obstacles);

        roadOffset -= Constants.OBJECT_SPEED * delta;
        if (roadOffset <= -100) roadOffset = 0;

        treeOffset -= Constants.OBJECT_SPEED * delta * 0.4f;
        if (treeOffset <= -180) treeOffset = 0;

        Iterator<GameObject> objIter = objects.iterator();
        while (objIter.hasNext()) {
            GameObject obj = objIter.next();
            obj.update(delta);
            if (obj.isOffScreen()) {
                objIter.remove();
            } else if (obj.collidesWith(player)) {
                handleCollection(obj);
                objIter.remove();
            }
        }

        Iterator<Obstacle> obsIter = obstacles.iterator();
        while (obsIter.hasNext()) {
            Obstacle obs = obsIter.next();
            obs.update(delta);
            if (obs.isOffScreen()) {
                obsIter.remove();
            } else if (obs.collidesWith(player)) {
                handleObstacleHit(obs);
                obsIter.remove();
            }
        }

        Iterator<Particle> partIter = particles.iterator();
        while (partIter.hasNext()) {
            if (!partIter.next().update(delta)) partIter.remove();
        }

        updateLevel();
        if (lives <= 0) {
            gameOver = true;
            saveHighScore();
        }
    }

    private void handleCollection(GameObject obj) {
        if (obj.isPowerUp) {
            activatePowerUp(obj.powerUpType);
            for (int i = 0; i < 25; i++) {
                particles.add(new Particle(obj.x, obj.y, 0.7f, 0.3f, 0.9f));
            }
            scorePopups.add(new ScorePopup(obj.x, obj.y, obj.powerUpType.symbol, 0.7f, 0.3f, 0.9f));
            return;
        }

        long points = obj.getPointValue();

        if (points > 0) {
            combo++;
            long comboBonus = points * Math.min(combo, 5);
            points += comboBonus / 2;

            if (combo % 5 == 0 && combo > 0) {
                scorePopups.add(new ScorePopup(obj.x, obj.y + 50, "COMBO x" + combo + "!", 1, 0.5f, 0));
                points += 30 * (combo / 5);
            }
            screenFlash = 0.12f;
            for (int i = 0; i < 12; i++) {
                particles.add(new Particle(obj.x, obj.y, 0.2f, 0.9f, 0.3f));
            }
            scorePopups.add(new ScorePopup(obj.x, obj.y, "+" + points, 0.2f, 0.9f, 0.3f));
        } else {
            combo = 0;
            screenShake = 0.2f;
            for (int i = 0; i < 15; i++) {
                particles.add(new Particle(obj.x, obj.y, 0.9f, 0.2f, 0.2f));
            }
            scorePopups.add(new ScorePopup(obj.x, obj.y, "" + points, 0.9f, 0.2f, 0.2f));
        }

        score += points;
        if (score < 0) score = 0;
    }

    private void handleObstacleHit(Obstacle obs) {
        if (RunForJoy.hasShield) {
            RunForJoy.hasShield = false;
            for (int i = 0; i < 35; i++) {
                particles.add(new Particle(player.x, player.y, 0.3f, 0.6f, 1f));
            }
            scorePopups.add(new ScorePopup(player.x, player.y, "SHIELD BLOCK!", 0.3f, 0.6f, 1f));
            return;
        }

        lives--;
        combo = 0;
        screenShake = 0.35f;
        screenFlash = 0.25f;
        player.hit();
        RunForJoy.isInvincible = true;

        new Thread(() -> {
            try { Thread.sleep((long)(Constants.INVINCIBLE_DURATION * 1000)); }
            catch (InterruptedException e) {}
            RunForJoy.isInvincible = false;
        }).start();

        for (int i = 0; i < 35; i++) {
            particles.add(new Particle(player.x, player.y, 1f, 0.2f, 0.2f));
        }
        scorePopups.add(new ScorePopup(player.x, player.y, "-1 LIFE!", 1, 0.2f, 0.2f));
    }

    private void activatePowerUp(PowerUpType type) {
        switch (type) {
            case SHIELD:
                RunForJoy.hasShield = true;
                break;
            case DOUBLE_SCORE:
                RunForJoy.isDoubleScoreActive = true;
                RunForJoy.doubleScoreTimer = Constants.DOUBLE_SCORE_DURATION;
                break;
            case SLOW_MO:
                RunForJoy.isSlowMoActive = true;
                RunForJoy.slowMoTimer = Constants.SLOW_MO_DURATION;
                break;
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            player.moveTo(Constants.LEFT_LANE);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            player.moveTo(Constants.RIGHT_LANE);
        }
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            player.moveTo(touchX < Gdx.graphics.getWidth() / 2f ? Constants.LEFT_LANE : Constants.RIGHT_LANE);
        }
    }

    private void handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) paused = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) Gdx.app.exit();

        if (Gdx.input.justTouched()) {
            float mx = Gdx.input.getX();
            float my = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (mx >= resumeX && mx <= resumeX + resumeW && my >= resumeY && my <= resumeY + resumeH) {
                paused = false;
            }
            if (mx >= exitX && mx <= exitX + exitW && my >= exitY && my <= exitY + exitH) {
                Gdx.app.exit();
            }
        }
    }

    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float shakeX = screenShake > 0 ? (float)(Math.random() * screenShake * 15 - screenShake * 7.5f) : 0;

        // ========== SHAPE RENDERER ==========
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Screen flash
        if (screenFlash > 0) {
            shape.setColor(1, 1, 1, screenFlash * 0.6f);
            shape.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }

        // Sky
        shape.setColor(0.05f, 0.05f, 0.1f, 1);
        shape.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Stars
        shape.setColor(1, 1, 1, 0.4f);
        for (int i = 0; i < 80; i++) {
            float starX = (i * 131) % Constants.SCREEN_WIDTH;
            float starY = (i * 253 + roadOffset * 20) % Constants.SCREEN_HEIGHT;
            shape.rect(starX, starY, 2, 2);
        }

        // ========== SIMPLE SCENERY (GUARANTEED VISIBLE) ==========
        // LEFT TREES
        for (float y = treeOffset; y < Constants.SCREEN_HEIGHT + 200; y += 140) {
            // Trunk
            shape.setColor(0.6f, 0.4f, 0.2f, 1);
            shape.rect(40, y, 12, 40);
            // Leaves
            shape.setColor(0.1f, 0.8f, 0.1f, 1);
            shape.circle(46, y + 50, 18);
            shape.circle(35, y + 40, 14);
            shape.circle(57, y + 40, 14);
        }

        // RIGHT TREES
        for (float y = treeOffset; y < Constants.SCREEN_HEIGHT + 200; y += 140) {
            // Trunk
            shape.setColor(0.6f, 0.4f, 0.2f, 1);
            shape.rect(548, y, 12, 40);
            // Leaves
            shape.setColor(0.1f, 0.8f, 0.1f, 1);
            shape.circle(554, y + 50, 18);
            shape.circle(543, y + 40, 14);
            shape.circle(565, y + 40, 14);
        }

        // Road
        shape.setColor(0.15f, 0.15f, 0.2f, 1);
        shape.rect(100 + shakeX, 0, 400, Constants.SCREEN_HEIGHT);

        // Side grass/soil
        shape.setColor(0.4f, 0.3f, 0.15f, 1);
        shape.rect(0 + shakeX, 0, 100, Constants.SCREEN_HEIGHT);
        shape.rect(500 + shakeX, 0, 100, Constants.SCREEN_HEIGHT);

        // Road lines
        shape.setColor(1, 1, 1, 0.5f);
        for (float ly = roadOffset; ly < Constants.SCREEN_HEIGHT; ly += 70) {
            shape.rect(295 + shakeX, ly + 25, 10, 35);
        }

        // Center line
        shape.setColor(1, 1, 0.4f, 0.4f);
        shape.rect(298 + shakeX, 0, 4, Constants.SCREEN_HEIGHT);

        // Game objects
        if (!gameOver && !paused) {
            player.draw(shape);
            for (GameObject obj : objects) obj.drawShape(shape);
            for (Obstacle obs : obstacles) obs.drawShape(shape);
        }

        // Particles
        for (Particle p : particles) p.draw(shape);

        // Pause buttons
        if (paused) {
            shape.setColor(0.2f, 0.2f, 0.3f, 0.9f);
            shape.rect(resumeX + shakeX, resumeY, resumeW, resumeH);
            shape.rect(exitX + shakeX, exitY, exitW, exitH);
        }

        shape.end();

        // ========== SPRITE BATCH RENDERER ==========
        batch.begin();

        // Object labels
        for (GameObject obj : objects) {
            obj.drawLabel(batch, font);
        }

        // Score popups
        for (ScorePopup popup : scorePopups) {
            popup.draw(batch, font);
        }

        // ========== HUD TEXT WITH BLACK OUTLINE (GUARANTEED VISIBLE) ==========
        String scoreText = "SCORE: " + formatScore(score);
        String livesText = "LIVES: " + lives;
        String bestText = "BEST: " + formatScore(highScore);
        String levelText = "LVL: " + RunForJoy.currentLevel;
        String comboText = combo > 0 ? "COMBO x" + combo : "";

        // Draw black outline (8 directions)
        font.setColor(0, 0, 0, 1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, scoreText, 20 + dx, Constants.SCREEN_HEIGHT - 20 + dy);
                font.draw(batch, livesText, 20 + dx, Constants.SCREEN_HEIGHT - 55 + dy);
                font.draw(batch, bestText, 20 + dx, Constants.SCREEN_HEIGHT - 90 + dy);
                font.draw(batch, levelText, Constants.SCREEN_WIDTH - 100 + dx, Constants.SCREEN_HEIGHT - 20 + dy);
                if (combo > 0) {
                    font.draw(batch, comboText, Constants.SCREEN_WIDTH - 130 + dx, Constants.SCREEN_HEIGHT - 55 + dy);
                }
            }
        }

        // Draw actual text (bright colors)
        font.setColor(1, 1, 0.2f, 1);
        font.draw(batch, scoreText, 20, Constants.SCREEN_HEIGHT - 20);

        font.setColor(1, 0.3f, 0.3f, 1);
        font.draw(batch, livesText, 20, Constants.SCREEN_HEIGHT - 55);

        font.setColor(0.3f, 1f, 0.3f, 1);
        font.draw(batch, bestText, 20, Constants.SCREEN_HEIGHT - 90);

        font.setColor(0.3f, 0.7f, 1f, 1);
        font.draw(batch, levelText, Constants.SCREEN_WIDTH - 100, Constants.SCREEN_HEIGHT - 20);

        if (combo > 0) {
            float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.01) * 0.3f + 1f);
            font.setColor(1, 0.5f + pulse * 0.3f, 0, 1);
            font.draw(batch, comboText, Constants.SCREEN_WIDTH - 130, Constants.SCREEN_HEIGHT - 55);
        }

        // Power-up status
        float py = Constants.SCREEN_HEIGHT - 130;
        if (RunForJoy.hasShield) {
            font.setColor(0.4f, 0.7f, 1f, 1);
            font.draw(batch, "SHIELD", 20, py);
            py -= 25;
        }
        if (RunForJoy.isDoubleScoreActive) {
            font.setColor(1, 0.8f, 0, 1);
            font.draw(batch, "1.5x SCORE", 20, py);
            py -= 25;
        }
        if (RunForJoy.isSlowMoActive) {
            font.setColor(0.5f, 0.9f, 1f, 1);
            font.draw(batch, "SLOW MO", 20, py);
        }

        // Level up animation
        if (levelUpTimer > 0) {
            float alpha = Math.min(1, levelUpTimer * 1.5f);
            float scale = 2.5f + (1 - alpha) * 1.5f;
            font.getData().setScale(scale);
            font.setColor(1, 0.6f, 0, alpha);
            font.draw(batch, "LEVEL " + RunForJoy.currentLevel + "!",
                Constants.SCREEN_WIDTH/2f - 70, Constants.SCREEN_HEIGHT/2f + 50);
            font.getData().setScale(2.0f);
        }

        // Game over
        if (gameOver) {
            // Outline
            font.setColor(0, 0, 0, 1);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    font.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH/2f - 65 + dx, Constants.SCREEN_HEIGHT/2f + 60 + dy);
                    font.draw(batch, "SCORE: " + formatScore(score), Constants.SCREEN_WIDTH/2f - 70 + dx, Constants.SCREEN_HEIGHT/2f + 20 + dy);
                    font.draw(batch, "PRESS R", Constants.SCREEN_WIDTH/2f - 45 + dx, Constants.SCREEN_HEIGHT/2f - 30 + dy);
                }
            }
            font.setColor(1, 0.2f, 0.2f, 1);
            font.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH/2f - 65, Constants.SCREEN_HEIGHT/2f + 60);
            font.setColor(1, 1, 0.2f, 1);
            font.draw(batch, "SCORE: " + formatScore(score), Constants.SCREEN_WIDTH/2f - 70, Constants.SCREEN_HEIGHT/2f + 20);
            font.setColor(1, 0.8f, 0, 1);
            font.draw(batch, "PRESS R", Constants.SCREEN_WIDTH/2f - 45, Constants.SCREEN_HEIGHT/2f - 30);
        }

        // Pause
        if (paused) {
            font.setColor(0, 0, 0, 1);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    font.draw(batch, "PAUSED", Constants.SCREEN_WIDTH/2f - 45 + dx, Constants.SCREEN_HEIGHT/2f + 80 + dy);
                    font.draw(batch, "RESUME", resumeX + 45 + dx, resumeY + 37 + dy);
                    font.draw(batch, "EXIT", exitX + 45 + dx, exitY + 37 + dy);
                }
            }
            font.setColor(1, 0.4f, 0.4f, 1);
            font.draw(batch, "PAUSED", Constants.SCREEN_WIDTH/2f - 45, Constants.SCREEN_HEIGHT/2f + 80);
            font.setColor(1, 1, 1, 1);
            font.draw(batch, "RESUME", resumeX + 45, resumeY + 35);
            font.draw(batch, "EXIT", exitX + 45, exitY + 35);
        }

        batch.end();
    }

    private String formatScore(long score) {
        if (score < 1000) return String.valueOf(score);
        if (score < 1000000) return (score / 1000) + "K";
        return (score / 1000000) + "M";
    }

    public void dispose() {
        shape.dispose();
        batch.dispose();
        font.dispose();
    }

    private class ScorePopup {
        float x, y, life = 1.0f;
        String text;
        float r, g, b;

        ScorePopup(float x, float y, String text, float r, float g, float b) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        boolean update(float delta) {
            y += delta * 85;
            life -= delta * 1.8f;
            return life > 0;
        }

        void draw(SpriteBatch batch, BitmapFont font) {
            font.setColor(r, g, b, life);
            float scale = 1.2f * (1 + (1 - life) * 0.6f);
            font.getData().setScale(scale);
            font.draw(batch, text, x - 40, y);
            font.getData().setScale(2.0f);
        }
    }
}
