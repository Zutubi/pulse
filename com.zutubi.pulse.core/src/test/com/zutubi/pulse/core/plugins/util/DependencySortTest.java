package com.zutubi.pulse.core.plugins.util;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;

public class DependencySortTest extends DependentTestCase
{
    public void testSortEmpty()
    {
        assertEquals(Collections.<String>emptyList(), DependencySort.sort(Collections.<String>emptyList(), fn));
    }

    public void testSortTrivial()
    {
        assertEquals(asList("left"), DependencySort.sort(asList("left"), fn));
    }

    public void testSortOneDependent()
    {
        List<String> sorted = asList("one-dep", "one-dep-1");
        assertEquals(sorted, DependencySort.sort(asList("one-dep", "one-dep-1"), fn));
        assertEquals(sorted, DependencySort.sort(asList("one-dep-1", "one-dep"), fn));
    }

    public void testSortTwoDependents()
    {
        assertEquals(asList("two-deps", "two-deps-1", "two-deps-2"), DependencySort.sort(asList("two-deps", "two-deps-1", "two-deps-2"), fn));
        assertEquals(asList("two-deps", "two-deps-2", "two-deps-1"), DependencySort.sort(asList("two-deps", "two-deps-2", "two-deps-1"), fn));
        assertEquals(asList("two-deps", "two-deps-1"), DependencySort.sort(asList("two-deps", "two-deps-1"), fn));
        assertEquals(asList("two-deps", "two-deps-1"), DependencySort.sort(asList("two-deps-1", "two-deps"), fn));
        assertEquals(asList("two-deps-1", "two-deps-2"), DependencySort.sort(asList("two-deps-1", "two-deps-2"), fn));
        assertEquals(asList("two-deps-2", "two-deps-1"), DependencySort.sort(asList("two-deps-2", "two-deps-1"), fn));
    }

    public void testSortChain()
    {
        assertEquals(asList("top", "middle", "bottom"), DependencySort.sort(asList("top", "middle", "bottom"), fn));
        assertEquals(asList("top", "middle", "bottom"), DependencySort.sort(asList("middle", "bottom", "top"), fn));
        assertEquals(asList("top", "middle", "bottom"), DependencySort.sort(asList("bottom", "middle", "top"), fn));
        assertEquals(asList("top", "bottom"), DependencySort.sort(asList("top", "bottom"), fn));
        assertEquals(asList("top", "bottom"), DependencySort.sort(asList("bottom", "top"), fn));
    }

    public void testTree()
    {
        assertEquals(asList("root", "left", "left-left", "left-right"), DependencySort.sort(asList("root", "left", "left-left", "left-right"), fn));
        assertEquals(asList("root", "left", "left-left", "left-right"), DependencySort.sort(asList("left-left", "left-right", "root", "left"), fn));
        assertEquals(asList("root", "left", "left-right", "left-left"), DependencySort.sort(asList("left-right", "root", "left-left", "left"), fn));
        assertEquals(asList("root", "left", "left-right", "left-left"), DependencySort.sort(asList("left-right", "left", "root", "left-left"), fn));
        assertEquals(asList("root", "left-left", "left-right"), DependencySort.sort(asList("left-left", "root", "left-right"), fn));
        assertEquals(asList("root", "left-right", "left-left"), DependencySort.sort(asList("left-right", "root", "left-left"), fn));
        assertEquals(asList("left-right", "left-left"), DependencySort.sort(asList("left-right", "left-left"), fn));
        assertEquals(asList("left-left", "left-right"), DependencySort.sort(asList("left-left", "left-right"), fn));
    }
}
