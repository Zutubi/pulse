package com.zutubi.pulse.core;

import nu.xom.Element;

/**
 */
public interface TypeLoadPredicate
{
    boolean loadType(Object type, Element element);
    boolean resolveReferences(Object type, Element element);
    boolean allowUnresolved(Object type, Element element);
}
