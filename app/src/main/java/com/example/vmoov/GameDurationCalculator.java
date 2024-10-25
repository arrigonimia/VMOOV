package com.example.vmoov;

public class GameDurationCalculator {

    public static String calculateGameDuration(long startTime, long endTime) {
        long durationMillis = endTime - startTime;

        long hours = (durationMillis / (1000 * 60 * 60)) % 24;
        long minutes = (durationMillis / (1000 * 60)) % 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}
