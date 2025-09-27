package org.example.utils;

import org.example.fork.Fork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexFetcherTest {

    private IndexFetcher indexFetcher;
    private ConcurrentHashMap<Integer, Fork> forks;

    @BeforeEach
    void setUp() {
        indexFetcher = new IndexFetcher();
        forks = new ConcurrentHashMap<>();
        forks.put(0, new Fork(0));
        forks.put(1, new Fork(1));
        forks.put(2, new Fork(2));
    }

    @Test
    void fetch_withValidIndex_returnsSameIndex() {
        assertEquals(0, indexFetcher.fetch(forks, 0));
        assertEquals(1, indexFetcher.fetch(forks, 1));
        assertEquals(2, indexFetcher.fetch(forks, 2));
    }

    @Test
    void fetch_withNegativeIndex_returnsLastIndex() {
        assertEquals(2, indexFetcher.fetch(forks, -1));
        assertEquals(2, indexFetcher.fetch(forks, -10));
    }

    @Test
    void fetch_withIndexTooLarge_returnsFirstIndex() {
        assertEquals(0, indexFetcher.fetch(forks, 3));
        assertEquals(0, indexFetcher.fetch(forks, 10));
    }

    @Test
    void fetch_withEmptyMap_returnsZero() {
        ConcurrentHashMap<Integer, Fork> empty = new ConcurrentHashMap<>();
        assertEquals(0, indexFetcher.fetch(empty, -1));
        assertEquals(0, indexFetcher.fetch(empty, 5));
    }
}
