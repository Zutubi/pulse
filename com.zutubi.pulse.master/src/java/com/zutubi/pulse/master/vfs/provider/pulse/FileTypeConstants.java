package com.zutubi.pulse.master.vfs.provider.pulse;

/**
 * A set of constants used to help render the file types by providing
 * extra details over the standard file/folder designation.
 */
public class FileTypeConstants
{
    /**
     * The file object represents a classical file.
     */
    public static final String FILE = "file";
    /**
     * The file object represents a classical folder, a container of other
     * file objects.
     */
    public static final String FOLDER = "folder";
    /**
     * The file object represents a message to be displayed to the user.
     */
    public static final String MESSAGE = "message";
    /**
     * The file object represents the in progress state, used as a placeholder
     * whilst the actual file object is being resolved.
     */
    public static final String IN_PROGRESS = "progress";
    /**
     * The file object is of unknown type.
     */
    public static final String UNKNOWN = "unknown";
    /**
     * The file object represents something that is viewable via html.
     */
    public static final String HTML_REPORT = "html";
    /**
     * The file object represents a placeholder to a file that is not
     * available.
     */
    public static final String BROKEN = "broken";
    /**
     * The file object represents a link to another resource.
     */
    public static final String LINK = "link";
    /**
     * The file object represents a plugin
     */
    public static final String PLUGIN = "plugin";
    /**
     * The file object represents a disabled plugin
     */
    public static final String PLUGIN_DISABLED = "plugin_disabled";
    /**
     * The file object represents a plugin that is in the error state.
     */
    public static final String PLUGIN_ERROR = "plugin_error";
}
