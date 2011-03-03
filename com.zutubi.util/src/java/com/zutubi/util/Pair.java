package com.zutubi.util;

/**
 * Simple struct to hold two objects.
 */
public class Pair<T, U>
{
    public T first;
    public U second;

    public Pair(T first, U second)
    {
        this.first = first;
        this.second = second;
    }

    public T getFirst()
    {
        return first;
    }

    public U getSecond()
    {
        return second;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Pair pair = (Pair) o;
        if (first != null ? !first.equals(pair.first) : pair.first != null)
        {
            return false;
        }
        return !(second != null ? !second.equals(pair.second) : pair.second != null);
    }

    public int hashCode()
    {
        int result;
        result = (first != null ? first.hashCode() : 0);
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + ObjectUtils.toString(first) + "," + ObjectUtils.toString(second) + ")";
    }
}
