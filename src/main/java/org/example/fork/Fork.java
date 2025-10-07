package org.example.fork;

import java.util.concurrent.locks.ReentrantLock;

// Just fork
public class Fork implements Comparable<Fork> {
    int id;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Fork(int id) {
        this.id = id;
    }
    public int getId() { return id; }

    @Override
    public int compareTo(Fork other) {
        return Integer.compare(this.id, other.id);
    }
}
