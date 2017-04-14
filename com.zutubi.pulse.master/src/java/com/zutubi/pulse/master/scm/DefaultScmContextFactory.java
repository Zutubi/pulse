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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeUtils;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.scm.PersistentContextImpl;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the scm context factory interface.
 */
public class DefaultScmContextFactory implements MasterScmContextFactory
{
    private File projectsDir;
    private final Map<Long, PersistentContextImpl> persistentContexts = new HashMap<Long, PersistentContextImpl>();

    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;
    private ResourceRepository resourceRepository;

    public ScmContext createContext(ScmConfiguration scmConfiguration, String implicitResource)
    {
        if (scmConfiguration.getConfigurationPath() != null)
        {
            ProjectConfiguration projectConfiguration = configurationProvider.getAncestorOfType(scmConfiguration, ProjectConfiguration.class);
            if (projectConfiguration != null)
            {
                Project project = projectManager.getProject(projectConfiguration.getProjectId(), false);
                if (project != null)
                {
                    try
                    {
                        return createContext(projectConfiguration, project.getState(), implicitResource);
                    }
                    catch (ScmException e)
                    {
                        // Fall through to default.
                    }
                }
            }
        }

        return createContext(implicitResource);
    }

    public ScmContext createContext(String implicitResource)
    {
        return new ScmContextImpl(null, getEnvironmentContext(null, implicitResource));
    }

    public ScmContext createContext(ProjectConfiguration projectConfiguration, Project.State projectState, String implicitResource) throws ScmException
    {
        try
        {
            ScmConfiguration config = projectConfiguration.getScm();
            PersistentContextImpl persistentContext = null;
            if (projectState.isInitialised())
            {
                synchronized(persistentContexts)
                {
                    persistentContext = persistentContexts.get(config.getHandle());
                    if (persistentContext == null)
                    {
                        persistentContext = new PersistentContextImpl(getPersistentWorkingDir(projectConfiguration.getProjectId()));
                        persistentContexts.put(config.getHandle(), persistentContext);
                    }
                }
            }

            return new ScmContextImpl(persistentContext, getEnvironmentContext(projectConfiguration, implicitResource));
        }
        catch (IOException e)
        {
            throw new ScmException("IO Failure creating scm context. " + e.getMessage(), e);
        }
    }

    private File getPersistentWorkingDir(long id) throws IOException
    {
        // Question: should the construction of the specific project path be done here, or via
        // an appropriate paths object?.
        File projectDir = new File(projectsDir, String.valueOf(id));
        File workingDir = new File(projectDir, "scm");
        if (!workingDir.isDirectory() && !workingDir.mkdirs())
        {
            throw new IOException("Failed to create persistent working directory '" + workingDir.getCanonicalPath() + "'");
        }
        return workingDir;
    }

    private PulseExecutionContext getEnvironmentContext(ProjectConfiguration projectConfiguration, String implicitResource)
    {
        PulseExecutionContext environmentContext = new PulseExecutionContext();
        if (projectConfiguration != null)
        {
            MasterBuildProperties.addProjectProperties(environmentContext, projectConfiguration, true);
        }
        if (StringUtils.stringSet(implicitResource))
        {
            RecipeUtils.addResourceProperties(environmentContext, Arrays.asList(new ResourceRequirement(implicitResource, false, true)), resourceRepository);
        }
        return environmentContext;
    }

    public void setProjectsDir(File projectsDir)
    {
        this.projectsDir = projectsDir;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }
}
