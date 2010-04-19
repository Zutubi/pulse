package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.ToveFileLoadInterceptor;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * allow unresolved references.
 */
public class CustomProjectValidationInterceptor implements ToveFileLoadInterceptor
{
    public boolean loadInstance(Configuration instance, Element element, Scope scope)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration instance, Element element)
    {
        return true;
    }

    public boolean validate(Configuration instance, Element element)
    {
        return true;
    }
}
