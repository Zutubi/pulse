package com.zutubi.util;

import com.google.common.base.Predicate;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import java.util.*;

/**
 * Various utilities for working with collections.  Note that we prefer to use Guava utilities, and
 * if Guava doesn't contain a utility we first ask why, as there may be a simple alternative by
 * combining a couple of existing utilities that makes the behaviour explicit.  Where our style is
 * different we end up with a method here.
 */
public class CollectionUtils
{
    /**
     * Tests if an array contains a value equal to the given one.  Essentially the same as
     * Arrays.asList(a).contains(x), but doesn't create a list view over the array.
     *
     * @param a the array to search
     * @param x the value to search for
     * @param <T> type of the array elements
     * @return true if a contains an element equal to v, false otherwise
     */
    public static <T> boolean contains(T[] a, T x)
    {
        for (T t: a)
        {
            if (t.equals(x))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a new pair out of two values.
     *
     * @param first the first value
     * @param second the second value
     * @param <T> type of the first value
     * @param <U> type of the second value
     * @return the new pair
     */
    public static <T, U> Pair<T, U> asPair(T first, U second)
    {
        return new Pair<T,U>(first, second);
    }

    public static <T, U> Map<T, U> asMap(Pair<? extends T, ? extends U>... pairs)
    {
        return asMap(Arrays.asList(pairs));
    }

    public static <T, U> Map<T, U> asMap(Collection<? extends Pair<? extends T, ? extends U>> pairs)
    {
        HashMap<T, U> result = new HashMap<T,U>(pairs.size());
        for(Pair<? extends T, ? extends U> pair: pairs)
        {
            result.put(pair.first, pair.second);
        }

        return result;
    }

    public static <T, U> Map<T, U> asOrderedMap(Pair<? extends T, ? extends U>... pairs)
    {
        return asOrderedMap(Arrays.asList(pairs));
    }

    public static <T, U> Map<T, U> asOrderedMap(Collection<? extends Pair<? extends T, ? extends U>> pairs)
    {
        HashMap<T, U> result = new LinkedHashMap<T,U>(pairs.size());
        for(Pair<? extends T, ? extends U> pair: pairs)
        {
            result.put(pair.first, pair.second);
        }

        return result;
    }

    public static <T> Vector<T> asVector(T... ts)
    {
        return new Vector<T>(Arrays.asList(ts));
    }

    public static <T> void traverse(Collection<T> c, UnaryProcedure<T> f)
    {
        for (T t : c)
        {
            f.run(t);
        }
    }

    /**
     * Return a copy of the list c that has no duplicates.
     *
     * @param l the list from which duplicates will be removed.
     *
     * @return the copy of the list with duplicates removed.
     */
    public static <T> List<T> unique(List<T> l)
    {
        // sets by definition are unique, and so will do the processing for us.
        LinkedHashSet<T> seen = new LinkedHashSet<T>();
        seen.addAll(l);
        return new LinkedList<T>(seen);
    }

    /**
     * Create a sorted version of the list using the provided comparator without changing the
     * original list.
     *
     * @param list          the list containing the items to be sorted.
     * @param comparator    the comparator used to define the sort order.
     * @param <T>           the type of item in the list.
     * @return  a sorted copy of the list.
     */
    public static <T> List<T> sort(List<T> list, Comparator<T> comparator)
    {
        List<T> copy = new LinkedList<T>(list);
        Collections.sort(copy, comparator);
        return copy;
    }

    /**
     * Collapses a collection down to a single value by successive applications
     * of the given binary function.  The function is applied to the current
     * result and each item of the collection in turn.
     *
     * @param c       input collection
     * @param initial initial value for the result
     * @param fn      function to apply to combine the current result with an
     *                item
     * @param <T>     type of the collection items
     * @return the result of reducing the collection using the given function
     */
    public static <T> T reduce(Collection<T> c, T initial, BinaryFunction<T, T, T> fn)
    {
        T result = initial;
        for (T t: c)
        {
            result = fn.process(result, t);
        }

        return result;
    }

    /**
     * Carries out a depth first traversal of the tree, using the predicate
     * to identify the node of interest.  If a node is found that satisfies the predicate,
     * true is returned.
     *
     * @param root          the tree to be searched.
     * @param predicate     the predicate identifying the node being searched for.
     * @param <T>           the type of the collection items
     * 
     * @return true if one of the tree nodes satisfies the predicate, false otherwise.
     */
    public static <T> boolean depthFirstContains(TreeNode<T> root, Predicate<T> predicate)
    {
        return depthFirstFind(root, predicate) != null;
    }

    /**
     * Carries out a depth first traversal of the tree, using the predicate
     * to identify the node of interest.  The first node to satisfy the predicate is returned.
     *
     * @param root          the tree to be searched.
     * @param predicate     the predicate identifying the node being searched for.
     * @param <T>           the type of the collection items
     *
     * @return the first node that satisfies the predicate, null if no nodes satisfy the predicate.
     */
    public static <T> T depthFirstFind(TreeNode<T> root, Predicate<T> predicate)
    {
        for (TreeNode<T> child: root.getChildren())
        {
            T found = depthFirstFind(child, predicate);
            if (found != null)
            {
                return found;
            }
        }

        if (predicate.apply(root.getData()))
        {
            return root.getData();
        }

        return null;
    }

    /**
     * Carries out a breadth first traversal of the tree, using the predicate
     * to identify the node of interest.  If a node is found that satisfies the predicate,
     * true is returned.
     *
     * @param root          the tree to be searched.
     * @param predicate     the predicate identifying the node being searched for.
     * @param <T>           the type of the collection items
     *
     * @return true if one of the tree nodes satisfies the predicate, false otherwise.
     */
    public static <T> boolean breadthFirstContains(TreeNode<T> root, Predicate<T> predicate)
    {
        return breadthFirstFind(root, predicate) != null;
    }

    /**
     * Carries out a breadth first traversal of the tree, using the predicate
     * to identify the node of interest.  The first node to satisfy the predicate is returned.
     *
     * @param root          the tree to be searched.
     * @param predicate     the predicate identifying the node being searched for.
     * @param <T>           the type of the collection items
     *
     * @return the first node that satisfies the predicate, null if no nodes satisfy the predicate.
     */
    public static <T> T breadthFirstFind(TreeNode<T> root, Predicate<T> predicate)
    {
        Queue<TreeNode<T>> toProcess = new LinkedList<TreeNode<T>>();
        toProcess.offer(root);

        return breadthFirstFind(toProcess, predicate);
    }

    private static <T> T breadthFirstFind(Queue<TreeNode<T>> toProcess, Predicate<T> predicate)
    {
        while (!toProcess.isEmpty())
        {
            TreeNode<T> next = toProcess.remove();
            if (predicate.apply(next.getData()))
            {
                return next.getData();
            }

            for (TreeNode<T> child: next.getChildren())
            {
                toProcess.offer(child);
            }
        }
        return null;
    }
}
