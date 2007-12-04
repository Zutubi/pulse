package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.plugins.Plugin;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.pulse.core.PulseRuntimeException;

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

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
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
        List<Plugin> allPlugins = pluginManager.getNonInternalPlugins();
        return CollectionUtils.mapToArray(allPlugins, new Mapping<Plugin, String>()
        {
            public String map(Plugin plugin)
            {
                return plugin.getId();
            }
        }, new String[allPlugins.size()]);
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
