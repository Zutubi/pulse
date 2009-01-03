package com.zutubi.pulse.core.test.api;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Pattern;

/**
 * A Hamcrest matcher that tests if a string matches a Java regular expression.
 */
public class MatchesRegex extends TypeSafeMatcher<String>
{
    private Pattern pattern;

    /**
     * Create a matcher that will match strings against the given regular
     * expression.
     *
     * @see java.util.regex.Pattern
     *
     * @param expression regular expression, in java.util.regex format
     */
    public MatchesRegex(String expression)
    {
        pattern = Pattern.compile(expression);
    }

    public boolean matchesSafely(String s)
    {
        return pattern.matcher(s).matches();
    }

    public void describeTo(Description description)
    {
        description.appendText("string matching regex '" + pattern.toString() + "'");
    }
}
