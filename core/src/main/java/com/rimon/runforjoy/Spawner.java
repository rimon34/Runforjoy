package com.rimon.runforjoy;

import java.util.List;
import java.util.Random;

public class Spawner {
    private float timer = 0;
    private Random rand = new Random();
    private int sinceLastObstacle = 0;

    public void update(float delta, List<GameObject> objects, List<Obstacle> obstacles) {
        timer += delta;
        float delay = Constants.SPAWN_DELAY;

        if (timer >= delay) {
            timer = 0;
            spawnRow(objects, obstacles);
        }
    }

    private void spawnRow(List<GameObject> objects, List<Obstacle> obstacles) {
        sinceLastObstacle++;
        boolean spawnObstacle = (sinceLastObstacle >= 3 && rand.nextFloat() < 0.6f) || sinceLastObstacle >= 5;

        float roll = rand.nextFloat();
        boolean spawnPowerUp = !RunForJoy.isDoubleScoreActive && rand.nextFloat() < 0.08f;

        if (spawnPowerUp) {
            spawnPowerUp(objects);
            if (spawnObstacle && rand.nextBoolean()) {
                spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // Determine spawn type (NO two negatives, NO × and ÷ together)
        boolean trySpawnRare = rand.nextFloat() < 0.12f;

        if (trySpawnRare) {
            if (spawnRarePair(objects)) {
                if (spawnObstacle && rand.nextBoolean()) {
                    spawnNormalObstacle(obstacles);
                    sinceLastObstacle = 0;
                }
                return;
            }
        }

        // Normal spawn - positive positive or mixed
        if (rand.nextFloat() < 0.55f) {
            spawnBothPositive(objects);
        } else {
            spawnMixed(objects);
        }

        if (spawnObstacle) {
            if (rand.nextFloat() < 0.4f) {
                spawnEvilWithGood(objects, obstacles);
            } else {
                spawnNormalObstacle(obstacles);
            }
            sinceLastObstacle = 0;
        }
    }

    private boolean spawnRarePair(List<GameObject> objects) {
        MathOperation op1 = MathOperation.MULTIPLY;
        MathOperation op2 = MathOperation.DIVIDE;

        if (rand.nextBoolean()) {
            int val1 = rand.nextInt(op1.maxValue - op1.minValue + 1) + op1.minValue;
            int val2 = rand.nextInt(op2.maxValue - op2.minValue + 1) + op2.minValue;
            objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, op1, val1));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, op2, val2));
            return true;
        }
        return false;
    }

    private void spawnBothPositive(List<GameObject> objects) {
        MathOperation op1 = MathOperation.ADD;
        MathOperation op2 = MathOperation.ADD;

        int val1 = rand.nextInt(op1.maxValue - op1.minValue + 1) + op1.minValue;
        int val2 = rand.nextInt(op2.maxValue - op2.minValue + 1) + op2.minValue;

        while (val1 == val2) {
            val2 = rand.nextInt(op2.maxValue - op2.minValue + 1) + op2.minValue;
        }

        objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, op1, val1));
        objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, op2, val2));
    }

    private void spawnMixed(List<GameObject> objects) {
        MathOperation positiveOp = MathOperation.ADD;
        MathOperation negativeOp = MathOperation.SUBTRACT;

        int posVal = rand.nextInt(positiveOp.maxValue - positiveOp.minValue + 1) + positiveOp.minValue;
        int negVal = rand.nextInt(negativeOp.maxValue - negativeOp.minValue + 1) + negativeOp.minValue;

        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, positiveOp, posVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, negativeOp, negVal));
        } else {
            objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, negativeOp, negVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, positiveOp, posVal));
        }
    }

    private void spawnEvilWithGood(List<GameObject> objects, List<Obstacle> obstacles) {
        MathOperation goodOp = rand.nextFloat() < 0.6f ? MathOperation.ADD : MathOperation.MULTIPLY;
        int goodVal = rand.nextInt(goodOp.maxValue - goodOp.minValue + 1) + goodOp.minValue;

        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, goodOp, goodVal));
            obstacles.add(new Obstacle(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, ObstacleType.EVIL));
        } else {
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, goodOp, goodVal));
            obstacles.add(new Obstacle(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, ObstacleType.EVIL));
        }
    }

    private void spawnNormalObstacle(List<Obstacle> obstacles) {
        float lane = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        obstacles.add(new Obstacle(lane, Constants.SCREEN_HEIGHT + 50, ObstacleType.NORMAL));
    }

    private void spawnPowerUp(List<GameObject> objects) {
        PowerUpType type = selectPowerUp();
        float lane = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        float otherLane = lane == Constants.LEFT_LANE ? Constants.RIGHT_LANE : Constants.LEFT_LANE;

        objects.add(new GameObject(lane, Constants.SCREEN_HEIGHT + 50, type));

        MathOperation dummyOp = MathOperation.ADD;
        int dummyVal = rand.nextInt(5) + 1;
        objects.add(new GameObject(otherLane, Constants.SCREEN_HEIGHT + 50, dummyOp, dummyVal));
    }

    private PowerUpType selectPowerUp() {
        float roll = rand.nextFloat();
        for (PowerUpType type : PowerUpType.values()) {
            if (roll < type.spawnChance) return type;
        }
        return PowerUpType.SHIELD;
    }
}
