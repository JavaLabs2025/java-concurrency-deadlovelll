package org.example.fork;

import java.util.concurrent.locks.ReentrantLock;

// Just fork
public class Fork {
    int id;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Fork(int id) {
        this.id = id;
    }
    public int getId() { return id; }
    public void lock() { lock.lock(); }
    public void unlock() { lock.unlock(); }
}
