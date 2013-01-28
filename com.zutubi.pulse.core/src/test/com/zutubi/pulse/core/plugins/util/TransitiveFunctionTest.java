package com.zutubi.pulse.core.plugins.util;

import com.google.common.collect.Sets;

import java.util.HashSet;

public class TransitiveFunctionTest extends DependentTestCase
{
    public void testNoDeps()
    {
        assertEquals(new HashSet<String>(), fn.process("no-deps"));
    }

    public void testOneDep()
    {
        assertEquals(Sets.newHashSet("one-dep-1"), fn.process("one-dep"));
    }

    public void testTwoDeps()
    {
        assertEquals(Sets.newHashSet("two-deps-1", "two-deps-2"), fn.process("two-deps"));
    }

    public void testChain()
    {
        assertEquals(Sets.newHashSet("middle", "bottom"), fn.process("top"));
    }

    public void testTree()
    {
        assertEquals(Sets.newHashSet("left", "right", "left-left", "left-right", "right-left", "right-right"), fn.process("root"));
    }

    public void testSelfReference()
    {
        assertEquals(Sets.newHashSet("self-dep"), fn.process("self-dep"));
    }

    public void testCycle()
    {
        assertEquals(Sets.newHashSet("home", "first", "second", "third"), fn.process("home"));
    }
}
