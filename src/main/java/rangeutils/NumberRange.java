package rangeutils;

import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a closed range of numbers. Currently, the class supports the
 * {@link Byte}, {@link Double}, {@link Float}, {@link Integer}, {@link Long},
 * and {@link Short} types.
 *
 * @param <T> the type of the numeric values in the range
 */
public class NumberRange<T extends Number & Comparable<T>> {

    /**
     * Represents the empty range.
     */
    private static final NumberRange<?> EMPTY = new NumberRange<>();

    /**
     * Stores the objects representing the ranges of all numbers.
     */
    public static final Map<Class<?>, NumberRange<?>> ALL_CACHE = Map.of(
            Byte.class, new NumberRange<>(Byte.MIN_VALUE, Byte.MAX_VALUE),
            Double.class, new NumberRange<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
            Float.class, new NumberRange<>(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY),
            Integer.class, new NumberRange<>(Integer.MIN_VALUE, Integer.MAX_VALUE),
            Long.class, new NumberRange<>(Long.MIN_VALUE, Long.MAX_VALUE),
            Short.class, new NumberRange<>(Short.MIN_VALUE, Short.MAX_VALUE)
    );

    private final T min;
    private final T max;

    // Private constructor to create the empty range.
    private NumberRange() {
        this(null, null);
    }

    private NumberRange(T min, T max) {
        this.min = min;
        this.max = max;
    }

    /**
     * {@return the inclusive lower bound of the range}
     */
    public T getMin()  {
        return min;
    }

    /**
     * {@return the inclusive upper bound of the range}
     */
    public T getMax() {
        return max;
    }

    /**
     * Creates a {@code NumberRange} object with the specified inclusive lower
     * and upper bounds. If {@code max} is less than {@code min}, their values
     * are interchanged to ensure that the upper bound is greater than or equal
     * to the lower bound.
     *
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @return a {@code NumberRange} object, not {@code null}
     */
    public static <T extends Number & Comparable<T>> @NonNull NumberRange<T> of(@NonNull T min, @NonNull T max) {
        Objects.requireNonNull(min, "min must not be null");
        Objects.requireNonNull(max, "max must not be null");
        if (!ALL_CACHE.containsKey(min.getClass())) {
            throw new IllegalArgumentException("Unsupported numeric type: " + min.getClass().getName());
        }
        if (min.compareTo(max) <= 0) {
            return new NumberRange<>(min, max);
        } else {
            return new NumberRange<>(max, min);
        }
    }

    /**
     * {@return the object representing the empty range}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number & Comparable<T>> @NonNull NumberRange<T> empty() {
        return (NumberRange<T>) EMPTY;
    }

    /**
     * {@return the range of all numbers for the type provided}
     * @param type the class object representing a numeric type
     * @param <T> a numeric type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number & Comparable<T>> @NonNull NumberRange<T> all(Class<T> type) {
        if (!ALL_CACHE.containsKey(type)) {
            throw new IllegalArgumentException();
        }
        return (NumberRange<T>) ALL_CACHE.get(type);
    }

    /**
     * {@return whether the range is empty}
     */
    public boolean isEmpty() {
        return min == null && max == null;
    }

    /**
     * {@return whether the range contains the value specified}
     *
     * @param value the value to be tested
     */
    public boolean contains(@NonNull T value) {
        Objects.requireNonNull(value, "value must not be null");
        return !isEmpty() && min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }

    /**
     * {@return whether the range contains the range specified}
     *
     * @param range the range to be tested
     */
    public boolean containsRange(@NonNull NumberRange<T> range) {
        Objects.requireNonNull(range, "range must not be null");
        return range.isEmpty() ||
            (contains(range.getMin()) && contains(range.getMax()));
    }

    /**
     * {@return whether the range overlaps with the range specified}
     *
     * @param range the range to be tested
     */
    public boolean isOverlapping(@NonNull NumberRange<T> range) {
        Objects.requireNonNull(range, "range must not be null");
        if (isEmpty() || range.isEmpty()) {
            return false;
        }
        return contains(range.getMin()) || contains(range.getMax())
                || range.contains(min) || range.contains(max);
    }

    /**
     * {@return whether the range is disjoint with the range specified}
     *
     * @param range the range to be tested
     */
    public boolean isDisjoint(@NonNull NumberRange<T> range) {
        Objects.requireNonNull(range, "range must not be null");
        return !isOverlapping(range);
    }

    /**
     * {@return the intersection of the range with the range specified}
     *
     * @param range the range to be intersected with the range
     */
    public @NonNull NumberRange<T> intersect(@NonNull NumberRange<T> range) {
        Objects.requireNonNull(range, "range must not be null");
        if (this == range) {
            return this;
        }
        if (isOverlapping(range)) {
            return new NumberRange<>(max(min, range.getMin()), min(max, range.getMax()));
        }
        return empty();
    }

    /**
     * {@return the intersection of the range with the ranges specified}
     *
     * @param ranges the ranges to be intersected with the range
     */
    @SafeVarargs
    public final @NonNull NumberRange<T> intersect(@NonNull NumberRange<T>... ranges) {
        Objects.requireNonNull(ranges, "ranges must not be null");
        var result = this;
        for (var range : ranges) {
            result = result.intersect(range);
            if (result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    /**
     * It constrains the value specified to the range. If the value is within
     * the range, it is returned as is. Otherwise, the lower or upper bound is
     * returned, whichever is closer to the value.
     *
     * @param value the value to be constrained
     * @return the constrained value in the range
     * @throws IllegalArgumentException if the range is empty
     */
    public @NonNull T clamp(@NonNull T value) {
        Objects.requireNonNull(value, "value must not be null");
        if (isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (contains(value)) {
            return value;
        }
        return min.compareTo(value) > 0 ? min : max;
    }

    /**
     * {@return the union of the range with the range specified}
     *
     * @param range the range to be unioned with the range
     * @throws IllegalArgumentException if the specified range is disjoint from
     *                                  this range
     */
    public @NonNull NumberRange<T> union(@NonNull NumberRange<T> range) {
        Objects.requireNonNull(range, "range must not be null");
        if (this == range || range.isEmpty()) {
            return this;
        }
        if (isOverlapping(range)) {
            return new NumberRange(min(min, range.getMin()), max(max, range.getMax()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * {@return the union of the range with the ranges specified}
     *
     * @param ranges the ranges to be unioned with the range
     * @throws IllegalArgumentException if the specified range is disjoint from
     *                                  this range
     */
    public @NonNull NumberRange<T> union(@NonNull NumberRange<T>... ranges) {
        Objects.requireNonNull(ranges, "ranges must not be null");
        var result = this;
        for (var range : ranges) {
            result = result.union(range);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof NumberRange<?> range)
                && Objects.equals(min, range.getMin())
                && Objects.equals(max, range.getMax());
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return isEmpty() ? "[EMPTY]" : String.format("[%s,%s]", min, max);
    }

    private static <T extends Number & Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    private static <T extends Number & Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) > 0 ? a : b;
    }

}
