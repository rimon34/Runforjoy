package com.rimon.runforjoy;

public class Constants {
    public static final int   SCREEN_WIDTH          = 600;
    public static final int   SCREEN_HEIGHT         = 800;
    public static final float LEFT_LANE             = 200f;
    public static final float RIGHT_LANE            = 400f;
    public static final float PLAYER_Y              = 100f;
    public static final int   START_LIVES           = 3;

    // Level up every 100 points
    public static final int   LEVEL_UP_SCORE        = 100;

    // Speed: starts at 300 (noticeably faster than the crawling 220),
    // rises by 12 per level, hard cap at 480.
    // Level 1=300, 2=312, 3=324 ... caps around level 16
    public static final float BASE_OBJECT_SPEED     = 300f;
    public static final float MAX_OBJECT_SPEED      = 480f;

    // Spawn delay: starts at 1.3s (tighter than 1.6s),
    // shrinks by 0.03s per level, minimum 0.65s
    public static final float BASE_SPAWN_DELAY      = 1.3f;
    public static float       SPAWN_DELAY           = BASE_SPAWN_DELAY;

    public static final float CATCH_X_MARGIN        = 50f;
    public static final float CATCH_Y_TOP           = 150f;
    public static final float CATCH_Y_BOTTOM        = 50f;

    public static final float SHIELD_DURATION       = 5f;
    public static final float DOUBLE_SCORE_DURATION = 4f;
    public static final float SLOW_MO_DURATION      = 3f;
    public static final float INVINCIBLE_DURATION   = 1.5f;

    public static float OBJECT_SPEED = BASE_OBJECT_SPEED;
}
