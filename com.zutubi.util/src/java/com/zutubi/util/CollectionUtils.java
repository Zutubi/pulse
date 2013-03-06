package com.zutubi.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.adt.TreeNode;

import java.io.PrintStream;
import java.util.*;

/**
 */
public class CollectionUtils
{
    public static <T, U> U[] mapToArray(T[] in, Function<T, U> m, U[] out)
    {
        int i = 0;
        for(T t: in)
        {
            out[i++] = m.apply(t);
        }

        return out;
    }

    public static <K, T, U> Map<K, U> map(Map<K, T> in, Function<T, U> m)
    {
        Map<K, U> result = new HashMap<K,U>(in.size());
        map(in, m, result);
        return result;
    }

    public static <K, T, U> void map(Map<K, T> in, Function<T, U> m, Map<K, U> out)
    {
        for(Map.Entry<K, T> e: in.entrySet())
        {
            out.put(e.getKey(), m.apply(in.get(e.getKey())));
        }
    }

    public static <T> boolean containsIdentity(T[] a, T x)
    {
        for(T t: a)
        {
            if(t == x)
            {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean contains(T[] a, T x)
    {
        for(T t: a)
        {
            if(t.equals(x))
            {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean equals(T[] a1, T[] a2)
    {
        if(a1.length == a2.length)
        {
            for(int i = 0; i < a1.length; i++)
            {
                if(a1[i] == null)
                {
                    if(a2[i] != null)
                    {
                        return false;
                    }
                }
                else
                {
                    if(!a1[i].equals(a2[i]))
                    {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    public static <T> void print(Collection<T> c, Function<T, String> m)
    {
        print(c, m, System.out);
    }

    public static <T> void print(Collection<T> c, Function<T, String> m, PrintStream stream)
    {
        boolean comma = false;
        stream.print("[");
        for(T t: c)
        {
            if(comma)
            {
                stream.print(", ");
            }
            else
            {
                comma = true;
            }

            stream.print(m.apply(t));
        }
        stream.println("]");
    }

    public static <T, U> Pair<T, U> asPair(T first, U second)
    {
        return new Pair<T,U>(first, second);
    }

    /**
     * Convert the properties instance into a map.
     *
     * @param properties    instance to be converted.
     * @return  the new map instance with the same values as contained
     * within the original properties instance.
     */
    public static Map<String, String> asMap(Properties properties)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Object propertyName : properties.keySet())
        {
            map.put((String) propertyName, properties.getProperty((String) propertyName));
        }
        return map;
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

    public static <T, U> Map<T, U> retainAll(Map<T, U> m, Map<T, U> n)
    {
        Iterator<Map.Entry<T, U>> it = m.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<T, U> entry = it.next();
            U value = n.get(entry.getKey());
            if(value == null || !value.equals(entry.getValue()))
            {
                it.remove();
            }
        }

        return m;
    }
    
    public static <T> void traverse(Collection<T> c, UnaryProcedure<T> f)
    {
        for (T t : c)
        {
            f.run(t);
        }
    }

    /**
     * In-place reversal of the elements of an object array.
     *
     * @param array the array to reverse
     */
    public static void reverse(Object[] array)
    {
        for (int i = 0, j = array.length - 1; i < j; i++, j--)
        {
            Object temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Get a new list with the items in the original list in reverse order.
     *
     * @param l     the list to be reversed.
     * @param <T>   the type contained by the list
     * @return  a new list with the elements of the original list in reverse order.
     */
    public static <T> List<T> reverse(List<T> l)
    {
        LinkedList<T> result = new LinkedList<T>();
        for (T t : l)
        {
            result.addFirst(t);
        }
        return result;
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
     * Creates a list holding count elements with the given value.
     *
     * @param value value to use for all list elements
     * @param count number of list elements to add
     * @return a list with count elements of value
     */
    public static <T> List<T> times(T value, int count)
    {
        return new LinkedList<T>(Collections.nCopies(count, value));
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

    /**
     * Returns the index of the specified object in the specified array.
     *
     * @param o     the object whose index is being determined
     * @param array the array in which the object is being searched
     * @return the index of the object in the array, or -1 if it is not
     * present.
     */
    public static <T> int indexOf(T o, T... array)
    {
        if (array != null)
        {
            for (int index = 0; index < array.length; index++)
            {
                if (ObjectUtils.equals(array[index], o))
                {
                    return index;
                }
            }
        }
        return -1;
    }
}
