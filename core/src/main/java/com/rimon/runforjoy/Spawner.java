package com.rimon.runforjoy;

import java.util.List;
import java.util.Random;

public class Spawner {
    private float timer = 0;
    private Random rand = new Random();
    private int sinceLastObstacle = 0;
    private int consecutivePositive = 0; // track back-to-back positive-only rows

    public void update(float delta, List<GameObject> objects, List<Obstacle> obstacles) {
        timer += delta;
        if (timer >= Constants.SPAWN_DELAY) {
            timer = 0;
            spawnRow(objects, obstacles);
        }
    }

    private void spawnRow(List<GameObject> objects, List<Obstacle> obstacles) {
        sinceLastObstacle++;

        boolean spawnObstacle = (sinceLastObstacle >= 3 && rand.nextFloat() < 0.6f)
            || sinceLastObstacle >= 5;

        // Power-up check (only when double score is not active)
        boolean spawnPowerUp = !RunForJoy.isDoubleScoreActive && rand.nextFloat() < 0.08f;

        if (spawnPowerUp) {
            spawnPowerUp(objects);
            consecutivePositive = 0;
            if (spawnObstacle && rand.nextBoolean()) {
                spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // Rare pair (× and ÷)
        boolean trySpawnRare = rand.nextFloat() < 0.12f;
        if (trySpawnRare && spawnRarePair(objects)) {
            consecutivePositive = 0;
            if (spawnObstacle && rand.nextBoolean()) {
                spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // After 2 positive-only rows in a row, force a mixed or obstacle row
        if (consecutivePositive >= 2) {
            spawnMixed(objects);
            consecutivePositive = 0;
            if (spawnObstacle) {
                if (rand.nextFloat() < 0.4f) spawnEvilWithGood(objects, obstacles);
                else spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // Normal spawn decision: 45% both positive, 55% mixed
        if (rand.nextFloat() < 0.45f) {
            spawnBothPositive(objects);
            consecutivePositive++;
        } else {
            spawnMixed(objects);
            consecutivePositive = 0;
        }

        if (spawnObstacle) {
            if (rand.nextFloat() < 0.4f) spawnEvilWithGood(objects, obstacles);
            else spawnNormalObstacle(obstacles);
            sinceLastObstacle = 0;
        }
    }

    private boolean spawnRarePair(List<GameObject> objects) {
        if (rand.nextBoolean()) {
            MathOperation op1 = MathOperation.MULTIPLY;
            MathOperation op2 = MathOperation.DIVIDE;
            int val1 = rand.nextInt(op1.maxValue - op1.minValue + 1) + op1.minValue;
            int val2 = rand.nextInt(op2.maxValue - op2.minValue + 1) + op2.minValue;
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, op1, val1));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, op2, val2));
            return true;
        }
        return false;
    }

    private void spawnBothPositive(List<GameObject> objects) {
        int val1 = rand.nextInt(MathOperation.ADD.maxValue - MathOperation.ADD.minValue + 1) + MathOperation.ADD.minValue;
        int val2 = rand.nextInt(MathOperation.ADD.maxValue - MathOperation.ADD.minValue + 1) + MathOperation.ADD.minValue;
        while (val1 == val2) {
            val2 = rand.nextInt(MathOperation.ADD.maxValue - MathOperation.ADD.minValue + 1) + MathOperation.ADD.minValue;
        }
        objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val1));
        objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val2));
    }

    private void spawnMixed(List<GameObject> objects) {
        int posVal = rand.nextInt(MathOperation.ADD.maxValue      - MathOperation.ADD.minValue      + 1) + MathOperation.ADD.minValue;
        int negVal = rand.nextInt(MathOperation.SUBTRACT.maxValue - MathOperation.SUBTRACT.minValue + 1) + MathOperation.SUBTRACT.minValue;
        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD,      posVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.SUBTRACT, negVal));
        } else {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.SUBTRACT, negVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD,      posVal));
        }
    }

    private void spawnEvilWithGood(List<GameObject> objects, List<Obstacle> obstacles) {
        MathOperation goodOp = rand.nextFloat() < 0.6f ? MathOperation.ADD : MathOperation.MULTIPLY;
        int goodVal = rand.nextInt(goodOp.maxValue - goodOp.minValue + 1) + goodOp.minValue;
        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, goodOp, goodVal));
            obstacles.add(new Obstacle(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, ObstacleType.EVIL));
        } else {
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, goodOp, goodVal));
            obstacles.add(new Obstacle(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, ObstacleType.EVIL));
        }
    }

    private void spawnNormalObstacle(List<Obstacle> obstacles) {
        float lane = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        obstacles.add(new Obstacle(lane, Constants.SCREEN_HEIGHT + 50, ObstacleType.NORMAL));
    }

    private void spawnPowerUp(List<GameObject> objects) {
        PowerUpType type = selectPowerUp();
        float lane = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        float otherLane = (lane == Constants.LEFT_LANE) ? Constants.RIGHT_LANE : Constants.LEFT_LANE;
        objects.add(new GameObject(lane, Constants.SCREEN_HEIGHT + 50, type));
        int dummyVal = rand.nextInt(5) + 1;
        objects.add(new GameObject(otherLane, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, dummyVal));
    }

    private PowerUpType selectPowerUp() {
        float roll = rand.nextFloat();
        for (PowerUpType type : PowerUpType.values()) {
            if (roll < type.spawnChance) return type;
        }
        return PowerUpType.SHIELD;
    }
}
