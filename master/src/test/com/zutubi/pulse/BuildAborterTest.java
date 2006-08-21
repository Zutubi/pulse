package com.zutubi.pulse;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class BuildAborterTest extends PulseTestCase
{
    private MockProjectManager projectManager;
    private MockBuildManager buildManager;
    private BuildAborter aborter;

    protected void setUp() throws Exception
    {
        projectManager = new MockProjectManager();
        buildManager = new MockBuildManager();
        aborter = new BuildAborter();
        aborter.setProjectManager(projectManager);
        aborter.setBuildManager(buildManager);
    }

    public void testNoProjects()
    {
        aborter.run();
    }

    public void testNoBuilds()
    {
        projectManager.create(new Project("hello", "test project"));
        aborter.run();
    }

    public void testCompletedBuild()
    {
        Project project = new Project("test", "project");
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, null, 1);
        result.commence(10);
        result.complete();

        projectManager.create(project);
        buildManager.save(result);

        assertTrue(result.succeeded());
        aborter.run();
        assertTrue(result.succeeded());
    }

    public void testIncompleteBuild()
    {
        Project project = new Project("test", "project");
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, null, 1);
        result.commence(10);

        projectManager.create(project);
        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());
        aborter.run();
        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("shut down"));
    }
}
