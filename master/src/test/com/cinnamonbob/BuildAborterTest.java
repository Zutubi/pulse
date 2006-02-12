package com.cinnamonbob;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.MockBuildManager;
import com.cinnamonbob.model.MockProjectManager;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.test.BobTestCase;

/**
 */
public class BuildAborterTest extends BobTestCase
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
        projectManager.save(new Project("hello", "test project"));
        aborter.run();
    }

    public void testCompletedBuild()
    {
        Project project = new Project("test", "project");
        BuildResult result = new BuildResult(project, null, 1);
        result.commence(10);
        result.complete();

        projectManager.save(project);
        buildManager.save(result);

        assertTrue(result.succeeded());
        aborter.run();
        assertTrue(result.succeeded());
    }

    public void testIncompleteBuild()
    {
        Project project = new Project("test", "project");
        BuildResult result = new BuildResult(project, null, 1);
        result.commence(10);

        projectManager.save(project);
        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());
        aborter.run();
        assertTrue(result.errored());
        assertTrue(result.getErrorMessage().contains("shut down"));
    }
}
