package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlikTest {

    @Test
    public void testSameWithSmallIntegers() {
        assertTrue(Flik.isSameNumber(1, 1));
        assertTrue(Flik.isSameNumber(100, 100));
    }

    @Test
    public void testSameWithLargeIntegers() {
        assertTrue(Flik.isSameNumber(128, 128)); // suspect value!
        assertFalse(Flik.isSameNumber(128, 129));
    }
}
