package com.zutubi.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.zutubi.util.adt.TreeNode;
import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.util.math.IntegerAddition;

import java.util.*;

import static com.google.common.base.Predicates.equalTo;
import static java.util.Arrays.asList;

public class CollectionUtilsTest extends ZutubiTestCase
{
    public void testUniqueStrings()
    {
        assertUnique(asList("a", "b", "c"), asList("a", "b", "b", "c"));
    }

    public void testUniqueEmpty()
    {
        assertUnique(new LinkedList<Object>(), new LinkedList<Object>());
    }

    public void testUniqueContainsNull()
    {
        assertUnique(asList("a", null, "c"), asList("a", null, null, "c"));
    }

    public void testUniqueOrderMaintained()
    {
        assertUnique(asList("c", "1", "a", "5", "2", "x", "n"), asList("c", "1", "a", "c", "1", "5", "2", "x", "n", "a"));
    }

    private <T> void assertUnique(List<T> expected, List<T> test)
    {
        List<T> result = CollectionUtils.unique(test);
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    public void testAsSet()
    {
        assertEquals(new HashSet<String>(Arrays.asList("foo", "bar", "baz")), Sets.newHashSet("bar", "foo", "baz", "bar"));
    }

    public void testReduceEmpty()
    {
        int result = CollectionUtils.reduce(Collections.<Integer>emptyList(), 4, new IntegerAddition());
        assertEquals(4, result);
    }

    public void testReduceSingle()
    {
        int result = CollectionUtils.reduce(asList(2), 4, new IntegerAddition());
        assertEquals(6, result);
    }

    public void testReduceMultiple()
    {
        int result = CollectionUtils.reduce(asList(1, 2, 3), 4, new IntegerAddition());
        assertEquals(10, result);
    }

    public void testDepthFirstFindTraversal()
    {
        TreeNode<String> root = setupTestTree();

        assertEquals(asList("1-1", "1-2-1", "1-2", "1", "2-1", "2-2", "2-3", "2", "0"), depthFirstSearchOrder(root));
    }

    public void testDepthFirstContains()
    {
        TreeNode<String> root = setupTestTree();
        assertFalse(CollectionUtils.depthFirstContains(root, equalTo("a")));
        assertTrue(CollectionUtils.depthFirstContains(root, equalTo("1")));
    }

    public void testBreadthFirstFindTraversal()
    {
        TreeNode<String> root = setupTestTree();

        assertEquals(asList("0", "1", "2", "1-1", "1-2", "2-1", "2-2", "2-3", "1-2-1"), breadthFirstSearchOrder(root));
    }

    public void testBreadthFirstContains()
    {
        TreeNode<String> root = setupTestTree();
        assertFalse(CollectionUtils.breadthFirstContains(root, equalTo("a")));
        assertTrue(CollectionUtils.breadthFirstContains(root, equalTo("1")));
    }

    private List<String> depthFirstSearchOrder(TreeNode<String> root)
    {
        final List<String> searchOrder = new LinkedList<String>();
        CollectionUtils.depthFirstFind(root, new Predicate<String>()
        {
            public boolean apply(String s)
            {
                searchOrder.add(s);
                return false;
            }
        });
        return searchOrder;
    }

    private List<String> breadthFirstSearchOrder(TreeNode<String> root)
    {
        final List<String> searchOrder = new LinkedList<String>();
        CollectionUtils.breadthFirstFind(root, new Predicate<String>()
        {
            public boolean apply(String s)
            {
                searchOrder.add(s);
                return false;
            }
        });
        return searchOrder;
    }

    private TreeNode<String> setupTestTree()
    {
        return new TreeNode<String>("0",
                new TreeNode<String>("1",
                        new TreeNode<String>("1-1"),
                        new TreeNode<String>("1-2",
                                new TreeNode<String>("1-2-1"))),
                new TreeNode<String>("2",
                        new TreeNode<String>("2-1"),
                        new TreeNode<String>("2-2"),
                        new TreeNode<String>("2-3")));
    }

    public void testIndexOf()
    {
        assertEquals(0, CollectionUtils.indexOf("A", "A", "B", "C"));
        assertEquals(1, CollectionUtils.indexOf("B", "A", "B", "C"));
        assertEquals(2, CollectionUtils.indexOf("C", "A", "B", "C"));
        assertEquals(-1, CollectionUtils.indexOf("D", "A", "B", "C"));
        assertEquals(-1, CollectionUtils.indexOf("E"));
        assertEquals(-1, CollectionUtils.indexOf(null));
        assertEquals(0, CollectionUtils.indexOf(null, (Object)null));
        assertEquals(-1, CollectionUtils.indexOf(null, "A", "B"));
        assertEquals(-1, CollectionUtils.indexOf("A", null, "B"));
        assertEquals(1, CollectionUtils.indexOf("B", null, "B"));
    }
}
