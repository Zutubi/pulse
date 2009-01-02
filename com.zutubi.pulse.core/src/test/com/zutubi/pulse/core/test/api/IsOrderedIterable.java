package com.zutubi.pulse.core.test.api;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Iterator;

/**
 * A Hamcrest matcher that tests a collection contains values matching the
 * given matchers in order, covering the whole iterable (i.e the iterable
 * of matchers and iterable under test must have the same number of elements).
 */
public class IsOrderedIterable<T> extends TypeSafeMatcher<Iterable<? extends T>>
{
    private final Iterable<Matcher<? super T>> matchers;

    /**
     * Creates a matcher that matches against the given matchers in order.
     *
     * @param matchers matchers to test against
     */
    public IsOrderedIterable(Iterable<Matcher<? super T>> matchers)
    {
        this.matchers = matchers;
    }

    public boolean matchesSafely(Iterable<? extends T> got)
    {
        Iterator<? extends T> gotIt = got.iterator();
        Iterator<Matcher<? super T>> matcherIt = matchers.iterator();
        while (gotIt.hasNext() && matcherIt.hasNext())
        {
            if (!matcherIt.next().matches(gotIt.next()))
            {
                return false;
            }
        }

        return !matcherIt.hasNext() && !gotIt.hasNext();
    }

    public void describeTo(Description description)
    {
        description.appendText("iterable with ordered items ");
        description.appendList("{", ", ", "}", matchers);
    }
}
