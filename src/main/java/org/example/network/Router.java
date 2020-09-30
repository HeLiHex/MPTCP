package org.example.network;

public class Router extends Routable {

    public Router() {
        super();
    }

    @Override
    public void run() {
        while (true){
            if (!this.inputBuffer.isEmpty()){
                this.route(this.inputBuffer.poll());
            }
        }
    }



}
