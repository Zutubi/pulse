package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.dependency.DependencyManager;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Arrays;

public class ProjectCleanupRequestTest extends PulseTestCase
{
    private BuildManager buildManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildManager = mock(BuildManager.class);
    }

    public void testWhatToOptionMappings()
    {
        assertWhatToOptionsMapping(CleanupWhat.BUILD_ARTIFACTS, BuildCleanupOptions.EXCEPT_DATABASE);
        assertWhatToOptionsMapping(CleanupWhat.WHOLE_BUILDS, BuildCleanupOptions.ALL);
        assertWhatToOptionsMapping(CleanupWhat.WORKING_DIRECTORIES_ONLY, BuildCleanupOptions.WORKD_DIR_ONLY);
    }

    public void testMultipleMatchingBuilds()
    {
        Project project = createProject("project");
        BuildResult resultA = createResult(project, 1);
        BuildResult resultB = createResult(project, 2);
        addCleanupRule(project, "a", CleanupWhat.WHOLE_BUILDS, resultA, resultB);

        ProjectCleanupRequest request = new ProjectCleanupRequest(project);
        request.setBuildManager(buildManager);
        request.process();

        verify(buildManager, times(1)).process(resultA, BuildCleanupOptions.ALL);
        verify(buildManager, times(1)).process(resultB, BuildCleanupOptions.ALL);
    }

    public void testMultipleCleanupRules()
    {
        Project project = createProject("project");
        BuildResult resultA = createResult(project, 1);
        addCleanupRule(project, "a", CleanupWhat.BUILD_ARTIFACTS, resultA);
        addCleanupRule(project, "b", CleanupWhat.WORKING_DIRECTORIES_ONLY, resultA);

        ProjectCleanupRequest request = new ProjectCleanupRequest(project);
        request.setBuildManager(buildManager);
        request.process();

        verify(buildManager, times(1)).process(resultA, BuildCleanupOptions.WORKD_DIR_ONLY);
        verify(buildManager, times(1)).process(resultA, BuildCleanupOptions.EXCEPT_DATABASE);
    }

    private void assertWhatToOptionsMapping(CleanupWhat what, BuildCleanupOptions options)
    {
        Project project = createProject("project");
        BuildResult result = createResult(project, 1);
        addCleanupRule(project, "a", what, result);

        ProjectCleanupRequest request = new ProjectCleanupRequest(project);
        request.setBuildManager(buildManager);
        request.process();

        verify(buildManager, times(1)).process(result, options);
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
        stub(cleanupConfig.getWhat()).toReturn(what);
        stub(cleanupConfig.getName()).toReturn(name);
        stub(cleanupConfig.getMatchingResults((Project)anyObject(), (BuildResultDao)anyObject(), (DependencyManager)anyObject())).toReturn(Arrays.asList(results));
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
