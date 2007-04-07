package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

/**
 */
public class PathUtilsTest extends TestCase
{
    public void testPrefixMatchesSimpleMatch()
    {
        assertTrue(PathUtils.prefixMatches("simple", "simple"));
    }

    public void testPrefixMatchesSimpleMismatch()
    {
        assertFalse(PathUtils.prefixMatches("simple", "notsimple"));
    }

    public void testPrefixMatchesMultipleMatch()
    {
        assertTrue(PathUtils.prefixMatches("simple/stuff", "simple"));
        assertTrue(PathUtils.prefixMatches("simple/stuff", "simple/stuff"));
    }

    public void testPrefixMatchesMultipleMismatch()
    {
        assertFalse(PathUtils.prefixMatches("simple/stuff", "stuff"));
    }

    public void testPrefixMatchesWildcardMatch()
    {
        assertTrue(PathUtils.prefixMatches("*/stuff", "some"));
        assertTrue(PathUtils.prefixMatches("*/stuff", "some/stuff"));
    }

    public void testPrefixMatchesWildcardMismatch()
    {
        assertFalse(PathUtils.prefixMatches("*/stuff", "some/thingelse"));
    }

    public void testPrefixMatchesLongerPrefix()
    {
        assertFalse(PathUtils.prefixMatches("simple", "simple/simple/simple"));
        assertFalse(PathUtils.prefixMatches("simple", "complex/complex/complex"));
    }
}
