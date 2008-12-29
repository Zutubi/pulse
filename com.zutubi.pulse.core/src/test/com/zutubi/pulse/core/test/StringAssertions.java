package com.zutubi.pulse.core.test;

import junit.framework.Assert;

import java.util.regex.Pattern;

/**
 * Assertions for testing string values.
 */
public class StringAssertions
{
    public static void assertEndsWith(String a, String b)
    {
        Assert.assertTrue("'" + b + "' does not end with '" + a + "'", b.endsWith(a));
    }

    public static void assertMatches(String expression, String got)
    {
        Pattern p = Pattern.compile(expression);
        Assert.assertTrue("'" + got + "' does not match expression '" + expression + "'", p.matcher(got).matches());
    }
}
