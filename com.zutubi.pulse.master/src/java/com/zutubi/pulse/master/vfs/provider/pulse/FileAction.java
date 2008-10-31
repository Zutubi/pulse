package com.zutubi.pulse.master.vfs.provider.pulse;

/**
 * Represents an action that a user can perform on a file.  The actions are
 * rendered as linked icons in the UI, and include such things as selecting 
 */
public class FileAction
{
    /** The action type determines how it is rendered. */
    private String type;
    /** The URL for the action link. */
    private String url;

    public FileAction(String type, String url)
    {
        this.type = type;
        this.url = url;
    }

    public String getType()
    {
        return type;
    }

    public String getUrl()
    {
        return url;
    }
}
