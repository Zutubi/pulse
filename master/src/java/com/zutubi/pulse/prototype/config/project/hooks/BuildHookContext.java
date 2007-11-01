package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.core.Scope;

/**
 */
public class BuildHookContext
{
    private Scope scope;

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }
}
