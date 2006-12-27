package com.zutubi.pulse.core;

import nu.xom.Element;

/**
 * The type load predicate allows control over the loading of types and resolving of references
 * during the pulse file loading process.
 *  
 */
public interface TypeLoadPredicate
{
    /**
     * Returns true if the type should be loaded from the xml element.
     *
     * @param type whose loading is being checked.
     * @param element that will provide the data for the type loading.
     *
     * @return true if the type should be loaded, false otherwise.
     */
    boolean loadType(Object type, Element element);

    
    boolean resolveReferences(Object type, Element element);
}
