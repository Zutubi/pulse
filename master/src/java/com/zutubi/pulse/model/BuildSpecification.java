/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.List;
import java.util.LinkedList;

/**
 * Describes the steps (recipes) required for a build, and where they should
 * be executed.
 */
public class BuildSpecification extends Entity
{
    public static final int TIMEOUT_NEVER = 0;

    private String name;
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
