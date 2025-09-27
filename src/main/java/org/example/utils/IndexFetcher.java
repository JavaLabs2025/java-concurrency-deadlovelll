package org.example.utils;

import org.example.fork.Fork;

import java.util.concurrent.ConcurrentHashMap;

public class IndexFetcher {
    public int fetch(ConcurrentHashMap<Integer, Fork> forks, int id) {
        // Getting the forks hashmap size
        int size = forks.size();
        // if size is zero return zero
        if (size == 0) {
            return 0;
        }
        // If id is 0, return the size - 1,
        // This way, we're imitating fork
        // that in left from us
        if (id < 0) {
            id = size - 1;
        } else if (id >= size) {
            // Otherwise, setting it to 0,
            // Imitating forks in right
            // from us
            id = 0;
        }
        return id;
    }
}