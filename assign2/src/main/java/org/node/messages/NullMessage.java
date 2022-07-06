package org.node.messages;

public class NullMessage extends Message {
    public NullMessage() {
        super();
    }

    @Override
    public void run() {
        // System.out.println("Ignored");
    }
}
