package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * The default interceptor, which allows full loading and validation.
 */
public class DefaultToveFileLoadInterceptor implements ToveFileLoadInterceptor
{
    public boolean loadInstance(Configuration instance, Element element, Scope scope)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration instance, Element element)
    {
        return false;
    }

    public boolean validate(Configuration instance, Element element)
    {
        return true;
    }
}
