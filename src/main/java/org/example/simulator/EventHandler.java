package org.example.simulator;

import org.example.simulator.events.Event;
import java.util.PriorityQueue;
import java.util.Queue;

public class EventHandler {

    private final Queue<Event> events;
    //private static final Statistics STATISTICS = new Statistics();

    public EventHandler(){
        this.events = new PriorityQueue<>();

    }

    public void addEvent(Event event){
        this.events.add(event);
    }

    public void run(){
        while(true){
            //System.out.println(this.events.size());
            Event event = this.events.poll();
            if (event == null){
                if (!this.events.isEmpty()) throw new IllegalStateException("get null event when events queue are nonempty!");
                //System.out.println(STATISTICS.toString());
                return;
            }
            //System.out.println(event.getClass().getSimpleName());
            event.run();
            event.generateNextEvent(this.events);
        }
    }


}
