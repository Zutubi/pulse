package com.zutubi.pulse.master.vfs.provider.pulse;

/**
 * Represents an action that a user can perform on a file.  The actions are
 * rendered as linked icons in the UI, and include such things as selecting 
 */
public class FileAction
{
    /**
     * The action url represents the ability to download the file.
     */
    public static final String TYPE_DOWNLOAD = "download";
    /**
     * The action url represents the ability to download an archived version
     * of this file.
     */
    public static final String TYPE_ARCHIVE = "archive";
    /**
     * The action url represents a link to viewing an online representation of
     * the file.
     */
    public static final String TYPE_LINK = "link";
    /**
     * The action url represents the ability to view a decorated version
     * of the file.
     */
    public static final String TYPE_DECORATE = "decorate";

    /**
     * The action type determines how it is rendered.
     */
    private String type;

    /**
     * The URL for the action link.
     */
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
