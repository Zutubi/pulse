package com.zutubi.pulse.core.test.api;

import com.google.common.base.Function;
import org.hamcrest.Matcher;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Static factory methods for Pulse's own Hamcrest matchers.
 */
public class Matchers
{
    /**
     * Converts a variable number of items into an iterable of matchers that
     * test equality to those items (using the standard Hamcrest
     * {@link org.hamcrest.Matchers#equalTo} matcher).  This iterable may then
     * be used to construct another matcher that tests against those matchers.
     *
     * @param items items to build matchers out of
     * @param <T> type of the items
     * @return an iterable of matchers that test equality to the given items
     */
    public static <T> Iterable<Matcher<? super T>> getEqualToMatchers(T... items)
    {
        return newArrayList(transform(asList(items), new Function<T, Matcher<? super T>>()
        {
            public Matcher<? super T> apply(T t)
            {
                return org.hamcrest.Matchers.equalTo(t);
            }
        }));
    }

    /**
     * Creates a matcher that tests an iterable for items matching the given
     * matchers.  The iterable matched must contain items that match the given
     * matchers in order, with the same number of items as their are matchers.
     *
     * @param matchers iterable of matchers to match against
     * @param <T> type of the items in the iterable to be matched
     * @return a matcher that tests an iterable against the given matchers in
     *         order
     */
    public static <T> IsOrderedIterable<T> hasOrderedItems(Iterable<Matcher<? super T>> matchers)
    {
        return new IsOrderedIterable<T>(matchers);
    }

    /**
     * Convenience method equivalent to {@link #hasOrderedItems(Iterable)} that
     * accepts a variable number of matchers to build the matchers iterable.
     *
     * @param matchers variable number of matchers to match against
     * @param <T> type of the items in the iterable to be matched
     * @return a matcher that tests an iterable against the given matchers in
     *         order
     */
    public static <T> IsOrderedIterable<T> hasOrderedItems(Matcher<? super T>... matchers)
    {
        return new IsOrderedIterable<T>(asList(matchers));
    }

    /**
     * Convenience equivalent to {@link #hasOrderedItems(Iterable)} that
     * accepts a variable number of items and converts them to equalTo
     * matchers to build the matchers iterable.
     *
     * @param items variable number of items to match against using
     *              {@link org.hamcrest.Matchers#equalTo}
     * @param <T> type of the items in the iterable to be matched
     * @return a matcher that tests an iterable against the given items in
     *         order
     */
    public static <T> IsOrderedIterable<T> hasOrderedItems(T... items)
    {
        return new IsOrderedIterable<T>(getEqualToMatchers(items));
    }

    /**
     * Creates a matcher that matches strings against the given regular
     * expression.
     *
     * @see java.util.regex.Pattern
     *
     * @param expression the regular expresion to match against
     * @return a matcher that matches against the given expression
     */
    public static MatchesRegex matchesRegex(String expression)
    {
        return new MatchesRegex(expression);
    }
}
