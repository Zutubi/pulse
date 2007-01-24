package com.zutubi.pulse.util;

import java.util.*;

/**
 */
public class CollectionUtils
{
    public static <T> List<T> filter(List<T> l, Predicate<T> p)
    {
        List<T> result = new LinkedList<T>();
        filter(l, p, result);
        return result;
    }

    public static <T> void filter(Collection<T> in, Predicate<T> p, Collection<T> out)
    {
        for(T t: in)
        {
            if(p.satisfied(t))
            {
                out.add(t);
            }
        }
    }

    public static <T, U> List<U> map(List<T> l, Mapping<T, U> m)
    {
        List<U> result = new LinkedList<U>();
        map(l, m, result);
        return result;
    }

    public static <T, U> void map(Collection<T> in, Mapping<T, U> m, Collection<U> out)
    {
        for(T t: in)
        {
            out.add(m.map(t));
        }
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

    public static <T> T find(Collection<T> c, Predicate<T> p)
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
}
