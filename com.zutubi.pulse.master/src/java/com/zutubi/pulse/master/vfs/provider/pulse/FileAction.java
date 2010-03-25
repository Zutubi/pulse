package com.zutubi.pulse.master.vfs.provider.pulse;

/**
 * Represents an action that a user can perform on a file.  The actions are
 * rendered as linked icons in the UI, and include such things as selecting 
 */
public class FileAction
{
    /**
     * This action url represents the ability to download the file.
     */
    public static final String TYPE_DOWNLOAD = "download";
    /**
     * This action url represents the ability to download an archived version
     * of this file.
     */
    public static final String TYPE_ARCHIVE = "archive";
    /**
     * This action url represents a link to viewing an online representation of
     * the file.
     */
    public static final String TYPE_LINK = "link";
    /**
     * This action url represents the ability to view a decorated version
     * of the file.
     */
    public static final String TYPE_DECORATE = "decorate";
    /**
     * Similar to the download action, but used when the target in HTML where
     * it will be viewed in the browser.
     */
    public static final String TYPE_VIEW = "view";

    /**
     * This action type determines how it is rendered.
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
