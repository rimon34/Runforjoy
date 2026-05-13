package com.rimon.runforjoy;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Spawner {
    private float timer = 0;
    private Random rand = new Random();
    private int sinceLastObstacle = 0;
    private int consecutivePositive = 0;
    private int rowIdCounter = 0; // unique ID for each spawned row

    public void update(float delta, List<GameObject> objects, List<Obstacle> obstacles) {
        timer += delta;
        if (timer >= Constants.SPAWN_DELAY) {
            timer = 0;
            spawnRow(objects, obstacles);
        }
    }

    private int nextRowId() {
        return ++rowIdCounter;
    }

    private void spawnRow(List<GameObject> objects, List<Obstacle> obstacles) {
        sinceLastObstacle++;

        boolean spawnObstacle = (sinceLastObstacle >= 3 && rand.nextFloat() < 0.55f)
                || sinceLastObstacle >= 4;

        // Power up: 7% — always paired with a + card, never with a - or obstacle
        if (!RunForJoy.isDoubleScoreActive && rand.nextFloat() < 0.07f) {
            spawnPowerUpRow(objects);
            consecutivePositive = 0;
            // No obstacle on power up rows — keep it clean
            return;
        }

        // After 2 positive-only rows, force mixed
        if (consecutivePositive >= 2) {
            spawnMixed(objects);
            consecutivePositive = 0;
            if (spawnObstacle) {
                // Obstacle always paired with a + card, never -
                spawnObstacleWithPositive(objects, obstacles);
                sinceLastObstacle = 0;
            }
            return;
        }

        // 25% both positive, 75% mixed
        if (rand.nextFloat() < 0.25f) {
            spawnBothPositive(objects);
            consecutivePositive++;
        } else {
            spawnMixed(objects);
            consecutivePositive = 0;
        }

        if (spawnObstacle) {
            spawnObstacleWithPositive(objects, obstacles);
            sinceLastObstacle = 0;
        }
    }

    /** Two different + values, same row ID so only one can be collected */
    private void spawnBothPositive(List<GameObject> objects) {
        int rowId = nextRowId();
        int val1 = randVal(MathOperation.ADD);
        int val2 = randVal(MathOperation.ADD);
        while (val1 == val2) val2 = randVal(MathOperation.ADD);
        GameObject a = new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val1);
        GameObject b = new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, val2);
        a.rowId = rowId;
        b.rowId = rowId;
        objects.add(a);
        objects.add(b);
    }

    /** Exactly one + and one -, same row ID — never two negatives */
    private void spawnMixed(List<GameObject> objects) {
        int rowId = nextRowId();
        int posVal = randVal(MathOperation.ADD);
        int negVal = randVal(MathOperation.SUBTRACT);
        GameObject pos = new GameObject(Constants.LEFT_LANE,  Constants.SCREEN_HEIGHT + 50, MathOperation.ADD,      posVal);
        GameObject neg = new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.SUBTRACT, negVal);
        pos.rowId = rowId;
        neg.rowId = rowId;
        if (rand.nextBoolean()) {
            pos.x = Constants.LEFT_LANE;
            neg.x = Constants.RIGHT_LANE;
        } else {
            pos.x = Constants.RIGHT_LANE;
            neg.x = Constants.LEFT_LANE;
        }
        objects.add(pos);
        objects.add(neg);
    }

    /** Obstacle in one lane, guaranteed + card in the other — never - with obstacle */
    private void spawnObstacleWithPositive(List<GameObject> objects, List<Obstacle> obstacles) {
        int rowId = nextRowId();
        int posVal = randVal(MathOperation.ADD);
        GameObject pos;
        if (rand.nextBoolean()) {
            pos = new GameObject(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, posVal);
            obstacles.add(new Obstacle(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, ObstacleType.NORMAL));
        } else {
            pos = new GameObject(Constants.RIGHT_LANE, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, posVal);
            obstacles.add(new Obstacle(Constants.LEFT_LANE, Constants.SCREEN_HEIGHT + 50, ObstacleType.NORMAL));
        }
        pos.rowId = rowId;
        objects.add(pos);
    }

    /** Power up in one lane, + card in the other — clean, no negatives, no obstacles */
    private void spawnPowerUpRow(List<GameObject> objects) {
        int rowId = nextRowId();
        PowerUpType type = selectPowerUp();
        float lane      = rand.nextBoolean() ? Constants.LEFT_LANE : Constants.RIGHT_LANE;
        float otherLane = (lane == Constants.LEFT_LANE) ? Constants.RIGHT_LANE : Constants.LEFT_LANE;
        GameObject pu  = new GameObject(lane,      Constants.SCREEN_HEIGHT + 50, type);
        GameObject pos = new GameObject(otherLane, Constants.SCREEN_HEIGHT + 50, MathOperation.ADD, randVal(MathOperation.ADD));
        pu.rowId  = rowId;
        pos.rowId = rowId;
        objects.add(pu);
        objects.add(pos);
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

    /** Remove the row partner of a just-collected object by matching rowId */
    public static void removeRowPartner(List<GameObject> objects, int collectedRowId) {
        Iterator<GameObject> it = objects.iterator();
        while (it.hasNext()) {
            if (it.next().rowId == collectedRowId) {
                it.remove();
                return;
            }
        }
    }
}
