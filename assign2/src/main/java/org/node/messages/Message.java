package org.node.messages;

import org.node.Node;

public abstract class Message implements Runnable {
    protected Node node;

    public Message() {
        this.node = null;
    }

    protected Message(Node node) {
        this.node = node;
    }

    public abstract void run();
}
