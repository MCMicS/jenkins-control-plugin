package org.codinjutsu.tools.jenkins.logic;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 *     {M,N}: From the M-th element (inclusive) to the N-th element (exclusive).
 *     {M,}: From the M-th element (inclusive) to the end.
 *     {,N}: From the first element (inclusive) to the N-th element (exclusive). The same as {0,N}.
 *     {N}: Just retrieve the N-th element. The same as {N,N+1}.
 * </pre>
 *
 * @see <a href="https://ci.jenkins.io/api/"> Jenkins API</a>
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class RangeToLoad {
    static final int NO_RESTRICTION = Integer.MIN_VALUE;
    public final int min;
    public final int max;

    /**
     * <pre>
     *     {,N}: From the first element (inclusive) to the N-th element (exclusive). The same as {0,N}.
     * </pre>
     */
    public static @NotNull RangeToLoad to(int maxValue) {
        return new RangeToLoad(NO_RESTRICTION, noRestrictionForNegative(maxValue));
    }

    /**
     * <pre>
     *     {M,N}: From the M-th element (inclusive) to the N-th element (exclusive).
     * </pre>
     */
    public static @NotNull RangeToLoad range(int startElement, int exclusiveEnd) {
        final int start = noRestrictionForNegative(startElement);
        final int end = noRestrictionForNegative(exclusiveEnd);
        return new RangeToLoad(start, end >= start ? end : NO_RESTRICTION);
    }

    /**
     * <pre>
     *     {M,}: From the M-th element (inclusive) to the end.
     * </pre>
     */
    public static @NotNull RangeToLoad from(int startElement) {
        return new RangeToLoad(zeroForNegative(startElement), NO_RESTRICTION);
    }

    /**
     * <pre>
     *     {N}: Just retrieve the N-th element. The same as {N,N+1}.
     * </pre>
     */
    public static @NotNull RangeToLoad only(int startElement) {
        final int start = zeroForNegative(startElement);
        return new RangeToLoad(start, start + 1);
    }

    public @NotNull @NonNls String toQueryParameter() {
        final var queryParameter = new StringBuilder("{");
        if (min == NO_RESTRICTION && (max == NO_RESTRICTION || max == 0)) {
            return "";
        }
        if (min != NO_RESTRICTION) {
            queryParameter.append(min);
        }
        final var hasRange = max == NO_RESTRICTION || min + 1 < max;
        if (hasRange || min == max) {
            queryParameter.append(',');
            if (max != NO_RESTRICTION) {
                queryParameter.append(max);
            }
        }
        queryParameter.append('}');
        return queryParameter.toString();
    }

    private static int noRestrictionForNegative(int number) {
        return Integer.signum(number) == -1 ? NO_RESTRICTION : number;
    }

    private static int zeroForNegative(int number) {
        return Math.max(0, number);
    }
}
