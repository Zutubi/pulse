package com.zutubi.prototype.type.record;

import junit.framework.TestCase;

/**
 */
public class PathUtilsTest extends TestCase
{
    public void testPrefixMatchesSimpleMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("simple", "simple"));
    }

    public void testPrefixMatchesSimpleMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "notsimple"));
    }

    public void testPrefixMatchesMultipleMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("simple/stuff", "simple"));
        assertTrue(PathUtils.prefixMatchesPathPattern("simple/stuff", "simple/stuff"));
    }

    public void testPrefixMatchesMultipleMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple/stuff", "stuff"));
    }

    public void testPrefixMatchesWildcardMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("*/stuff", "some"));
        assertTrue(PathUtils.prefixMatchesPathPattern("*/stuff", "some/stuff"));
    }

    public void testPrefixMatchesWildcardMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("*/stuff", "some/thingelse"));
    }

    public void testPrefixMatchesLongerPrefix()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "simple/simple/simple"));
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "complex/complex/complex"));
    }
}
