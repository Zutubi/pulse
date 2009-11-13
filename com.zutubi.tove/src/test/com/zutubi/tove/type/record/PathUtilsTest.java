package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

public class PathUtilsTest extends ZutubiTestCase
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

    public void testGetSuffixEmpty()
    {
        assertEquals("", PathUtils.getSuffix("", 0));
        assertEquals("", PathUtils.getSuffix("", 1));
        assertEquals("", PathUtils.getSuffix("", 2));
    }

    public void testGetSuffixSingleElement()
    {
        assertEquals("el", PathUtils.getSuffix("el", 0));
        assertEquals("", PathUtils.getSuffix("el", 1));
        assertEquals("", PathUtils.getSuffix("el", 2));
    }

    public void testGetSuffixTwoElements()
    {
        assertEquals("el1/el2", PathUtils.getSuffix("el1/el2", 0));
        assertEquals("el2", PathUtils.getSuffix("el1/el2", 1));
        assertEquals("", PathUtils.getSuffix("el1/el2", 2));
    }

    public void testGetSuffixManyElements()
    {
        assertEquals("el1/el2/el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 0));
        assertEquals("el2/el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 1));
        assertEquals("el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 2));
    }
}
