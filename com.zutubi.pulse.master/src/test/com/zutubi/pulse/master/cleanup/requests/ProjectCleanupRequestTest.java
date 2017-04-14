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

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;

public class ProjectCleanupRequestTest extends PulseTestCase
{
    private BuildManager buildManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildManager = mock(BuildManager.class);
    }

    public void testMultipleMatchingBuilds()
    {
        Project project = createProject("project");
        BuildResult resultA = createResult(project, 1);
        BuildResult resultB = createResult(project, 2);
        addCleanupRule(project, "a", null, resultA, resultB);

        ProjectCleanupRequest request = new ProjectCleanupRequest(project);
        request.setBuildManager(buildManager);
        request.run();

        BuildCleanupOptions options = new BuildCleanupOptions(true);

        verify(buildManager, times(1)).cleanup(resultA, options);
        verify(buildManager, times(1)).cleanup(resultB, options);
    }

    public void testMultipleCleanupRules()
    {
        Project project = createProject("project");
        BuildResult resultA = createResult(project, 1);
        addCleanupRule(project, "a", CleanupWhat.BUILD_ARTIFACTS, resultA);

        ProjectCleanupRequest request = new ProjectCleanupRequest(project);
        request.setBuildManager(buildManager);
        request.run();

        verify(buildManager, times(1)).cleanup(resultA, new BuildCleanupOptions(CleanupWhat.BUILD_ARTIFACTS));
    }

    private void addCleanupRule(Project project, String name, CleanupWhat what, BuildResult... results)
    {
        ProjectConfiguration config = project.getConfig();
        if (!config.getExtensions().containsKey("cleanup"))
        {
            config.getExtensions().put("cleanup", new HashMap<String, CleanupConfiguration>());
        }
        HashMap<String, CleanupConfiguration> cleanups = (HashMap<String, CleanupConfiguration>) config.getExtensions().get("cleanup");

        CleanupConfiguration cleanupConfig = mock(CleanupConfiguration.class);
        if (what != null)
        {
            stub(cleanupConfig.getWhat()).toReturn(Arrays.asList(what));
            stub(cleanupConfig.isCleanupAll()).toReturn(false);
        }
        else
        {
            stub(cleanupConfig.isCleanupAll()).toReturn(true);
        }
        stub(cleanupConfig.getName()).toReturn(name);
        stub(cleanupConfig.getMatchingResults((Project)anyObject(), (BuildResultDao)anyObject())).toReturn(Arrays.asList(results));
        cleanups.put(name, cleanupConfig);
    }

    private BuildResult createResult(Project project, long id)
    {
        BuildResult result = new BuildResult(new ManualTriggerBuildReason(), project, 1, false);
        result.setId(id);
        return result;
    }

    private Project createProject(String name)
    {
        ProjectConfiguration config = new ProjectConfiguration();
        config.setName(name);

        Project project = new Project();
        project.setConfig(config);

        return project;
    }
}
