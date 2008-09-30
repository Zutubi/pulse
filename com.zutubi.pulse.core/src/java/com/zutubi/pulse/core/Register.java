package com.zutubi.pulse.core;

/**
 *
 *
 */
public class Register implements InitComponent, FileLoaderAware
{
    private String tagName;
    private String className;

    private FileLoader loader;

    public void setFileLoader(FileLoader loader)
    {
        this.loader = loader;
    }

    public void setName(String tagName)
    {
        this.tagName = tagName;
    }

    public void setClass(String className)
    {
        this.className = className;
    }

    public void initBeforeChildren() throws FileLoadException
    {
        // register this new tag with the current file loader instance.
        try
        {
            loader.register(tagName, Class.forName(className));
        }
        catch (ClassNotFoundException e)
        {
            throw new FileLoadException("Failed to register tag '"+tagName+"'. Cause: Unknown class '"+className+"'");
        }
    }

    public void initAfterChildren()
    {
        // no-op.
    }
}
