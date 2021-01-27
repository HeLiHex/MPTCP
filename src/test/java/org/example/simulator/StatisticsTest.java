package org.example.simulator;

import org.junit.Test;

public class StatisticsTest {


    @Test
    public void noExceptionCallingToStringTest(){
        Statistics statistics = new Statistics();
        System.out.println(statistics.toString());
    }



}
