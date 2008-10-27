package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.Scope;

import java.util.Map;
import java.util.TreeMap;

/**
 * Test type for file loader: ensures adders that also take a scope are
 * supported.
 */
public class ScopeAcceptingRoot
{
    private Map<String, Reference> references = new TreeMap<String, Reference>();
    private Map<String, Scope> scopes = new TreeMap<String, Scope>();

    public void addRef(Reference r, Scope s)
    {
        references.put(r.getName(), r);
        scopes.put(r.getName(), s);
    }

    public void add(Reference r, Scope s)
    {
        references.put(r.getName(), r);
        scopes.put(r.getName(), s);
    }

    public Reference getReference(String name)
    {
        return references.get(name);
    }

    public Scope getScope(String name)
    {
        return scopes.get(name);
    }
}
