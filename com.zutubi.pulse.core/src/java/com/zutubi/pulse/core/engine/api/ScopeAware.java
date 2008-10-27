package com.zutubi.pulse.core.engine.api;

import com.zutubi.pulse.core.engine.api.Scope;

/**
 * Interface for types that are aware of the scope in which they are loaded.
 */
public interface ScopeAware
{
    public void setScope(Scope scope);
}
