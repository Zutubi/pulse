package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.ToPluginIdFunction;
import com.zutubi.util.CollectionUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

/**
 * A file to represent all installed plugins.  Viewing gives a summary of all
 * plugins.
 */
public class PluginsFileObject extends AbstractPulseFileObject
{
    private PluginManager pluginManager;

    public PluginsFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        Plugin plugin = pluginManager.getPlugin(fileName.getBaseName());
        if(plugin == null)
        {
            throw new PulseRuntimeException("Unknown plugin [" + fileName.getBaseName() + "]");
        }

        return objectFactory.buildBean(PluginFileObject.class,
                new Class[]{FileName.class, Plugin.class, AbstractFileSystem.class},
                new Object[]{fileName, plugin, pfs}
        );

    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<Plugin> allPlugins = pluginManager.getPlugins();
        return UriParser.encode(CollectionUtils.mapToArray(allPlugins, new ToPluginIdFunction(), new String[allPlugins.size()]));
    }

    @Override
    public String getIconCls()
    {
        return "plugins-icon";
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
