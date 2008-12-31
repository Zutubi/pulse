package com.zutubi.pulse.core.test;

import junit.framework.Assert;

import java.util.regex.Pattern;

/**
 * Assertions for testing string values.
 */
public class StringAssertions
{
    /**
     * Asserts the given substring is found within the given string, with an
     * appropriate message if it is not.
     *
     * @param expectedSubstring the suffix expected at the end of the string
     * @param got the string to test
     */
    public static void assertContains(CharSequence expectedSubstring, String got)
    {
        Assert.assertTrue("'" + got + "' does not contain '" + expectedSubstring + "'", got.contains(expectedSubstring));
    }

    /**
     * Asserts the given suffix is found at the end of the given string, with
     * an appropriate message if it is not.
     *
     * @param expectedSuffix the suffix expected at the end of the string
     * @param got the string to test
     */
    public static void assertEndsWith(String expectedSuffix, String got)
    {
        Assert.assertTrue("'" + got + "' does not end with '" + expectedSuffix + "'", got.endsWith(expectedSuffix));
    }

    /**
     * Asserts the given prefix is found at the start of the given string, with
     * an appropriate message if it is not.
     *
     * @param expectedPrefix the prefix expected at the start of the string
     * @param got the string to test
     */
    public static void assertStartsWith(String expectedPrefix, String got)
    {
        Assert.assertTrue("'" + got + "' does not start with '" + expectedPrefix + "'", got.startsWith(expectedPrefix));
    }

    /**
     * Asserts the given regular expression matches the given string, with
     * an appropriate message if it does not.
     *
     * @param expression the regular expression to match
     * @param got the string to test
     */
    public static void assertMatches(String expression, String got)
    {
        Pattern p = Pattern.compile(expression);
        Assert.assertTrue("'" + got + "' does not match expression '" + expression + "'", p.matcher(got).matches());
    }
}
