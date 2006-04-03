package com.cinnamonbob.core;

import nu.xom.Element;

/**
 */
public interface TypeLoadPredicate
{
    boolean loadType(Object type, Element element);
    boolean resolveReferences(Object type, Element element);
}
