package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 */
public interface TypeLoadPredicate
{
    boolean loadType(Configuration type, Element element);
    boolean allowUnresolved(Configuration type, Element element);
    boolean validate(Configuration type, Element element);
}
