package com.rimon.runforjoy;

import java.util.List;
import java.util.Random;

public class Spawner {
    private float timer = 0;
    private Random rand = new Random();
    private int sinceLastObstacle = 0;
    private int consecutivePositive = 0;

    public void update(float delta, List<GameObject> objects, List<Obstacle> obstacles) {
        timer += delta;
        if (timer >= Constants.SPAWN_DELAY) {
            timer = 0;
            spawnRow(objects, obstacles);
        }
    }

    private void spawnRow(List<GameObject> objects, List<Obstacle> obstacles) {
        sinceLastObstacle++;

        boolean spawnObstacle = (sinceLastObstacle >= 3 && rand.nextFloat() < 0.55f)
            || sinceLastObstacle >= 4;

        // Power up: 7% — always paired with a guaranteed positive, NEVER with a negative
        if (!RunForJoy.isDoubleScoreActive && rand.nextFloat() < 0.07f) {
            spawnPowerUpRow(objects);
            consecutivePositive = 0;
            if (spawnObstacle) {
                spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // Rare row (× and ÷): always one positive × and one positive ÷ — no negatives
        if (rand.nextFloat() < 0.10f) {
            spawnRarePair(objects);
            consecutivePositive++;
            if (spawnObstacle) {
                spawnNormalObstacle(obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // After 2 positive-only rows, force mixed
        if (consecutivePositive >= 2) {
            spawnMixed(objects);
            consecutivePositive = 0;
            if (spawnObstacle) {
                spawnEvilWithGood(objects, obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // 25% both positive, 75% mixed — negatives dominate to force switching
        if (rand.nextFloat() < 0.25f) {
            spawnBothPositive(objects);
            consecutivePositive++;
        } else {
            spawnMixed(objects);   // always exactly one + and one -, never two -
            consecutivePositive = 0;
        }

        if (spawnObstacle) {
            if (rand.nextFloat() < 0.5f) spawnEvilWithGood(objects, obstacles);
            else spawnNormalObstacle(obstacles);
            sinceLastObstacle = 0;
        }
    }

    /** One × and one ÷ — both give points, both positive */
    private void spawnRarePair(List<GameObject> objects) {
        int val1 = rand.nextInt(MathOperation.MULTIPLY.maxValue - MathOperation.MULTIPLY.minValue + 1) + MathOperation.MULTIPLY.minValue;
        int val2 = rand.nextInt(MathOperation.DIVIDE.maxValue   - MathOperation.DIVIDE.minValue   + 1) + MathOperation.DIVIDE.minValue;
        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.MULTIPLY, val1));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.DIVIDE,   val2));
        } else {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.DIVIDE,   val2));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.MULTIPLY, val1));
        }
    }

    /** Two different + values */
    private void spawnBothPositive(List<GameObject> objects) {
        int val1 = randVal(MathOperation.ADD);
        int val2 = randVal(MathOperation.ADD);
        while (val1 == val2) val2 = randVal(MathOperation.ADD);
        objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val1));
        objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val2));
    }

    /** Exactly one + and one - , randomly assigned to lanes — NEVER two negatives */
    private void spawnMixed(List<GameObject> objects) {
        int posVal = randVal(MathOperation.ADD);
        int negVal = randVal(MathOperation.SUBTRACT);
        if (rand.nextBoolean()) {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD,      posVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.SUBTRACT, negVal));
        } else {
            objects.add(new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.SUBTRACT, negVal));
            objects.add(new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD,      posVal));
        }
    }

    /** Power up in one lane, guaranteed + card in the other — never paired with negative */
    private void spawnPowerUpRow(List<GameObject> objects) {
        PowerUpType type = selectPowerUp();
        float lane      = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        float otherLane = (lane == Constants.LEFT_LANE) ? Constants.RIGHT_LANE : Constants.LEFT_LANE;
        objects.add(new GameObject(lane,      Constants.SCREEN_HEIGHT + 50, type));
        objects.add(new GameObject(otherLane, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, randVal(MathOperation.ADD)));
    }

    /** Good card in one lane, obstacle in the other */
    private void spawnEvilWithGood(List<GameObject> objects, List<Obstacle> obstacles) {
        MathOperation goodOp = rand.nextFloat() < 0.6f ? MathOperation.ADD : MathOperation.MULTIPLY;
        int goodVal = randVal(goodOp);
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

    private int randVal(MathOperation op) {
        return rand.nextInt(op.maxValue - op.minValue + 1) + op.minValue;
    }

    private PowerUpType selectPowerUp() {
        float roll = rand.nextFloat();
        for (PowerUpType type : PowerUpType.values()) {
            if (roll < type.spawnChance) return type;
        }
        return PowerUpType.SHIELD;
    }
}
