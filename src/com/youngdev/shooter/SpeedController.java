package com.youngdev.shooter;

public class SpeedController {
    private static float speed = 1f;
    private static float multiplier = 1f;

    public static float getMultiplier() {
        return multiplier;
    }

    public static float getSpeed() {
        return speed;
    }

    public static void setSpeed(float newSpeed) {
        speed = newSpeed;
    }

    public static void multiply(float multiplier) {
        SpeedController.multiplier *= multiplier;
    }

    public static void resetMultipliers() {
        multiplier = 1f;
    }

    public static float calcSpeed() {
        return speed*multiplier;
    }
}
