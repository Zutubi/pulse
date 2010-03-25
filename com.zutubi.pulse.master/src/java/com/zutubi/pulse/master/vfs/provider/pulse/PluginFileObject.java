package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.plugins.Plugin;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a single plugin.  Selection shows a detailed view of the plugin.
 */
public class PluginFileObject extends AbstractPulseFileObject
{
    private Plugin plugin;

    public PluginFileObject(FileName name, Plugin plugin, AbstractFileSystem fs)
    {
        super(name, fs);
        this.plugin = plugin;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return NO_CHILDREN;
    }

    public String getId()
    {
        return plugin.getId();
    }

    public String getDisplayName()
    {
        return plugin.getName();
    }

    public String getFileType() throws FileSystemException
    {
        switch(plugin.getState())
        {
            case DISABLED:
            case DISABLING:
            case UNINSTALLING:
                if(plugin.getErrorMessage() != null)
                {
                        return FileTypeConstants.PLUGIN_ERROR;
                }
                else
                {
                    return FileTypeConstants.PLUGIN_DISABLED;
                }
            default:
                return FileTypeConstants.PLUGIN;
        }
    }
}
