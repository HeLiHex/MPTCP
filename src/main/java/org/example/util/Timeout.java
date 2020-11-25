package org.example.util;

import org.example.protocol.AbstractTCP;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Timeout {

    private final Logger logger = Logger.getLogger(Timeout.class.getName());
    private int duration;
    private TimerState state;

    public Timeout(int duration) {
        this.duration = duration;
        this.state = TimerState.waiting;
    }

    public void decrement(){
        if (this.isActive()){
            this.duration--;
            if (duration == 0) this.deactivate();
        }
    }

    public void start(){
        if (this.isActive()) logger.log(Level.WARNING, "The timer is already active");
        this.state = TimerState.active;
    }

    protected void deactivate(){
        this.state = TimerState.done;
    }

    public boolean isActive() {
        return this.state == TimerState.active;
    }

    public boolean isDone(){
        return this.state == TimerState.done;
    }

    public int getTimeoutValue(){
        return this.duration;
    }



}
