package com.zutubi.util;

import java.io.PrintStream;
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

    public static String[] filterToArray(String[] in, Predicate<String> predicate)
    {
        List<String> result = filter(in, predicate);
        return result.toArray(new String[result.size()]);
    }

    public static <T, U> List<U> map(Collection<T> l, Mapping<T, U> m)
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

    public static <T, U> List<U> map(T[] in, Mapping<T, U> m)
    {
        List<U> result = new LinkedList<U>();
        map(in, m, result);
        return result;
    }

    public static <T, U> void map(T[] in, Mapping<T, U> m, Collection<U> out)
    {
        for(T t: in)
        {
            out.add(m.map(t));
        }
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
        int i = 0;
        for(T t: in)
        {
            out[i++] = m.map(t);
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

    public static <T, U> Pair<T, U> makePair(T first, U second)
    {
        return new Pair<T,U>(first, second);
    }

    public static <T, U> Map<T, U> makeHashMap(Collection<? extends Pair<? extends T, ? extends U>> pairs)
    {
        HashMap<T, U> result = new HashMap<T,U>(pairs.size());
        for(Pair<? extends T, ? extends U> pair: pairs)
        {
            result.put(pair.first, pair.second);
        }

        return result;
    }
}
