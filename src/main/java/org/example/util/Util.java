package org.example.util;

import org.example.simulator.events.Event;

import java.util.Random;

public class Util {

    private static final Random random = new Random(1337);
    private static int time = 0;

    public static void setSeed(int seed){
        random.setSeed(seed);
    }

    public static int getNextRandomInt(){
        return random.nextInt();
    }

    public static int getNextRandomInt(int bound){
        return random.nextInt(bound);
    }

    public static double getNextGaussian(){
        return random.nextGaussian();
    }

    public static void tickTime(Event event){
        time = event.getInitInstant() + 1;
    }

    public static int getTime(){
        return time;
    }

    public static void resetTime(){
        time = 0;
    }


}
