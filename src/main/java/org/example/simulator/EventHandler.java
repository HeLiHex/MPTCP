package org.example.simulator;

import org.example.simulator.events.Event;
import java.util.PriorityQueue;
import java.util.Queue;

public class EventHandler {

    private final Queue<Event> events;
    private final Statistics STATISTICS = new Statistics();

    public EventHandler(){
        this.events = new PriorityQueue<>();
        STATISTICS.reset();
    }

    public void addEvent(Event event){
        this.events.add(event);
    }

    public int getNumberOfEvents(){
        return this.events.size();
    }

    public void run(){
        while(true){
            Event event = this.events.poll();
            if (event == null){
                if (!this.events.isEmpty()) throw new IllegalStateException("get null event when events queue are nonempty!");
                break;
            }
            event.run();
            event.generateNextEvent(this.events);
        }
        System.out.println(STATISTICS.toString());
    }
}
