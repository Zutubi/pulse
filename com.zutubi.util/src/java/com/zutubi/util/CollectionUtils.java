package com.zutubi.util;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 */
public class CollectionUtils
{
    public static <T> List<T> filter(Collection<T> l, Predicate<T> p)
    {
        return filter(l, p, (List<T>) new LinkedList<T>());
    }

    public static <T, U extends Collection<T>> U filter(Collection<T> in, Predicate<T> p, U out)
    {
        for(T t: in)
        {
            if(p.satisfied(t))
            {
                out.add(t);
            }
        }

        return out;
    }

    public static <T> List<T> filter(T[] l, Predicate<T> p)
    {
        List<T> result = new LinkedList<T>();
        filter(l, p, result);
        return result;
    }

    public static <T> void filter(T[] in, Predicate<T> p, Collection<T> out)
    {
        for(T t: in)
        {
            if(p.satisfied(t))
            {
                out.add(t);
            }
        }
    }

    public static <T> T[] filterToArray(T[] in, Predicate<T> predicate)
    {
        List<T> result = filter(in, predicate);
        return result.toArray((T[])Array.newInstance(in.getClass().getComponentType(), result.size()));
    }

    public static <T, U> List<U> map(Collection<T> l, Mapping<T, U> m)
    {
        List<U> result = new LinkedList<U>();
        map(l, m, result);
        return result;
    }

    public static <T, U, V extends Collection<U>> V map(Collection<T> in, Mapping<T, U> m, V out)
    {
        for(T t: in)
        {
            out.add(m.map(t));
        }

        return out;
    }

    public static <T, U> List<U> map(T[] in, Mapping<T, U> m)
    {
        List<U> result = new LinkedList<U>();
        map(in, m, result);
        return result;
    }

    public static <T, U, V extends Collection<U>> V map(T[] in, Mapping<T, U> m, V out)
    {
        for(T t: in)
        {
            out.add(m.map(t));
        }

        return out;
    }

    public static <T, U> U[] mapToArray(Iterable<T> iterable, Mapping<T, U> m, U[] out)
    {
        int i = 0;
        for(T t: iterable)
        {
            out[i++] = m.map(t);
        }

        return out;
    }

    public static <T, U> U[] mapToArray(T[] in, Mapping<T, U> m, U[] out)
    {
        return mapToArray(in, m, out, 0);
    }

    public static <T, U> U[] mapToArray(T[] in, Mapping<T, U> m, U[] out, int offset)
    {
        int i = 0;
        for(T t: in)
        {
            out[i++ + offset] = m.map(t);
        }

        return out;
    }

    public static <K, T, U> Map<K, U> map(Map<K, T> in, Mapping<T, U> m)
    {
        Map<K, U> result = new HashMap<K,U>(in.size());
        map(in, m, result);
        return result;
    }

    public static <K, T, U> void map(Map<K, T> in, Mapping<T, U> m, Map<K, U> out)
    {
        for(Map.Entry<K, T> e: in.entrySet())
        {
            out.put(e.getKey(), m.map(in.get(e.getKey())));
        }
    }

    public static <T> T find(Iterable<T> c, Predicate<T> p)
    {
        for(T t: c)
        {
            if(p.satisfied(t))
            {
                return t;
            }
        }

        return null;
    }

    public static <T> T find(T[] c, Predicate<T> p)
    {
        return find(Arrays.asList(c), p);
    }

    public static <T> boolean contains(Iterable<T> in, Predicate<T> p)
    {
        return find(in, p) != null;
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

    public static <T> Mapping<? extends T, T> identityMapping()
    {
        return new Mapping<T, T>()
        {
            public T map(T t)
            {
                return t;
            }
        };
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

    public static <T> void print(Collection<T> c, Mapping<T, String> m)
    {
        print(c, m, System.out);
    }

    public static <T> void print(Collection<T> c, Mapping<T, String> m, PrintStream stream)
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

            stream.print(m.map(t));
        }
        stream.println("]");
    }

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
        Vector<T> result = new Vector<T>(ts.length);
        for(T t: ts)
        {
            result.add(t);
        }
        
        return result;
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
            f.process(t);
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
        List<T> result = new LinkedList<T>();
        while (count-- > 0)
        {
            result.add(value);
        }

        return result;
    }

    /**
     * Caclulates the number of items in the given collection that satisfy the
     * given predicate.
     *
     * @param c         collection to count items from
     * @param predicate predicate that must be satisfied to include an item in
     *                  the count
     * @param <T> type of item in the collection
     * @return the number of items in the collection that satisfy the predicate
     */
    public static <T> int count(Collection<T> c, Predicate<T> predicate)
    {
        int count = 0;
        for (T t: c)
        {
            if (predicate.satisfied(t))
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Return a list of the items contained by the specified iterator.
     * The ordering of the iterator is preserved by the list.
     *
     * @param iterator  containing the items to turn into a list.
     * @return a list that is equivalent to the contents of the iterator.
     */
    public static <T> List<T> asList(Iterator<T> iterator)
    {
        List<T> result = new LinkedList<T>();
        while (iterator.hasNext())
        {
            result.add(iterator.next());
        }
        return result;
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

}
