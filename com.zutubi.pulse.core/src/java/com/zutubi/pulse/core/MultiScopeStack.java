package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ReferenceMap;

import java.util.Collection;

/**
 * Stores a stack of named scopes where the roots of scopes are tied to
 * the leaves of earlier scopes.  This forms a long scope chain that can be
 * used as a single scope.  When a specific scope is required, it can be
 * looked up by name.
 */
public class MultiScopeStack implements ReferenceMap
{
    private String[] names;
    private PulseScope[] roots;
    private PulseScope[] leaves;
    private PulseScope leaf = null;

    @SuppressWarnings({"UnusedDeclaration"})
    private MultiScopeStack()
    {
        // For hessian
    }
    
    public MultiScopeStack(String... names)
    {
        if(names.length == 0)
        {
            throw new IllegalArgumentException("At least one name must be specified");
        }

        initArrays(names);
        for(int i = 0; i < names.length; i++)
        {
            leaf = new PulseScope(leaf);
            roots[i] = leaves[i] = leaf;
        }
    }

    public MultiScopeStack(MultiScopeStack other)
    {
        initArrays(other.names);
        
        for(int i = 0; i < names.length; i++)
        {
            leaves[i] = other.leaves[i].copyTo(other.roots[i].getParent());
            roots[i] = leaves[i].getRoot();
            if(i > 0)
            {
                roots[i].setParent(leaves[i - 1]);
            }
        }

        leaf = leaves[leaves.length - 1];
    }

    private void initArrays(String... names)
    {
        this.names = new String[names.length];
        System.arraycopy(names, 0, this.names, 0, names.length);
        roots = new PulseScope[names.length];
        leaves = new PulseScope[names.length];
    }

    public String getLabel()
    {
        return leaf.getLabel();
    }

    public void setLabel(String label)
    {
        for(PulseScope scope: leaves)
        {
            scope.setLabel(label);
        }
    }

    public void push()
    {
        for(int i = 0; i < leaves.length; i++)
        {
            leaves[i] = leaves[i].createChild();
            if (i > 0)
            {
                roots[i].setParent(leaves[i - 1]);
            }
        }

        leaf = leaves[leaves.length - 1];
    }

    public void pop()
    {
        if(leaves[0].getParent() == null)
        {
            throw new IllegalStateException("Attempt to pop an empty stack");
        }

        for(int i = 0; i < leaves.length; i++)
        {
            leaves[i] = leaves[i].getParent();
            if(i > 0)
            {
                roots[i].setParent(leaves[i - 1]);
            }
        }

        leaf = leaves[leaves.length - 1];
    }

    public void popTo(String label)
    {
        while(!label.equals(getLabel()) & leaves[0].getParent() != null)
        {
            pop();
        }
    }

    public PulseScope getScope()
    {
        return leaf;
    }

    public PulseScope getScope(String name)
    {
        for(int i = 0; i < names.length; i++)
        {
            if(names[i].equals(name))
            {
                return leaves[i];
            }
        }

        throw new IllegalArgumentException("No such scope '" + name + "'");
    }

    public boolean containsReference(String name)
    {
        return getScope().containsReference(name);
    }

    public Reference getReference(String name)
    {
        return getScope().getReference(name);
    }

    public Collection<Reference> getReferences()
    {
        return getScope().getReferences();
    }

    public void add(Reference reference)
    {
        getScope().add(reference);
    }

    public void addAll(Collection<? extends Reference> references)
    {
        getScope().addAll(references);
    }
}
