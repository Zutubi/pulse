package com.zutubi.pulse.model;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.PersistentName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes the steps (recipes) required for a build, and where they should
 * be executed.
 */
public class BuildSpecification extends Entity implements NamedEntity
{
    public static final int TIMEOUT_NEVER = 0;

    /**
     * The checkout scheme defines the maner in which a projects source is bootstrapped.
     */
    public enum CheckoutScheme
    {
        /**
         * Always checkout a fresh copy of the project to the base directory.
         */
        CLEAN_CHECKOUT,

        /**
         * Keep a local copy of the project, update it to the required
         * revision and copy to a clean base directory to build.
         */
        CLEAN_UPDATE,

        /**
         * Keep a copy of the project, update to the required revision and
         * build in place.
         */
        INCREMENTAL_UPDATE
    }

    private PersistentName pname;
    private boolean isolateChangelists = false;
    private boolean retainWorkingCopy = false;
    private int timeout = TIMEOUT_NEVER;
    private CheckoutScheme checkoutScheme = CheckoutScheme.CLEAN_CHECKOUT;
    private BuildSpecificationNode root = new BuildSpecificationNode(null);
    private boolean forceClean;
    private List<ResourceProperty> properties = new LinkedList<ResourceProperty>();
    private boolean prompt = false;

    public BuildSpecification()
    {

    }

    public BuildSpecification(String name)
    {
        this.pname = new PersistentName(name);
    }

    public BuildSpecification copy()
    {
        BuildSpecification copy = new BuildSpecification(pname.getName());
        copy.timeout = timeout;
        copy.retainWorkingCopy = retainWorkingCopy;
        copy.isolateChangelists = isolateChangelists;
        copy.checkoutScheme = checkoutScheme;
        copy.root = root.copy();
        copy.properties = copyProperties();
        copy.prompt = prompt;
        return copy;
    }

    public String getName()
    {
        if(pname == null)
        {
            return null;
        }
        
        return pname.getName();
    }

    public void setName(String name)
    {
        if(pname == null)
        {
            pname = new PersistentName(name);
        }
        else
        {
            pname.setName(name);
        }
    }

    public PersistentName getPname()
    {
        return pname;
    }

    public void setPname(PersistentName pname)
    {
        this.pname = pname;
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

    public CheckoutScheme getCheckoutScheme()
    {
        return checkoutScheme;
    }

    public void setCheckoutScheme(CheckoutScheme checkoutScheme)
    {
        this.checkoutScheme = checkoutScheme;
    }

    /**
     * Used by hibernate to persist the checkout scheme value.
     */
    private String getCheckoutSchemeName()
    {
        return checkoutScheme.toString();
    }

    /**
     * Used by hibernate to persist the checkout scheme value.
     */
    private void setCheckoutSchemeName(String str)
    {
        checkoutScheme = CheckoutScheme.valueOf(str);
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

    public boolean getForceClean()
    {
        return forceClean;
    }

    public void setForceClean(boolean forceClean)
    {
        this.forceClean = forceClean;
    }

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<ResourceProperty> properties)
    {
        this.properties = properties;
    }

    public ResourceProperty getProperty(String name)
    {
        for(ResourceProperty p: properties)
        {
            if(p.getName().equals(name))
            {
                return p;
            }
        }

        return null;
    }

    public boolean hasProperty(String name)
    {
        return getProperty(name) != null;
    }

    public void addProperty(ResourceProperty property)
    {
        properties.add(property);
    }

    public List<ResourceProperty> copyProperties()
    {
        List<ResourceProperty> result = new ArrayList<ResourceProperty>(properties.size());
        for(ResourceProperty property: properties)
        {
            ResourceProperty copyProperty = property.copy();
            result.add(copyProperty);
        }

        return result;
    }

    public boolean getPrompt()
    {
        return prompt;
    }

    public void setPrompt(boolean prompt)
    {
        this.prompt = prompt;
    }


    public boolean equals(Object other)
    {
        return super.equals(other);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
