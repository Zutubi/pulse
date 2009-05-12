package com.zutubi.pulse.master;

import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.bootstrap.tasks.BuildAborterStartupTask;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.mock.MockEntityDao;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BuildAborterTest extends PulseTestCase
{
    private BuildManager buildManager;
    private DefaultProjectManager projectManager;
    private BuildAborterStartupTask aborter;

    protected void setUp() throws Exception
    {
        buildManager = new MockBuildManager();

        projectManager = new DefaultProjectManager();
        projectManager.setProjectDao(new MockProjectDao());
        projectManager.setBuildManager(buildManager);

        aborter = new BuildAborterStartupTask();
        aborter.setProjectManager(projectManager);
        aborter.setBuildManager(buildManager);

        UserManager userManager = mock(UserManager.class);
        doReturn(Collections.emptyList()).when(userManager).getAllUsers();
        aborter.setUserManager(userManager);
    }

    public void testNoProjects()
    {
        aborter.execute();
    }

    public void testNoBuilds()
    {
        projectManager.save(new Project());
        aborter.execute();
    }

    public void testCompletedBuild()
    {
        Project project = new Project();
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, 1, false);
        result.commence(10);
        result.complete();

        projectManager.save(project);
        buildManager.save(result);

        assertTrue(result.succeeded());
        aborter.execute();
        assertTrue(result.succeeded());
    }

    public void testIncompleteBuild()
    {
        Project project = new Project();
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, 1, false);
        result.commence(10);

        projectManager.save(project);
        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());
        aborter.execute();
        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("shut down"));
    }

    public void testCompletePersonalBuild()
    {
        Project project = new Project();
        User user = newUser();
        BuildResult result = new BuildResult(new PersonalBuildReason(user.getLogin()), user, project, 1);
        result.commence(10);
        result.complete();

        buildManager.save(result);

        assertTrue(result.succeeded());

        wireMockUserManager(user);
        aborter.execute();

        assertTrue(result.succeeded());
    }

    public void testIncompletePersonalBuild()
    {
        Project project = new Project();
        User user = newUser();
        BuildResult result = new BuildResult(new PersonalBuildReason(user.getLogin()), user, project, 1);
        result.commence(10);

        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());

        wireMockUserManager(user);
        aborter.execute();

        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("shut down"));
    }

    private void wireMockUserManager(User... users)
    {
        UserManager userManager = mock(UserManager.class);
        doReturn(Arrays.asList(users)).when(userManager).getAllUsers();
        aborter.setUserManager(userManager);
    }

    private User newUser()
    {
        UserConfiguration config = new UserConfiguration("test", "test");
        User user = new User();
        user.setId(1);
        user.setConfig(config);
        return user;
    }

    public static class MockProjectDao extends MockEntityDao<Project> implements ProjectDao
    {
        public List<Project> findByResponsible(User user)
        {
            throw new RuntimeException("Not implemented");
        }
    }
}
