package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
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
        return UriParser.encode(CollectionUtils.mapToArray(allPlugins, new Mapping<Plugin, String>()
        {
            public String map(Plugin plugin)
            {
                return plugin.getId();
            }
        }, new String[allPlugins.size()]));
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
