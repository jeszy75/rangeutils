package rangeutils;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class IntRangeAssertJTest {

    private IntRange range;

    void assertIntRange(int expectedMin, int expectedMax, IntRange range) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(range)
                    .as("IntRange bound check")
                    .extracting(IntRange::getMin, IntRange::getMax)
                    .as("min and max")
                    .containsExactly(expectedMin, expectedMax);
        });
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
            "15, 15, 15, 15",
    })
    void of(int min, int max, int expectedMin, int expectedMax) {
        assertIntRange(expectedMin, expectedMax, IntRange.of(min, max));
    }

    @Test
    void isEmpty() {
        assertThat(range).doesNotMatch(IntRange::isEmpty);
        assertThat(IntRange.EMPTY).matches(IntRange::isEmpty);
    }

    @Test
    void contains() {
        assertThat(range.contains(15)).isTrue();
        assertThat(range.contains(30)).isTrue();
        assertThat(range.contains(20)).isTrue();
        assertThat(range.contains(-10)).isFalse();
        assertThat(range.contains(40)).isFalse();
        assertThat(IntRange.ALL.contains(Integer.MIN_VALUE)).isTrue();
        assertThat(IntRange.ALL.contains(Integer.MAX_VALUE)).isTrue();
        assertThat(IntRange.EMPTY.contains(0)).isFalse();
        assertThat(IntRange.EMPTY.contains(Integer.MIN_VALUE)).isFalse();
        assertThat(IntRange.EMPTY.contains(Integer.MAX_VALUE)).isFalse();
    }

    @Test
    void containsRange() {
        assertThat(range.containsRange(range)).isTrue();
        assertThat(range.containsRange(IntRange.of(15, 20))).isTrue();
        assertThat(range.containsRange(IntRange.of(25, 30))).isTrue();
        assertThat(range.containsRange(IntRange.of(20, 25))).isTrue();
        assertThat(range.containsRange(IntRange.EMPTY)).isTrue();
        assertThat(range.containsRange(IntRange.of(-100, 20))).isFalse();
        assertThat(range.containsRange(IntRange.of(25, 100))).isFalse();
        assertThat(range.containsRange(IntRange.ALL)).isFalse();
        assertThat(IntRange.EMPTY.containsRange(IntRange.EMPTY)).isTrue();
    }

    @Test
    void isOverlapping() {
        assertThat(range.isOverlapping(range)).isTrue();
        assertThat(range.isOverlapping(IntRange.of(15, 20))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(25, 30))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(20, 25))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(-100, 20))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(25, 100))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(-100, 100))).isTrue();
        assertThat(range.isOverlapping(IntRange.of(-100, 0))).isFalse();
        assertThat(range.isOverlapping(IntRange.of(40, 100))).isFalse();
        assertThat(range.isOverlapping(IntRange.EMPTY)).isFalse();
        assertThat(IntRange.EMPTY.isOverlapping(IntRange.EMPTY)).isFalse();
    }

    @Test
    void isDisjoint() {
        assertThat(range.isDisjoint(range)).isFalse();
        assertThat(range.isDisjoint(IntRange.of(15, 20))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(25, 30))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(20, 25))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(-100, 20))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(25, 100))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(-100, 100))).isFalse();
        assertThat(range.isDisjoint(IntRange.of(-100, 0))).isTrue();
        assertThat(range.isDisjoint(IntRange.of(40, 100))).isTrue();
        assertThat(range.isDisjoint(IntRange.EMPTY)).isTrue();
        assertThat(IntRange.EMPTY.isDisjoint(IntRange.EMPTY)).isTrue();
    }

    @Test
    void intersect() {
        assertThat(range).isSameAs(range.intersect(range))
                .isEqualTo(range.intersect(IntRange.of(-100, 100)));
        assertThat(IntRange.of(15, 20)).isEqualTo(range.intersect(IntRange.of(15, 20)))
                .isEqualTo(range.intersect(IntRange.of(-100, 20)));
        assertThat(IntRange.of(20, 25)).isEqualTo(range.intersect(IntRange.of(20, 25)));
        assertThat(IntRange.of(25, 30)).isEqualTo(range.intersect(IntRange.of(25, 30)))
                .isEqualTo(range.intersect(IntRange.of(25, 100)));
        assertThat(IntRange.EMPTY).isEqualTo(range.intersect(IntRange.of(-100, 0)))
                .isEqualTo(range.intersect(IntRange.of(40, 100)))
                .isEqualTo(IntRange.EMPTY.intersect(IntRange.ALL));
    }

    @Test
    void intersect_vararg() {
        assertThat(IntRange.of(20, 25)).isEqualTo(range.intersect(IntRange.of(10, 25), IntRange.of(20, 35)))
                .isEqualTo(range.intersect(IntRange.of(-100, 100), IntRange.of(20, 25)));
        assertThat(IntRange.EMPTY).isEqualTo(range.intersect(IntRange.ALL, IntRange.EMPTY));
    }

    @Test
    void testHashCode() {
        assertThat(range).hasSameHashCodeAs(range)
                .hasSameHashCodeAs(IntRange.of(15, 30))
                .doesNotHaveSameHashCodeAs(IntRange.ALL);
    }

    @Test
    void testEquals() {
        assertThat(range).isEqualTo(range)
                .isEqualTo(IntRange.of(15, 30))
                .isNotEqualTo(IntRange.of(-100, 100))
                .isNotEqualTo(IntRange.EMPTY)
                .isNotEqualTo(null)
                .isNotEqualTo("Hello, World!");
    }

    @Test
    void testToString() {
        assertThat(range).hasToString("[15,30]");
        assertThat(IntRange.EMPTY).hasToString("[EMPTY]");
    }

}
