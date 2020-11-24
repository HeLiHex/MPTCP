package org.example.util;

public class Timeout {

    private boolean active;
    private int duration;

    public Timeout(int duration) {
        this.active = false;
        this.duration = duration;
    }

    public void decrement(){
        if (active){
            this.duration--;
            if (duration == 0) this.active = false;
        }
    }

    public void start(){
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }



}
