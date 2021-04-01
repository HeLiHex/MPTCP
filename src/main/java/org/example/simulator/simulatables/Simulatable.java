package org.example.simulator.simulatables;

import java.util.List;

public interface Simulatable {

    void start();

    void simulate();

    long delay();

    List<Simulatable> simulatablesToEnqueue();

}
