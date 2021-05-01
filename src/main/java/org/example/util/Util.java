package org.example.util;

import org.example.simulator.events.Event;

import java.util.Random;

public class Util {

    private static final Random random = new Random(1337);
    private static long time = 0;
    private Util() {
        throw new IllegalStateException("Utility class");
    }

    public static void setSeed(int seed) {
        random.setSeed(seed);
    }

    public static double getNextRandomDouble() {
        return random.nextDouble();
    }

    public static int getNextRandomInt() {
        return random.nextInt();
    }

    public static int getNextRandomInt(int bound) {
        return random.nextInt(bound);
    }

    public static double getNextGaussian() {
        return random.nextGaussian();
    }

    public static void tickTime(Event event) {
        time = event.getInstant();
    }

    public static long getTime() {
        return time;
    }

    public static long seeTime() {
        return time;
    }

    public static void resetTime() {
        time = 0;
    }


}
