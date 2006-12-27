package com.zutubi.pulse.core;

/**
 * During loading, any component that implements this interface will have the current scope injected
 * via ScopeAware#setScope 
 *
 *
 */
public interface ScopeAware
{
    public void setScope(Scope scope);
}
