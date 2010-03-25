package com.zutubi.util;

/**
 * A mapping that represents two delegate mappings.
 */
public class CompositeMapping<X, Y, Z> implements Mapping<X, Z>
{
    private Mapping<X, Y> first;
    private Mapping<Y, Z> second;

    public CompositeMapping(Mapping<X, Y> first, Mapping<Y, Z> second)
    {
        this.first = first;
        this.second = second;
    }

    public Z map(X x)
    {
        return second.map(first.map(x));
    }
}
