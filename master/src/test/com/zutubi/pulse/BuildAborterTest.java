package com.zutubi.pulse;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.ArrayList;
import java.util.Arrays;

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

        Mock mockUserManager = new Mock(UserManager.class);
        mockUserManager.expectAndReturn("getAllUsers", C.ANY_ARGS, new ArrayList<User>());
        aborter.setUserManager((UserManager) mockUserManager.proxy());
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
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, new BuildSpecification("foo"), 1, false);
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
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, new BuildSpecification("foo"), 1, false);
        result.commence(10);

        projectManager.create(project);
        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());
        aborter.run();
        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("shut down"));
    }

    public void testCompletePersonalBuild()
    {
        Project project = new Project("test", "project");
        User user = new User("u", "u");
        BuildResult result = new BuildResult(user, project, new BuildSpecification("foo"), 1);
        result.commence(10);
        result.complete();

        buildManager.save(result);

        assertTrue(result.succeeded());

        Mock mockUserManager = new Mock(UserManager.class);
        mockUserManager.expectAndReturn("getAllUsers", C.ANY_ARGS, Arrays.asList(new User[] { user }));
        aborter.setUserManager((UserManager) mockUserManager.proxy());
        aborter.run();

        assertTrue(result.succeeded());
    }

    public void testIncompletePersonalBuild()
    {
        Project project = new Project("test", "project");
        User user = new User("u", "u");
        BuildResult result = new BuildResult(user, project, new BuildSpecification("foo"), 1);
        result.commence(10);

        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());

        Mock mockUserManager = new Mock(UserManager.class);
        mockUserManager.expectAndReturn("getAllUsers", C.ANY_ARGS, Arrays.asList(new User[] { user }));
        aborter.setUserManager((UserManager) mockUserManager.proxy());
        aborter.run();

        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("shut down"));
    }
}
