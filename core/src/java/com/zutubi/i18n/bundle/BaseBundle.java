package com.zutubi.i18n.bundle;

import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
public abstract class BaseBundle extends ResourceBundle
{
    public BaseBundle()
    {
    }

    public void setParent(ResourceBundle parent)
    {
        if (this == parent)
        {
            throw new IllegalArgumentException("Can't set bundle as its own parent.");
        }
        this.parent = parent;
    }

    public ResourceBundle getParent()
    {
        return parent;
    }
}