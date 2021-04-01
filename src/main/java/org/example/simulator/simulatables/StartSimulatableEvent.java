package org.example.simulator.simulatables;

public class StartSimulatableEvent extends SimulatableEvent{

    public StartSimulatableEvent(Simulatable simulatable) {
        super(simulatable);
    }

    @Override
    public void run() {
        this.getSimulatable().start();
    }
}
