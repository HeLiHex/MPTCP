package org.example.data;

public class Message implements Payload {

    private String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    @Override
    public int size() {
        return this.msg.length();
    }

    @Override
    public String toString() {
        return this.msg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Payload) return this.toString().equals(obj.toString());
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
