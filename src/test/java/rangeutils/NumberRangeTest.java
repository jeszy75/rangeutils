package rangeutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumberRangeTest {

    private NumberRange<Byte> byteRange;
    private NumberRange<Double> doubleRange;
    private NumberRange<Float> floatRange;
    private NumberRange<Integer> integerRange;
    private NumberRange<Long> longRange;
    private NumberRange<Short> shortRange;

    <T extends Number & Comparable<T>> void assertRange(T expectedMin, T expectedMax, NumberRange<T> range) {
        assertEquals(expectedMin, range.getMin());
        assertEquals(expectedMax, range.getMax());
    }

    @BeforeEach
    void setUp() {
        byteRange = NumberRange.of((byte) 15, (byte) 30);
        doubleRange = NumberRange.of(15.0, 30.0);
        floatRange = NumberRange.of(15.0f, 30.0f);
        integerRange = NumberRange.of(15, 30);
        longRange = NumberRange.of(15L, 30L);
        shortRange = NumberRange.of((short) 15, (short) 30);
    }

    @Test
    void empty() {
        assertRange(null, null, NumberRange.<Integer>empty());
    }

    @Test
    void all() {
        assertRange(Byte.MIN_VALUE, Byte.MAX_VALUE, NumberRange.all(Byte.class));
        assertRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, NumberRange.all(Double.class));
        assertRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, NumberRange.all(Float.class));
        assertRange(Integer.MIN_VALUE, Integer.MAX_VALUE, NumberRange.all(Integer.class));
        assertRange(Long.MIN_VALUE, Long.MAX_VALUE, NumberRange.all(Long.class));
        assertRange(Short.MIN_VALUE, Short.MAX_VALUE, NumberRange.all(Short.class));
        assertThrows(IllegalArgumentException.class, () -> NumberRange.all(BigDecimal.class));
        assertThrows(NullPointerException.class, () -> NumberRange.all(null));
    }

    @Test
    void of() {
        assertRange(15, 30, integerRange);
        assertRange(15, 30, NumberRange.of(30, 15));
        assertRange(15, 15, NumberRange.of(15, 15));
        assertRange(15.0, 30.0, doubleRange);
        assertRange(15.0, 30.0, NumberRange.of(30.0, 15.0));
        assertRange(15.0, 15.0, NumberRange.of(15.0, 15.0));
    }

    @Test
    void isEmpty() {
        assertFalse(integerRange.isEmpty());
        assertTrue(NumberRange.<Integer>empty().isEmpty());
    }

    @Test
    void contains() {
        assertTrue(doubleRange.contains(15.0));
        assertTrue(doubleRange.contains(30.0));
        assertTrue(doubleRange.contains(20.0));
        assertFalse(doubleRange.contains(-10.0));
        assertFalse(doubleRange.contains(40.0));
        var smallDoubleRange = NumberRange.of(-1E-10, 1E-10);
        assertTrue(smallDoubleRange.contains(-1E-10));
        assertTrue(smallDoubleRange.contains(0.0));
        assertTrue(smallDoubleRange.contains(1E-10));
        assertTrue(smallDoubleRange.contains(-1E-11));
        assertFalse(smallDoubleRange.contains(1E-9));
        assertTrue(NumberRange.all(Double.class).contains(Double.NEGATIVE_INFINITY));
        assertTrue(NumberRange.all(Double.class).contains(Double.POSITIVE_INFINITY));
        assertFalse(NumberRange.<Double>empty().contains(0.0));
        assertFalse(NumberRange.<Double>empty().contains(Double.NEGATIVE_INFINITY));
        assertFalse(NumberRange.<Double>empty().contains(Double.POSITIVE_INFINITY));
    }

    @Test
    void containsRange() {
        assertTrue(integerRange.containsRange(integerRange));
        assertTrue(integerRange.containsRange(NumberRange.of(15, 20)));
        assertTrue(integerRange.containsRange(NumberRange.of(25, 30)));
        assertTrue(integerRange.containsRange(NumberRange.of(20, 25)));
        assertTrue(integerRange.containsRange(NumberRange.<Integer>empty()));
        assertFalse(integerRange.containsRange(NumberRange.of(-100, 20)));
        assertFalse(integerRange.containsRange(NumberRange.of(25, 100)));
        assertFalse(integerRange.containsRange(NumberRange.all(Integer.class)));
        assertTrue(NumberRange.<Integer>empty().containsRange(NumberRange.<Integer>empty()));
    }

    @Test
    void isOverlapping() {
        assertTrue(integerRange.isOverlapping(integerRange));
        assertTrue(integerRange.isOverlapping(NumberRange.of(15, 20)));
        assertTrue(integerRange.isOverlapping(NumberRange.of(25, 30)));
        assertTrue(integerRange.isOverlapping(NumberRange.of(20, 25)));
        assertTrue(integerRange.isOverlapping(NumberRange.of(-100, 20)));
        assertTrue(integerRange.isOverlapping(NumberRange.of(25, 100)));
        assertTrue(integerRange.isOverlapping(NumberRange.of(-100, 100)));
        assertFalse(integerRange.isOverlapping(NumberRange.of(-100, 0)));
        assertFalse(integerRange.isOverlapping(NumberRange.of(40, 100)));
        assertFalse(integerRange.isOverlapping(NumberRange.<Integer>empty()));
        assertFalse(NumberRange.<Integer>empty().isOverlapping(NumberRange.<Integer>empty()));
    }

    @Test
    void isDisjoint() {
        assertFalse(integerRange.isDisjoint(integerRange));
        assertFalse(integerRange.isDisjoint(NumberRange.of(15, 20)));
        assertFalse(integerRange.isDisjoint(NumberRange.of(25, 30)));
        assertFalse(integerRange.isDisjoint(NumberRange.of(20, 25)));
        assertFalse(integerRange.isDisjoint(NumberRange.of(-100, 20)));
        assertFalse(integerRange.isDisjoint(NumberRange.of(25, 100)));
        assertFalse(integerRange.isDisjoint(NumberRange.of(-100, 100)));
        assertTrue(integerRange.isDisjoint(NumberRange.of(-100, 0)));
        assertTrue(integerRange.isDisjoint(NumberRange.of(40, 100)));
        assertTrue(integerRange.isDisjoint(NumberRange.<Integer>empty()));
        assertTrue(NumberRange.<Integer>empty().isDisjoint(NumberRange.<Integer>empty()));
    }

    @Test
    void intersect() {
        assertSame(integerRange, integerRange.intersect(integerRange));
        assertEquals(NumberRange.of(15, 20), integerRange.intersect(NumberRange.of(15, 20)));
        assertEquals(NumberRange.of(25, 30), integerRange.intersect(NumberRange.of(25, 30)));
        assertEquals(NumberRange.of(20, 25), integerRange.intersect(NumberRange.of(20, 25)));
        assertEquals(NumberRange.of(15, 20), integerRange.intersect(NumberRange.of(-100, 20)));
        assertEquals(NumberRange.of(25, 30), integerRange.intersect(NumberRange.of(25, 100)));
        assertEquals(integerRange, integerRange.intersect(NumberRange.of(-100, 100)));
        assertEquals(NumberRange.<Integer>empty(), integerRange.intersect(NumberRange.of(-100, 0)));
        assertEquals(NumberRange.<Integer>empty(), integerRange.intersect(NumberRange.of(40, 100)));
        assertEquals(NumberRange.<Integer>empty(), NumberRange.<Integer>empty().intersect(NumberRange.all(Integer.class)));
    }

    @Test
    void intersect_Varargs() {
        assertEquals(NumberRange.of(20, 25), integerRange.intersect(NumberRange.of(10, 25), NumberRange.of(20, 35)));
        assertEquals(NumberRange.of(20, 25), integerRange.intersect(NumberRange.of(-100, 100), NumberRange.of(20, 25)));
        assertEquals(NumberRange.<Integer>empty(), integerRange.intersect(NumberRange.all(Integer.class), NumberRange.<Integer>empty()));
    }

    @Test
    void clamp() {
        assertEquals(23L, longRange.clamp(23L));
        assertEquals(15L, longRange.clamp(15L));
        assertEquals(30L, longRange.clamp(30L));
        assertEquals(15L, longRange.clamp(-10L));
        assertEquals(30L, longRange.clamp(100L));
        assertThrows(IllegalArgumentException.class, () -> NumberRange.<Long>empty().clamp(0L));
    }

    @Test
    void union() {
        assertSame(doubleRange, doubleRange.union(doubleRange));
        assertRange(15.0, 30.0, doubleRange.union(NumberRange.of(20.0, 25.0)));
        assertRange(-5.0, 30.0, doubleRange.union(NumberRange.of(-5.0, 20.0)));
        assertRange(15.0, 50.0, doubleRange.union(NumberRange.of(25.0, 50.0)));
        assertEquals(doubleRange, doubleRange.union(NumberRange.<Double>empty()));
        assertEquals(NumberRange.all(Double.class), doubleRange.union(NumberRange.all(Double.class)));
        assertThrows(IllegalArgumentException.class, () -> doubleRange.union(NumberRange.of(-10.0, 5.0)));
        assertThrows(IllegalArgumentException.class, () -> doubleRange.union(NumberRange.of(40.0, 50.0)));
    }

    @Test
    void union_Varargs() {
        assertRange(15.0f, 50.0f, floatRange.union(NumberRange.of(25.0f, 40.0f), NumberRange.of(35.0f, 50.0f)));
        assertThrows(IllegalArgumentException.class, () -> floatRange.union(NumberRange.of(25.0f, 40.0f), NumberRange.of(-5.0f, 10.0f)));
    }

    @Test
    void testEquals() {
        assertTrue(integerRange.equals(integerRange));
        assertTrue(integerRange.equals(NumberRange.of(15, 30)));
        assertFalse(integerRange.equals(NumberRange.of(-100, 100)));
        assertFalse(integerRange.equals(NumberRange.<Integer>empty()));
        assertFalse(integerRange.equals(doubleRange));
        assertFalse(integerRange.equals(null));
        assertFalse(integerRange.equals("Hello, World!"));
    }

    @Test
    void testHashCode() {
        assertTrue(doubleRange.hashCode() == doubleRange.hashCode());
        assertTrue(doubleRange.hashCode() == doubleRange.of(15.0, 30.0).hashCode());
        assertFalse(doubleRange.hashCode() == NumberRange.all(Double.class).hashCode());
    }

    @Test
    void testToString() {
        assertEquals("[15,30]", integerRange.toString());
        assertEquals("[15.0,30.0]", doubleRange.toString());
        assertEquals("[EMPTY]", NumberRange.<Double>empty().toString());
    }

}
