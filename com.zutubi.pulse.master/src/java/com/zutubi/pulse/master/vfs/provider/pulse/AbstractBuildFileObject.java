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

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Helper base the implements most of the functionality for files that
 * represent a single build result.
 */
public abstract class AbstractBuildFileObject extends AbstractPulseFileObject implements BuildResultProvider, AddressableFileObject, ProjectProvider
{
    public AbstractBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        if (name.equals("artifacts"))
        {
            return objectFactory.buildBean(BuildArtifactsFileObject.class, fileName, pfs);
        }
        else if (name.equals("details"))
        {
            return objectFactory.buildBean(BuildDetailsFileObject.class, fileName, pfs);
        }
        else if (name.equals("stages"))
        {
            return objectFactory.buildBean(BuildStagesFileObject.class, fileName, pfs);
        }
        else if (name.equals("wc"))
        {
            accessManager.ensurePermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, getBuildResult());
            return objectFactory.buildBean(BuildWorkingCopiesFileObject.class, fileName, pfs);
        }
        else 
        {
            return null;
        }
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"artifacts", "details", "stages", "wc"};
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return "/browse/projects/" + getProjectConfig().getName() + "/builds/" + getBuildResult().getNumber() + "/";
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectManager.getProjectConfig(getProjectId(), false);
    }

    public Project getProject()
    {
        return getBuildResult().getProject();
    }

    public long getProjectId()
    {
        return getProject().getId();
    }

    public long getBuildResultId()
    {
        BuildResult result = getBuildResult();
        if (result != null)
        {
            return result.getId();
        }
        return -1;
    }

    public abstract BuildResult getBuildResult();
}
