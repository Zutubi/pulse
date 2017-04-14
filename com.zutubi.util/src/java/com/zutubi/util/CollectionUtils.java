/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.util;

import com.google.common.base.Predicate;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

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
