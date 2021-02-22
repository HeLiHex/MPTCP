package org.example.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class UtilTest {


    @Test
    public void getGaussianIsStable(){
        ArrayList<Double> doubles = new ArrayList<>();
        Util.setSeed(1337);

        int arraySize = 10000;
        for (int i = 0; i < arraySize; i++) {
            doubles.add(Util.getNextGaussian());
        }

        Assert.assertEquals(doubles.size(), arraySize);

        Util.setSeed(1337);
        for (double gaussian : doubles){
            Assert.assertEquals(gaussian, Util.getNextGaussian(), 0);
        }
    }


    @Test
    public void getNextRandomIsStable(){
        ArrayList<Integer> ints = new ArrayList<>();
        Util.setSeed(1337);

        int arraySize = 10000;
        for (int i = 0; i < arraySize; i++) {
            ints.add(Util.getNextRandomInt());
        }

        Assert.assertEquals(ints.size(), arraySize);

        Util.setSeed(1337);
        for (double rand : ints){
            Assert.assertEquals(rand, Util.getNextRandomInt(), 0);
        }
    }


    @Test
    public void getNextRandomWithBoundIsStable(){
        ArrayList<Integer> ints = new ArrayList<>();
        int bound = 100;
        Util.setSeed(1337);

        int arraySize = 10000;
        for (int i = 0; i < arraySize; i++) {
            ints.add(Util.getNextRandomInt(bound));
        }

        Assert.assertEquals(ints.size(), arraySize);

        Util.setSeed(1337);
        for (double rand : ints){
            Assert.assertEquals(rand, Util.getNextRandomInt(bound), 0);
        }
    }


    @Test
    public void allIsStable(){
        ArrayList<Integer> ints = new ArrayList<>();
        ArrayList<Integer> boundedInts = new ArrayList<>();
        ArrayList<Double> doubles = new ArrayList<>();
        final int bound = 100;
        Util.setSeed(1337);

        int arraySize = 10000;
        for (int i = 0; i < arraySize; i++) {
            ints.add(Util.getNextRandomInt());
            boundedInts.add(Util.getNextRandomInt(bound));
            doubles.add(Util.getNextGaussian());
        }

        Assert.assertEquals(ints.size(), arraySize);
        Assert.assertEquals(boundedInts.size(), arraySize);
        Assert.assertEquals(doubles.size(), arraySize);

        Util.setSeed(1337);
        for (int i = 0; i < arraySize; i++) {
            Assert.assertEquals(ints.get(i), Util.getNextRandomInt(), 0);
            Assert.assertEquals(boundedInts.get(i), Util.getNextRandomInt(bound), 0);
            Assert.assertEquals(doubles.get(i), Util.getNextGaussian(), 0);
        }
    }


}
