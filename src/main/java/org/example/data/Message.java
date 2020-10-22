package org.example.data;

public class Message implements Payload{

    private String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

}
