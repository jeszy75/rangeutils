package rangeutils;

/**
 * Represents a closed range of integers of type {@code int}.
 */
public class IntRange {

    /**
     * Represents the empty range.
     */
    public static final IntRange EMPTY = new IntRange();

    /**
     * Represents the range of all integers.
     */
    public static final IntRange ALL = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);

    private final int min;
    private final int max;


    // Private constructor to create the empty range.
    private IntRange() {
        this(Integer.MAX_VALUE, Integer.MIN_VALUE);
    }

    private IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * {@return the inclusive lower bound of the range}
     */
    public int getMin()  {
        return min;
    }

    /**
     * {@return the inclusive upper bound of the range}
     */
    public int getMax() {
        return max;
    }

    /**
     * Creates an {@code IntRange} object with the specified inclusive lower and
     * upper bounds. If {@code max} is less than {@code min}, their values are
     * interchanged to ensure that the upper bound is greater or equal to the
     * lower bound.
     *
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @return an {@code IntRange} object, not null
     */
    public static IntRange of(int min, int max) {
        if (min <= max) {
            return new IntRange(min, max);
        } else {
            return new IntRange(max, min);
        }
    }

    /**
     * {@return whether the range is empty}
     */
    public boolean isEmpty() {
        return min == Integer.MAX_VALUE && max == Integer.MIN_VALUE;
    }

    /**
     * {@return whether the range contains the value specified}
     *
     * @param value the value to be tested
     */
    public boolean contains(int value) {
        return min <= value && value <= max;
    }

    /**
     * {@return whether the range contains the range specified}
     *
     * @param range the range to be tested
     */
    public boolean containsRange(IntRange range) {
        return contains(range.getMin()) && contains(range.getMax());
    }

    /**
     * {@return whether the range overlaps with the range specified}
     *
     * @param range the range to be tested
     */
    public boolean isOverlapping(IntRange range) {
        return contains(range.getMin()) || contains(range.getMax())
                || range.contains(min) || range.contains(max);
    }

    /**
     * {@return whether the range is disjoint with the range specified}
     *
     * @param range the range to be tested
     */
    public boolean isDisjoint(IntRange range) {
        return !isOverlapping(range);
    }

    /**
     * {@return the intersection of the range with the range specified}
     *
     * @param range the range to be intersected with the range
     */
    public IntRange intersect(IntRange range) {
        if (this == range) {
            return this;
        }
        if (isOverlapping(range)) {
            return new IntRange(Integer.max(min, range.getMin()), Integer.min(max, range.getMax()));
        }
        return EMPTY;
    }

    /**
     * {@return the intersection of the range with the ranges specified}
     *
     * @param ranges the ranges to be intersected with the range
     */
    public IntRange intersect(IntRange... ranges) {
        var result = this;
        for (var range : ranges) {
            result = result.intersect(range);
            if (result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = 5;
        result = 17 * result + min;
        result = 17 * result + max;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IntRange range) && min == range.getMin() && max == range.getMax();
    }

    @Override
    public String toString() {
        return isEmpty() ? "[EMPTY]" : String.format("[%d,%d]", min, max);
    }

}
