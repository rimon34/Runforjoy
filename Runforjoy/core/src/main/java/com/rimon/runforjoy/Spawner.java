package com.rimon.runforjoy;

import java.util.List;
import java.util.Random;

public class Spawner {
    private float timer = 0, delay;
    private Random rand = new Random();

    public Spawner(float delay) { this.delay = delay; }

    public void update(float delta, List<GameObject> objects, List<Obstacle> obstacles) {
        timer += delta;
        // Gradual speed ramp
        if (Constants.OBJECT_SPEED < 900) {
            Constants.OBJECT_SPEED += delta * 2.5f;
        }

        if (timer >= delay) {
            timer = 0;
            spawnRow(objects, obstacles);
        }
    }

    private void spawnRow(List<GameObject> objects, List<Obstacle> obstacles) {
        float roll = rand.nextFloat();

        if (roll < 0.45f) { // 45% chance for Hazard (Obstacle + Math)
            boolean leftObs = rand.nextBoolean();
            float obsX = leftObs ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
            float mathX = leftObs ? Constants.RIGHT_LANE : Constants.LEFT_LANE;

            obstacles.add(new Obstacle(obsX, Constants.SCREEN_HEIGHT + 50));
            objects.add(new GameObject(mathX, Constants.SCREEN_HEIGHT + 50, "+", rand.nextInt(10) + 5, false));
        } else { // 55% chance for Choice (Two Math Blocks)
            boolean fakeLeft = rand.nextFloat() < 0.25f; // Option B trigger

            // Left block
            objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, "+", rand.nextInt(15) + 5, fakeLeft));
            // Right block
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, "-", rand.nextInt(8) + 2, false));
        }
    }
}
