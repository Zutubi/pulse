package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.FileLoadException;

/**
 * Allow for custom pulse file tags to be defined and registered within the Pulse file.
 *
 * For example:
 *   <register name="customTag" class="com.example.custom.Tag"/>
 *
 *   <customTag/>
 */
public class Register implements InitComponent, FileLoaderAware
{
    /**
     * The name of the tag as it will appear in the pulse file.
     */
    private String tagName;
    /**
     * The class name of the class that implements the tag.
     */
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
