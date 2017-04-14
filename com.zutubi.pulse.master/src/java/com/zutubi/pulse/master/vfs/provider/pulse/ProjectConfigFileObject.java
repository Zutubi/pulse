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
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.vfs.provider.pulse.scm.ScmRootFileObject;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class ProjectConfigFileObject extends AbstractPulseFileObject implements ProjectConfigProvider, AddressableFileObject
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
    
    static
    {
        nodesDefinitions.put("scm", ScmRootFileObject.class);
    }

    private ConfigurationProvider configurationProvider;

    public ProjectConfigFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class<? extends AbstractPulseFileObject> clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> children = nodesDefinitions.keySet();
        return children.toArray(new String[children.size()]);
    }

    public ProjectConfiguration getProjectConfig()
    {
        try
        {
            return configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, UriParser.decode(getName().getBaseName())), ProjectConfiguration.class);
        }
        catch (FileSystemException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return Urls.getBaselessInstance().project(WebUtils.uriComponentEncode(getProjectConfig().getName()));
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
