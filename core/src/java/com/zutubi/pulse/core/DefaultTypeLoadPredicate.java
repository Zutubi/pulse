package com.zutubi.pulse.core;

import nu.xom.Element;

/**
 * <class comment/>
 */
public class DefaultTypeLoadPredicate implements TypeLoadPredicate
{
    public boolean loadType(Object type, Element element)
    {
        return true;
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return true;
    }
}
