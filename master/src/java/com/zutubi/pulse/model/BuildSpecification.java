package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Describes the steps (recipes) required for a build, and where they should
 * be executed.
 */
public class BuildSpecification extends Entity implements NamedEntity
{
    public static final int TIMEOUT_NEVER = 0;

    private String name;
    private boolean isolateChangelists = false;
    private boolean retainWorkingCopy = false;
    private int timeout = TIMEOUT_NEVER;
    private BuildSpecificationNode root = new BuildSpecificationNode(null);

    public BuildSpecification()
    {

    }

    public BuildSpecification(String name)
    {
        this.name = name;
    }

    public BuildSpecification copy()
    {
        BuildSpecification copy = new BuildSpecification(name);
        copy.timeout = timeout;
        copy.isolateChangelists = isolateChangelists;
        copy.root = root.copy();
        return copy;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Boolean getIsolateChangelists()
    {
        return isolateChangelists;
    }

    public void setIsolateChangelists(Boolean isolateChangelists)
    {
        if(isolateChangelists == null)
        {
            isolateChangelists = Boolean.FALSE;
        }

        this.isolateChangelists = isolateChangelists;
    }

    public boolean getRetainWorkingCopy()
    {
        return retainWorkingCopy;
    }

    public void setRetainWorkingCopy(boolean retainWorkingCopy)
    {
        this.retainWorkingCopy = retainWorkingCopy;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public String getPrettyTimeout()
    {
        if (timeout == TIMEOUT_NEVER)
        {
            return "[never]";
        }
        else
        {
            return timeout + " minutes";
        }
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public BuildSpecificationNode getRoot()
    {
        return root;
    }

    public void setRoot(BuildSpecificationNode root)
    {
        this.root = root;
    }

    public BuildSpecificationNode getNode(long id)
    {
        return root.getNode(id);
    }

    public BuildSpecificationNode getNodeByStageName(String name)
    {
        return root.getNodeByStageName(name);
    }
}
