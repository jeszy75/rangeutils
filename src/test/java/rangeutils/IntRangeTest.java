package rangeutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntRangeTest {

    private IntRange range;

    void assertIntRange(int expectedMin, int expectedMax, IntRange range) {
        assertAll("IntRange bounds",
                () -> assertEquals(expectedMin, range.getMin()),
                () -> assertEquals(expectedMax, range.getMax())
        );
    }

    @BeforeEach
    void setUp() {
        range = IntRange.of(15, 30);
    }

    @Test
    void testEMPTY() {
        assertIntRange(Integer.MAX_VALUE, Integer.MIN_VALUE, IntRange.EMPTY);
    }

    @Test
    void testALL() {
        assertIntRange(Integer.MIN_VALUE, Integer.MAX_VALUE, IntRange.ALL);
    }

    @Test
    void of() {
        assertIntRange(15, 30, IntRange.of(15, 30));
        assertIntRange(15, 30, IntRange.of(30, 15));
        assertIntRange(15, 15, IntRange.of(15, 15));
    }

    @ParameterizedTest
    @CsvSource({
            "15, 30, 15, 30",
            "30, 15, 15, 30",
            "15, 15, 15, 15"
    })
    void of(int min, int max, int expectedMin, int expectedMax) {
        assertIntRange(expectedMin, expectedMax, IntRange.of(min, max));
    }

    @Test
    void isEmpty() {
        assertFalse(range.isEmpty());
        assertTrue(IntRange.EMPTY.isEmpty());
    }

    @Test
    void contains() {
        assertTrue(range.contains(15));
        assertTrue(range.contains(30));
        assertTrue(range.contains(20));
        assertFalse(range.contains(-10));
        assertFalse(range.contains(40));
        assertTrue(IntRange.ALL.contains(Integer.MIN_VALUE));
        assertTrue(IntRange.ALL.contains(Integer.MAX_VALUE));
        assertFalse(IntRange.EMPTY.contains(0));
        assertFalse(IntRange.EMPTY.contains(Integer.MIN_VALUE));
        assertFalse(IntRange.EMPTY.contains(Integer.MAX_VALUE));
    }

    @Test
    void containsRange() {
        assertTrue(range.containsRange(range));
        assertTrue(range.containsRange(IntRange.of(15, 20)));
        assertTrue(range.containsRange(IntRange.of(25, 30)));
        assertTrue(range.containsRange(IntRange.of(20, 25)));
        assertTrue(range.containsRange(IntRange.EMPTY));
        assertFalse(range.containsRange(IntRange.of(-100, 20)));
        assertFalse(range.containsRange(IntRange.of(25, 100)));
        assertFalse(range.containsRange(IntRange.ALL));
        assertTrue(IntRange.EMPTY.containsRange(IntRange.EMPTY));
    }

    @Test
    void isOverlapping() {
        assertTrue(range.isOverlapping(range));
        assertTrue(range.isOverlapping(IntRange.of(15, 20)));
        assertTrue(range.isOverlapping(IntRange.of(25, 30)));
        assertTrue(range.isOverlapping(IntRange.of(20, 25)));
        assertTrue(range.isOverlapping(IntRange.of(-100, 20)));
        assertTrue(range.isOverlapping(IntRange.of(25, 100)));
        assertTrue(range.isOverlapping(IntRange.of(-100, 100)));
        assertFalse(range.isOverlapping(IntRange.of(-100, 0)));
        assertFalse(range.isOverlapping(IntRange.of(40, 100)));
        assertFalse(range.isOverlapping(IntRange.EMPTY));
        assertFalse(IntRange.EMPTY.isOverlapping(IntRange.EMPTY));
    }

    @Test
    void isDisjoint() {
        assertFalse(range.isDisjoint(range));
        assertFalse(range.isDisjoint(IntRange.of(15, 20)));
        assertFalse(range.isDisjoint(IntRange.of(25, 30)));
        assertFalse(range.isDisjoint(IntRange.of(20, 25)));
        assertFalse(range.isDisjoint(IntRange.of(-100, 20)));
        assertFalse(range.isDisjoint(IntRange.of(25, 100)));
        assertFalse(range.isDisjoint(IntRange.of(-100, 100)));
        assertTrue(range.isDisjoint(IntRange.of(-100, 0)));
        assertTrue(range.isDisjoint(IntRange.of(40, 100)));
        assertTrue(range.isDisjoint(IntRange.EMPTY));
        assertTrue(IntRange.EMPTY.isDisjoint(IntRange.EMPTY));
    }

    @Test
    void intersect() {
        assertSame(range, range.intersect(range));
        assertEquals(IntRange.of(15, 20), range.intersect(IntRange.of(15, 20)));
        assertEquals(IntRange.of(25, 30), range.intersect(IntRange.of(25, 30)));
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(20, 25)));
        assertEquals(IntRange.of(15, 20), range.intersect(IntRange.of(-100, 20)));
        assertEquals(IntRange.of(25, 30), range.intersect(IntRange.of(25, 100)));
        assertEquals(range, range.intersect(IntRange.of(-100, 100)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.of(-100, 0)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.of(40, 100)));
        assertEquals(IntRange.EMPTY, IntRange.EMPTY.intersect(IntRange.ALL));
    }

    @Test
    void intersect_vararg() {
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(10, 25), IntRange.of(20, 35)));
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(-100, 100), IntRange.of(20, 25)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.ALL, IntRange.EMPTY));
    }

    @Test
    void clamp() {
        assertEquals(23, range.clamp(23));
        assertEquals(15, range.clamp(15));
        assertEquals(30, range.clamp(30));
        assertEquals(15, range.clamp(-10));
        assertEquals(30, range.clamp(100));
    }

    @Test
    void union() {
        assertSame(range, range.union(range));
        assertIntRange(15, 30, range.union(IntRange.of(20, 25)));
        assertIntRange(-5, 30,  range.union(IntRange.of(-5, 20)));
        assertIntRange(15, 50, range.union(IntRange.of(25, 50)));
        assertIntRange(10, 30, range.union(IntRange.of(10, 14)));
        assertIntRange(15, 40, range.union(IntRange.of(31, 40)));
        assertThrows(IllegalArgumentException.class, () -> range.union(IntRange.of(-10, 5)));
        assertThrows(IllegalArgumentException.class, () -> range.union(IntRange.of(40, 50)));
    }

    @Test
    void testEquals() {
        assertTrue(range.equals(range));
        assertTrue(range.equals(IntRange.of(15, 30)));
        assertFalse(range.equals(IntRange.of(-100, 100)));
        assertFalse(range.equals(IntRange.EMPTY));
        assertFalse(range.equals(null));
        assertFalse(range.equals("Hello, World!"));
    }

    @Test
    void testHashCode() {
        assertTrue(range.hashCode() == range.hashCode());
        assertTrue(range.hashCode() == IntRange.of(15, 30).hashCode());
        assertFalse(range.hashCode() == IntRange.ALL.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("[15,30]", range.toString());
        assertEquals("[EMPTY]", IntRange.EMPTY.toString());
    }

}
