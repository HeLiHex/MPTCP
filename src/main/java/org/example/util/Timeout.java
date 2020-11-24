package org.example.util;

import org.example.protocol.AbstractTCP;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Timeout {

    private final Logger logger = Logger.getLogger(Timeout.class.getName());

    private boolean active;
    private int duration;

    public Timeout(int duration) {
        this.active = false;
        this.duration = duration;
    }

    public boolean alarm(){
        this.decrement();
        return isActive();
    }

    private void decrement(){
        if (this.isActive()){
            this.duration--;
            if (duration == 0) this.deactivate();
        }
    }

    public void start(){
        if (this.isActive()) logger.log(Level.WARNING, "The timer is already active");
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

    protected void deactivate(){
        this.active = false;
    }



}
