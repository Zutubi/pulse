package com.zutubi.pulse.core.plugins.util;

import com.google.common.collect.Sets;

import java.util.HashSet;

public class TransitiveFunctionTest extends DependentTestCase
{
    public void testNoDeps()
    {
        assertEquals(new HashSet<String>(), fn.apply("no-deps"));
    }

    public void testOneDep()
    {
        assertEquals(Sets.newHashSet("one-dep-1"), fn.apply("one-dep"));
    }

    public void testTwoDeps()
    {
        assertEquals(Sets.newHashSet("two-deps-1", "two-deps-2"), fn.apply("two-deps"));
    }

    public void testChain()
    {
        assertEquals(Sets.newHashSet("middle", "bottom"), fn.apply("top"));
    }

    public void testTree()
    {
        assertEquals(Sets.newHashSet("left", "right", "left-left", "left-right", "right-left", "right-right"), fn.apply("root"));
    }

    public void testSelfReference()
    {
        assertEquals(Sets.newHashSet("self-dep"), fn.apply("self-dep"));
    }

    public void testCycle()
    {
        assertEquals(Sets.newHashSet("home", "first", "second", "third"), fn.apply("home"));
    }
}
