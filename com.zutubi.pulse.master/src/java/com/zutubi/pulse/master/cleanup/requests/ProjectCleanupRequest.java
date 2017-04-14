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

package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.cleanup.config.AbstractCleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.RetainConfiguration;
import com.zutubi.pulse.master.model.BuildCleanupOptions;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A request to cleanup a projects builds.  Which builds and what specifically
 * are cleaned up is dependent upon the projects configured cleanup rules.
 */
public class ProjectCleanupRequest extends EntityCleanupRequest
{
    private BuildResultDao buildResultDao;
    private BuildManager buildManager;

    private Project project;

    public ProjectCleanupRequest(Project project)
    {
        super(project);
        this.project = project;
    }

    public void run()
    {
        ProjectConfiguration projectConfig = project.getConfig();
        @SuppressWarnings({"unchecked"})
        Map<String, AbstractCleanupConfiguration> cleanupConfigs = (Map<String, AbstractCleanupConfiguration>) projectConfig.getExtensions().get(MasterConfigurationRegistry.EXTENSION_PROJECT_CLEANUP);

        if (cleanupConfigs != null)
        {
            Set<BuildResult> excludedBuilds = new HashSet<BuildResult>();
            for (AbstractCleanupConfiguration rule : cleanupConfigs.values())
            {
                if (rule instanceof RetainConfiguration)
                {
                    excludedBuilds.addAll(rule.getMatchingResults(project, buildResultDao));
                }
            }

            for (AbstractCleanupConfiguration rule : cleanupConfigs.values())
            {
                if (rule instanceof CleanupConfiguration)
                {
                    CleanupConfiguration cleanupRule = (CleanupConfiguration) rule;
                    List<BuildResult> oldBuilds = cleanupRule.getMatchingResults(project, buildResultDao);
                    oldBuilds.removeAll(excludedBuilds);

                    for (BuildResult build : oldBuilds)
                    {
                        BuildCleanupOptions options = new BuildCleanupOptions(false);
                        if (cleanupRule.isCleanupAll())
                        {
                            options = new BuildCleanupOptions(true);
                        }
                        else
                        {
                            if (cleanupRule.getWhat() != null)
                            {
                                options = new BuildCleanupOptions(cleanupRule.getWhat());
                            }
                        }

                        buildManager.cleanup(build, options);
                    }
                }
            }
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
