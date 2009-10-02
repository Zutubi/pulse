package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 */
public class DefaultTypeLoadPredicate implements TypeLoadPredicate
{
    public boolean loadType(Configuration type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration type, Element element)
    {
        return false;
    }

    public boolean validate(Configuration type, Element element)
    {
        return true;
    }
}
