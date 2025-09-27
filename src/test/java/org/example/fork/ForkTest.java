package org.example.fork;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ForkTest {

    @Test
    void testConstructorAndGetter() {
        Fork fork0 = new Fork(0);
        Fork fork1 = new Fork(1);

        assertEquals(0, fork0.getId(), "Fork id should be 0");
        assertEquals(1, fork1.getId(), "Fork id should be 1");
    }
}