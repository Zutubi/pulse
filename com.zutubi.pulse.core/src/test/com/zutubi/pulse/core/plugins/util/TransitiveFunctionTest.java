package com.zutubi.pulse.core.plugins.util;

import static com.zutubi.util.CollectionUtils.asSet;

import java.util.HashSet;

public class TransitiveFunctionTest extends DependentTestCase
{
    public void testNoDeps()
    {
        assertEquals(new HashSet<String>(), fn.process("no-deps"));
    }

    public void testOneDep()
    {
        assertEquals(asSet("one-dep-1"), fn.process("one-dep"));
    }

    public void testTwoDeps()
    {
        assertEquals(asSet("two-deps-1", "two-deps-2"), fn.process("two-deps"));
    }

    public void testChain()
    {
        assertEquals(asSet("middle", "bottom"), fn.process("top"));
    }

    public void testTree()
    {
        assertEquals(asSet("left", "right", "left-left", "left-right", "right-left", "right-right"), fn.process("root"));
    }

    public void testSelfReference()
    {
        assertEquals(asSet("self-dep"), fn.process("self-dep"));
    }

    public void testCycle()
    {
        assertEquals(asSet("home", "first", "second", "third"), fn.process("home"));
    }
}
