/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.ToPluginIdFunction;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

import static com.google.common.collect.Collections2.transform;

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

        return objectFactory.buildBean(PluginFileObject.class, fileName, plugin, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<Plugin> allPlugins = pluginManager.getPlugins();
        return UriParser.encode(transform(allPlugins, new ToPluginIdFunction()).toArray(new String[allPlugins.size()]));
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
