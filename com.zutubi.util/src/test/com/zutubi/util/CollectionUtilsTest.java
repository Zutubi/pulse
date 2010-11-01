package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.util.math.IntegerAddition;
import static java.util.Arrays.asList;

import java.util.*;

public class CollectionUtilsTest extends ZutubiTestCase
{
    public void testReverseEmpty()
    {
        Object[] empty = new Object[0];
        CollectionUtils.reverse(empty);

        // A bit useless, more checking nothing blows up
        assertEquals(0, empty.length);
    }

    public void testReverseOneElement()
    {
        Object a = new Object();
        Object[] oneEl = new Object[]{a};
        CollectionUtils.reverse(oneEl);

        assertEquals(1, oneEl.length);
        assertSame(a, oneEl[0]);
    }

    public void testReverseTwoElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object[] twoEls = new Object[]{a, b};
        CollectionUtils.reverse(twoEls);

        assertEquals(2, twoEls.length);
        assertSame(b, twoEls[0]);
        assertSame(a, twoEls[1]);
    }

    public void testReverseThreeElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object[] threeEls = new Object[]{a, b, c};
        CollectionUtils.reverse(threeEls);

        assertEquals(3, threeEls.length);
        assertSame(c, threeEls[0]);
        assertSame(b, threeEls[1]);
        assertSame(a, threeEls[2]);
    }

    public void testReverseFourElements()
    {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object d = new Object();
        Object[] fourEls = new Object[]{a, b, c, d};
        CollectionUtils.reverse(fourEls);

        assertEquals(4, fourEls.length);
        assertSame(d, fourEls[0]);
        assertSame(c, fourEls[1]);
        assertSame(b, fourEls[2]);
        assertSame(a, fourEls[3]);
    }

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

    public void testTimesZero()
    {
        timesHelper(0);
    }

    public void testTimesOne()
    {
        timesHelper(1);
    }

    public void testTimesTwo()
    {
        timesHelper(2);
    }

    public void testTimesMany()
    {
        timesHelper(20);
    }

    private void timesHelper(int count)
    {
        Object o = new Object();
        List<Object> list = CollectionUtils.times(o, count);
        assertEquals(count, list.size());
        for (Object p: list)
        {
            assertSame(o, p);
        }
    }

    public void testCountEmpty()
    {
        assertEquals(0, CollectionUtils.count(Collections.emptyList(), new TruePredicate<Object>()));
    }

    public void testCountAllMatch()
    {
        assertEquals(3, CollectionUtils.count(asList(1, 2, 3), new TruePredicate<Integer>()));
    }

    public void testCountNoneMatch()
    {
        assertEquals(0, CollectionUtils.count(asList(1, 2, 3), new FalsePredicate<Integer>()));
    }

    public void testCountSomeMatch()
    {
        int result = CollectionUtils.count(asList(1, 2, 3), new EqualsPredicate<Integer>(2));
        assertEquals(1, result);
    }

    public void testAsSet()
    {
        assertEquals(new HashSet<String>(Arrays.asList("foo", "bar", "baz")), CollectionUtils.asSet("bar", "foo", "baz", "bar"));
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
        assertFalse(CollectionUtils.depthFirstContains(root, new EqualsPredicate("a")));
        assertTrue(CollectionUtils.depthFirstContains(root, new EqualsPredicate("1")));
    }

    public void testBreadthFirstFindTraversal()
    {
        TreeNode<String> root = setupTestTree();

        assertEquals(asList("0", "1", "2", "1-1", "1-2", "2-1", "2-2", "2-3", "1-2-1"), breadthFirstSearchOrder(root));
    }

    public void testBreadthFirstContains()
    {
        TreeNode<String> root = setupTestTree();
        assertFalse(CollectionUtils.breadthFirstContains(root, new EqualsPredicate("a")));
        assertTrue(CollectionUtils.breadthFirstContains(root, new EqualsPredicate("1")));
    }

    private List<String> depthFirstSearchOrder(TreeNode<String> root)
    {
        final List<String> searchOrder = new LinkedList<String>();
        CollectionUtils.depthFirstFind(root, new Predicate<String>()
        {
            public boolean satisfied(String s)
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
            public boolean satisfied(String s)
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

    public void testFilterInPlace()
    {
        List<Object> list = new LinkedList<Object>(Arrays.asList((Object)"a", 1, "b", 2));
        List<Object> filtered = CollectionUtils.filterInPlace(list, new InstanceOfPredicate(String.class));
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals(2, filtered.size());
        assertEquals(1, filtered.get(0));
        assertEquals(2, filtered.get(1));
    }

    public void testFilterInPlaceRejectingAll()
    {
        List<Object> list = new LinkedList<Object>(Arrays.asList("a", "b", "c"));
        List<Object> removed = CollectionUtils.filterInPlace(list, new FalsePredicate());
        assertEquals(0, list.size());
        assertEquals(3, removed.size());
    }

    public void testFilterInPlaceAcceptingAll()
    {
        List<Object> list = new LinkedList<Object>(Arrays.asList("a", "b", "c"));
        List<Object> removed = CollectionUtils.filterInPlace(list, new TruePredicate());
        assertEquals(3, list.size());
        assertEquals(0, removed.size());
    }

    public void testReverse()
    {
        List<String> list = Arrays.asList("a", "b", "c");
        List<String> reversed = CollectionUtils.reverse(list);
        assertEquals("c", reversed.get(0));
        assertEquals("b", reversed.get(1));
        assertEquals("a", reversed.get(2));
    }

    public void testPartitionEmpty()
    {
        assertEquals(new LinkedList<List<Integer>>(), CollectionUtils.partition(1, Arrays.<Integer>asList()));
    }

    public void testPartitionOneSingle()
    {
        assertEquals(asList(asList(1)), CollectionUtils.partition(1, asList(1)));
    }

    public void testPartitionTwoSingle()
    {
        assertEquals(asList(asList(1)), CollectionUtils.partition(1, asList(1)));
    }

    public void testPartitionOneSeveral()
    {
        assertEquals(asList(asList(1), asList(2), asList(3), asList(4)), CollectionUtils.partition(1, asList(1, 2, 3, 4)));
    }

    public void testPartitionTwoEven()
    {
        assertEquals(asList(asList(1, 2), asList(3, 4)), CollectionUtils.partition(2, asList(1, 2, 3, 4)));
    }

    public void testPartitionThreeTwo()
    {
        assertEquals(asList(asList(1, 2)), CollectionUtils.partition(3, asList(1, 2)));
    }

    public void testPartitionThreeThree()
    {
        assertEquals(asList(asList(1, 2, 3)), CollectionUtils.partition(3, asList(1, 2, 3)));
    }

    public void testPartitionThreeFour()
    {
        assertEquals(asList(asList(1, 2, 3), asList(4)), CollectionUtils.partition(3, asList(1, 2, 3, 4)));
    }

    public void testPartitionThreeFive()
    {
        assertEquals(asList(asList(1, 2, 3), asList(4, 5)), CollectionUtils.partition(3, asList(1, 2, 3, 4, 5)));
    }

    public void testPartitionThreeSix()
    {
        assertEquals(asList(asList(1, 2, 3), asList(4, 5, 6)), CollectionUtils.partition(3, asList(1, 2, 3, 4, 5, 6)));
    }

    public void testConcatenateLists()
    {
        assertEquals(asList("a", "b", "c"), CollectionUtils.concatenate(asList("a"), asList("b", "c")));
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
