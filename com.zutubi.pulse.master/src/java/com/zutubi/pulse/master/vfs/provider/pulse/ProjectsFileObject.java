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

import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.api.Configurations;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import static com.google.common.collect.Iterables.transform;

public class ProjectsFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    public ProjectsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName)
    {
        // the fileName may be project name or the project id.
        long projectId = convertToProjectId(fileName.getBaseName());
        if (projectId != -1)
        {
            // Within the pulse file system, we use the projects id, not the projects name. So
            // lets reconstruct the adjusted name.
            String absPath = fileName.getParent().getPath() + "/" + projectId;

            fileName = new PulseFileName(fileName.getScheme(), absPath, fileName.getType());

            return objectFactory.buildBean(ProjectFileObject.class, fileName, projectId, pfs);
        }
        
        // we need to return a place holder here.
        return null;
    }

    private long convertToProjectId(String str)
    {
        try
        {
            return Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            ProjectConfiguration project = projectManager.getProjectConfig(str, true);
            if (project != null)
            {
                return project.getProjectId();
            }
        }
        return -1;
    }

    protected FileType doGetType() throws Exception
    {
        // allow traversal of this node.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Iterable<ProjectConfiguration> configs = projectManager.getAllProjectConfigs(false);
        return UriParser.encode(Iterables.toArray(transform(configs, Configurations.<ProjectConfiguration>toConfigurationName()), String.class));
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return Urls.getBaselessInstance().projects();
    }
}