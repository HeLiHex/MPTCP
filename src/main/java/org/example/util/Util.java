package org.example.util;

import java.util.Random;

public class Util {

    private static final Random random = new Random(1337);

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



}
