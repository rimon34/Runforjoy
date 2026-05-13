package com.rimon.runforjoy;

public class Constants {
    public static final int SCREEN_WIDTH        = 600;
    public static final int SCREEN_HEIGHT       = 800;
    public static final float LEFT_LANE         = 200f;
    public static final float RIGHT_LANE        = 400f;
    public static final float PLAYER_Y          = 100f;
    public static final int START_LIVES         = 3;
    public static final int LEVEL_UP_SCORE      = 150;       // LV1=150, LV2=300, LV3=450...
    public static final float BASE_SPAWN_DELAY  = 1.6f;      // slower, more readable
    public static float SPAWN_DELAY             = BASE_SPAWN_DELAY;
    public static final float BASE_OBJECT_SPEED = 220f;      // noticeably slower start
    public static final float MAX_OBJECT_SPEED  = 480f;      // moderate cap
    public static final float CATCH_X_MARGIN    = 50f;
    public static final float CATCH_Y_TOP       = 150f;
    public static final float CATCH_Y_BOTTOM    = 50f;
    public static final float SHIELD_DURATION       = 5f;
    public static final float DOUBLE_SCORE_DURATION = 4f;
    public static final float SLOW_MO_DURATION      = 3f;
    public static final float INVINCIBLE_DURATION   = 1.5f;

    public static float OBJECT_SPEED = BASE_OBJECT_SPEED;
}
