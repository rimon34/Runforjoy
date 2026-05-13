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
    private List<Scenery> sceneryLeft;
    private List<Scenery> sceneryRight;
    private Spawner spawner;

    private long score;
    private int lives, combo, highScore;
    private boolean gameOver, paused;
    private float screenShake, screenFlash, levelUpTimer;

    private float resumeX = 200, resumeY = 350, resumeW = 200, resumeH = 50;
    private float exitX   = 200, exitY   = 280, exitW   = 200, exitH   = 50;
    private float roadOffset = 0;
    private float scenerySpawnTimer = 0;

    private List<ScorePopup> scorePopups = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public GameScreen() {
        shape  = new ShapeRenderer();
        batch  = new SpriteBatch();
        font   = new BitmapFont();
        layout = new GlyphLayout();
        font.getData().setScale(2.0f);

        player      = new Player(Constants.LEFT_LANE, Constants.PLAYER_Y);
        objects     = new ArrayList<>();
        obstacles   = new ArrayList<>();
        particles   = new ArrayList<>();
        sceneryLeft  = new ArrayList<>();
        sceneryRight = new ArrayList<>();
        spawner     = new Spawner();

        loadHighScore();
        resetGame();
    }

    // -------------------------------------------------------------------------
    // High-score persistence
    // -------------------------------------------------------------------------
    private void loadHighScore() {
        Preferences prefs = Gdx.app.getPreferences("runforjoy");
        highScore = prefs.getInteger("highscore", 0);
    }

    private void saveHighScore() {
        if (score > highScore) {
            highScore = (int) score;
            Preferences prefs = Gdx.app.getPreferences("runforjoy");
            prefs.putInteger("highscore", highScore);
            prefs.flush();
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    private void resetGame() {
        score      = 0;
        lives      = Constants.START_LIVES;
        combo      = 0;
        gameOver   = false;
        paused     = false;
        screenShake = screenFlash = levelUpTimer = 0;
        scorePopups.clear();
        scenerySpawnTimer = 0;

        RunForJoy.currentLevel       = 1;
        RunForJoy.hasShield          = false;
        RunForJoy.isDoubleScoreActive = false;
        RunForJoy.isSlowMoActive     = false;
        RunForJoy.isInvincible       = false;
        RunForJoy.doubleScoreTimer   = 0;
        RunForJoy.slowMoTimer        = 0;

        Constants.OBJECT_SPEED = Constants.BASE_OBJECT_SPEED;
        Constants.SPAWN_DELAY  = Constants.BASE_SPAWN_DELAY;

        objects.clear();
        obstacles.clear();
        particles.clear();
        sceneryLeft.clear();
        sceneryRight.clear();
        player.moveTo(Constants.LEFT_LANE);

        // Pre-populate scenery so screen isn't empty at start
        for (int i = 0; i < 6; i++) {
            float startY = i * 150;
            sceneryLeft.add(new Scenery(10,  startY, i % 4));
            sceneryRight.add(new Scenery(510, startY, (i + 2) % 4));
        }
    }

    // -------------------------------------------------------------------------
    // Difficulty — gradual, moderate increases
    // Speed  : +12 per level (was +18), cap 420 (was 480)
    // Delay  : -0.03s per level (was -0.04s), min 0.7s (was 0.6s)
    // Lerp   : 0.03 factor = smoother ramp (was 0.05)
    // -------------------------------------------------------------------------
    private void updateDifficulty() {
        int level = RunForJoy.currentLevel;

        float targetSpeed = Math.min(
            Constants.BASE_OBJECT_SPEED + (level - 1) * 12f,
            Constants.MAX_OBJECT_SPEED
        );
        Constants.OBJECT_SPEED += (targetSpeed - Constants.OBJECT_SPEED) * 0.03f;

        Constants.SPAWN_DELAY = Math.max(0.7f, Constants.BASE_SPAWN_DELAY - (level - 1) * 0.03f);
    }

    // -------------------------------------------------------------------------
    // Level — every 100 points
    // -------------------------------------------------------------------------
    private void updateLevel() {
        int newLevel = (int) (score / Constants.LEVEL_UP_SCORE) + 1;
        if (newLevel != RunForJoy.currentLevel) {
            RunForJoy.currentLevel = newLevel;
            levelUpTimer = 2f;
            for (int i = 0; i < 60; i++) {
                particles.add(new Particle(
                    Constants.SCREEN_WIDTH / 2f + (float) (Math.random() * 300 - 150),
                    Constants.SCREEN_HEIGHT / 2f, 1, 0.8f, 0));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Scenery
    // -------------------------------------------------------------------------
    private void updateScenery(float delta) {
        scenerySpawnTimer -= delta;
        if (scenerySpawnTimer <= 0) {
            scenerySpawnTimer = 0.7f;
            int type = (int) (Math.random() * 4);
            sceneryLeft.add(new Scenery(10,  Constants.SCREEN_HEIGHT + 50, type));
            sceneryRight.add(new Scenery(510, Constants.SCREEN_HEIGHT + 50, (type + 2) % 4));
        }

        Iterator<Scenery> leftIt = sceneryLeft.iterator();
        while (leftIt.hasNext()) {
            Scenery s = leftIt.next();
            s.update(delta);
            if (s.isOffScreen()) leftIt.remove();
        }
        Iterator<Scenery> rightIt = sceneryRight.iterator();
        while (rightIt.hasNext()) {
            Scenery s = rightIt.next();
            s.update(delta);
            if (s.isOffScreen()) rightIt.remove();
        }
    }

    // -------------------------------------------------------------------------
    // Update loop
    // -------------------------------------------------------------------------
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

        screenShake  = Math.max(0, screenShake  - delta);
        screenFlash  = Math.max(0, screenFlash  - delta);
        levelUpTimer = Math.max(0, levelUpTimer - delta);

        // Score popups
        Iterator<ScorePopup> popupIter = scorePopups.iterator();
        while (popupIter.hasNext()) {
            if (!popupIter.next().update(delta)) popupIter.remove();
        }

        RunForJoy.updatePowerUps(delta);
        player.update(delta);
        handleInput();
        spawner.update(delta, objects, obstacles);
        updateScenery(delta);

        roadOffset -= Constants.OBJECT_SPEED * delta;
        if (roadOffset <= -100) roadOffset = 0;

        // --- GameObject pass: update, collision, off-screen removal ---
        int collectedRowId = -1;
        Iterator<GameObject> objIter = objects.iterator();
        while (objIter.hasNext()) {
            GameObject obj = objIter.next();
            obj.update(delta);
            if (obj.isOffScreen()) {
                objIter.remove();
            } else if (obj.collidesWith(player)) {
                handleCollection(obj);
                collectedRowId = obj.rowId;
                objIter.remove();
                break; // handle one collection per frame
            }
        }

        // Remove the row partner instantly after a collection
        if (collectedRowId >= 0) {
            Spawner.removeRowPartner(objects, collectedRowId);
        }

        // Second pass — clear any remaining off-screen objects
        objIter = objects.iterator();
        while (objIter.hasNext()) {
            if (objIter.next().isOffScreen()) objIter.remove();
        }

        // --- Obstacle pass ---
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

        // --- Particle pass ---
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

    // -------------------------------------------------------------------------
    // Collection / obstacle handlers
    // -------------------------------------------------------------------------
    private void handleCollection(GameObject obj) {
        if (obj.isPowerUp) {
            activatePowerUp(obj.powerUpType);
            for (int i = 0; i < 25; i++) {
                particles.add(new Particle(obj.x, obj.y, 0.7f, 0.3f, 0.9f));
            }
            String label;
            switch (obj.powerUpType) {
                case SHIELD:       label = "SHIELD!";       break;
                case DOUBLE_SCORE: label = "DOUBLE SCORE!"; break;
                case SLOW_MO:      label = "SLOW MO!";      break;
                default:           label = "POWER UP!";     break;
            }
            scorePopups.add(new ScorePopup(obj.x, obj.y, label, 1f, 0.85f, 0.1f));
            return;
        }

        long points = obj.getPointValue();

        if (points > 0) {
            combo++;
            if (combo % 5 == 0) {
                scorePopups.add(new ScorePopup(obj.x, obj.y + 55, "COMBO x" + combo + "!", 1, 0.5f, 0));
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
            scorePopups.add(new ScorePopup(obj.x, obj.y, String.valueOf(points), 0.9f, 0.2f, 0.2f));
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
            try { Thread.sleep((long) (Constants.INVINCIBLE_DURATION * 1000)); }
            catch (InterruptedException ignored) {}
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

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------
    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float shakeX = screenShake > 0
            ? (float) (Math.random() * screenShake * 15 - screenShake * 7.5f) : 0;

        // ===== SHAPE RENDERER =====
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Sky
        shape.setColor(0.04f, 0.04f, 0.12f, 1);
        shape.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Moon
        shape.setColor(0.95f, 0.95f, 0.8f, 1);
        shape.circle(520, 740, 28);
        shape.setColor(0.04f, 0.04f, 0.12f, 1);
        shape.circle(530, 748, 20);

        // Stars
        int[] starX = {30,80,140,200,260,320,380,440,490,550,
            15,95,160,230,300,370,440,510,570,50,
            120,195,270,345,415,485,555,75,185,395};
        int[] starY = {780,760,770,750,765,755,745,760,775,752,
            720,730,710,725,715,705,718,728,712,690,
            700,685,695,680,692,688,678,660,670,665};
        for (int i = 0; i < starX.length; i++) {
            float twinkle = (float)(Math.sin(System.currentTimeMillis() * 0.002 + i) * 0.3 + 0.7);
            shape.setColor(1, 1, 1, twinkle);
            shape.rect(starX[i], starY[i], i % 3 == 0 ? 3 : 2, i % 3 == 0 ? 3 : 2);
        }

        // Grass strips
        shape.setColor(0.18f, 0.28f, 0.12f, 1);
        shape.rect(0   + shakeX, 0, 100, Constants.SCREEN_HEIGHT);
        shape.rect(500 + shakeX, 0, 100, Constants.SCREEN_HEIGHT);

        // Grass highlight
        shape.setColor(0.22f, 0.38f, 0.15f, 1);
        shape.rect(85  + shakeX, 0, 15, Constants.SCREEN_HEIGHT);
        shape.rect(500 + shakeX, 0, 15, Constants.SCREEN_HEIGHT);

        // Road base
        shape.setColor(0.18f, 0.18f, 0.22f, 1);
        shape.rect(100 + shakeX, 0, 400, Constants.SCREEN_HEIGHT);

        // Kerb lines
        shape.setColor(0.9f, 0.9f, 0.9f, 0.8f);
        shape.rect(100 + shakeX, 0, 4, Constants.SCREEN_HEIGHT);
        shape.rect(496 + shakeX, 0, 4, Constants.SCREEN_HEIGHT);

        // Road dashes
        shape.setColor(0.9f, 0.9f, 0.9f, 0.5f);
        for (float ly = roadOffset; ly < Constants.SCREEN_HEIGHT; ly += 70) {
            shape.rect(295 + shakeX, ly + 10, 10, 42);
        }

        // Yellow centre divider
        shape.setColor(1f, 0.85f, 0.1f, 0.6f);
        for (float ly = roadOffset; ly < Constants.SCREEN_HEIGHT; ly += 70) {
            shape.rect(296 + shakeX, ly + 10, 4, 30);
        }

        // Scenery (on top of grass, beside road)
        for (Scenery s : sceneryLeft)  s.draw(shape);
        for (Scenery s : sceneryRight) s.draw(shape);

        // Game objects + player
        if (!gameOver && !paused) {
            player.draw(shape);
            for (GameObject obj : objects)  obj.drawShape(shape);
            for (Obstacle  obs : obstacles) obs.drawShape(shape);
        }

        // Particles
        for (Particle p : particles) p.draw(shape);

        // Screen flash
        if (screenFlash > 0) {
            shape.setColor(1, 1, 0.8f, screenFlash * 0.35f);
            shape.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }

        // Pause overlay + buttons
        if (paused) {
            shape.setColor(0.1f, 0.1f, 0.2f, 0.85f);
            shape.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            shape.setColor(0.2f, 0.5f, 0.3f, 1);
            shape.rect(resumeX + shakeX, resumeY, resumeW, resumeH);
            shape.setColor(0.5f, 0.2f, 0.2f, 1);
            shape.rect(exitX   + shakeX, exitY,   exitW,   exitH);
        }

        shape.end();

        // ===== SPRITE BATCH (text) =====
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();

        for (GameObject obj : objects)  obj.drawLabel(batch, font);
        for (Obstacle  obs : obstacles) obs.drawLabel(batch, font);
        for (ScorePopup popup : scorePopups) popup.draw(batch, font);

        drawHud(shakeX);

        if (gameOver) drawGameOver();
        if (paused)   drawPause(shakeX);

        batch.end();
    }

    // -------------------------------------------------------------------------
    // HUD
    // -------------------------------------------------------------------------
    private void drawHud(float shakeX) {
        int gh = Gdx.graphics.getHeight();
        int gw = Gdx.graphics.getWidth();

        String scoreText = "SCORE: " + formatScore(score);
        String livesText = buildLivesText();
        String bestText  = "BEST: "  + formatScore(highScore);
        String levelText = "LVL "    + RunForJoy.currentLevel;

        font.getData().setScale(2.0f);

        // Outline pass
        font.setColor(0, 0, 0, 1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, scoreText, 15 + dx,        gh - 12 + dy);
                font.draw(batch, livesText, 15 + dx,        gh - 50 + dy);
                font.draw(batch, bestText,  15 + dx,        gh - 88 + dy);
                font.draw(batch, levelText, gw - 120 + dx,  gh - 12 + dy);
            }
        }

        font.setColor(1f, 0.95f, 0.1f, 1);
        font.draw(batch, scoreText, 15,       gh - 12);
        font.setColor(1f, 0.35f, 0.35f, 1);
        font.draw(batch, livesText, 15,       gh - 50);
        font.setColor(0.4f, 1f, 0.4f, 1);
        font.draw(batch, bestText,  15,       gh - 88);
        font.setColor(0.4f, 0.75f, 1f, 1);
        font.draw(batch, levelText, gw - 120, gh - 12);

        // Combo
        if (combo > 1) {
            String comboText = "x" + combo + " COMBO";
            float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.01) * 0.25 + 0.85);
            font.getData().setScale(1.7f * pulse);
            font.setColor(0, 0, 0, 1);
            font.draw(batch, comboText, gw - 155, gh - 50);
            font.setColor(1f, 0.55f, 0.05f, 1);
            font.draw(batch, comboText, gw - 157, gh - 48);
            font.getData().setScale(2.0f);
        }

        // Power-up indicators
        float py = gh - 130;
        font.getData().setScale(1.5f);
        if (RunForJoy.hasShield) {
            font.setColor(0, 0, 0, 1);
            font.draw(batch, "SHIELD", 17, py + 1);
            font.setColor(0.5f, 0.8f, 1f, 1);
            font.draw(batch, "SHIELD", 15, py);
            py -= 30;
        }
        if (RunForJoy.isDoubleScoreActive) {
            font.setColor(0, 0, 0, 1);
            font.draw(batch, "2x SCORE", 17, py + 1);
            font.setColor(1f, 0.85f, 0.1f, 1);
            font.draw(batch, "2x SCORE", 15, py);
            py -= 30;
        }
        if (RunForJoy.isSlowMoActive) {
            font.setColor(0, 0, 0, 1);
            font.draw(batch, "SLOW MO", 17, py + 1);
            font.setColor(0.5f, 0.95f, 1f, 1);
            font.draw(batch, "SLOW MO", 15, py);
        }
        font.getData().setScale(2.0f);

        // Level-up banner
        if (levelUpTimer > 0) {
            float alpha = Math.min(1f, levelUpTimer * 1.5f);
            float scale = 2.8f + (1f - alpha) * 1.2f;
            font.getData().setScale(scale);
            font.setColor(0, 0, 0, alpha);
            font.draw(batch, "LEVEL " + RunForJoy.currentLevel + "!", gw / 2f - 75, gh / 2f + 55);
            font.setColor(1f, 0.6f, 0.05f, alpha);
            font.draw(batch, "LEVEL " + RunForJoy.currentLevel + "!", gw / 2f - 77, gh / 2f + 57);
            font.getData().setScale(2.0f);
        }
    }

    private String buildLivesText() {
        StringBuilder sb = new StringBuilder("LIVES: ");
        for (int i = 0; i < lives; i++) sb.append("* ");
        return sb.toString().trim();
    }

    private void drawGameOver() {
        float cx = Gdx.graphics.getWidth()  / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;

        font.getData().setScale(3.0f);
        font.setColor(0, 0, 0, 1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, "GAME OVER", cx - 90 + dx, cy + 80 + dy);
            }
        }
        font.setColor(1f, 0.2f, 0.2f, 1);
        font.draw(batch, "GAME OVER", cx - 92, cy + 82);

        font.getData().setScale(2.0f);
        font.setColor(0, 0, 0, 1);
        font.draw(batch, "SCORE: " + formatScore(score),     cx - 80,  cy + 25);
        font.draw(batch, "BEST:  " + formatScore(highScore), cx - 80,  cy - 10);
        font.draw(batch, "PRESS R TO RESTART",               cx - 135, cy - 60);
        font.setColor(1f, 1f, 0.2f, 1);
        font.draw(batch, "SCORE: " + formatScore(score),     cx - 82,  cy + 27);
        font.setColor(0.4f, 1f, 0.4f, 1);
        font.draw(batch, "BEST:  " + formatScore(highScore), cx - 82,  cy - 8);
        font.setColor(1f, 0.8f, 0.1f, 1);
        font.draw(batch, "PRESS R TO RESTART",               cx - 137, cy - 58);
        font.getData().setScale(2.0f);
    }

    private void drawPause(float shakeX) {
        font.getData().setScale(2.8f);
        font.setColor(0, 0, 0, 1);
        font.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - 65,  Constants.SCREEN_HEIGHT / 2f + 100);
        font.setColor(1f, 0.95f, 0.3f, 1);
        font.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - 67,  Constants.SCREEN_HEIGHT / 2f + 102);

        font.getData().setScale(2.0f);
        font.setColor(0, 0, 0, 1);
        font.draw(batch, "RESUME", resumeX + 42, resumeY + 37);
        font.draw(batch, "EXIT",   exitX   + 65, exitY   + 37);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "RESUME", resumeX + 40, resumeY + 35);
        font.setColor(1, 0.6f, 0.6f, 1);
        font.draw(batch, "EXIT",   exitX   + 63, exitY   + 35);
    }

    private String formatScore(long s) {
        return String.valueOf(s);
    }

    // -------------------------------------------------------------------------
    // Dispose
    // -------------------------------------------------------------------------
    public void dispose() {
        shape.dispose();
        batch.dispose();
        font.dispose();
    }

    // -------------------------------------------------------------------------
    // Inner class — ScorePopup
    // -------------------------------------------------------------------------
    private class ScorePopup {
        float x, y, life = 1.0f;
        String text;
        float r, g, b;

        ScorePopup(float x, float y, String text, float r, float g, float b) {
            this.x = x; this.y = y; this.text = text;
            this.r = r; this.g = g; this.b = b;
        }

        boolean update(float delta) {
            y    += delta * 85;
            life -= delta * 1.8f;
            return life > 0;
        }

        void draw(SpriteBatch batch, BitmapFont font) {
            font.setColor(r, g, b, life);
            float scale = 1.4f * (1 + (1 - life) * 0.4f);
            font.getData().setScale(scale);
            font.draw(batch, text, x - 40, y);
            font.getData().setScale(2.0f);
        }
    }
}
