package org.example.simulator.simulatables;

public class RunSimulatableEvent extends SimulatableEvent{

    public RunSimulatableEvent(Simulatable simulatable) {
        super(simulatable);
    }

    @Override
    public void run() {
        this.getSimulatable().simulate();
    }
}
