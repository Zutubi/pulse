package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.mock.MockEntityDao;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.RandomUtils;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BuildAborterStartupTaskTest extends PulseTestCase
{
    private BuildManager buildManager;
    private DefaultProjectManager projectManager;
    private BuildAborterStartupTask aborter;
    private UserManager userManager;

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);

        projectManager = new DefaultProjectManager();
        projectManager.setProjectDao(new MockProjectDao());
        projectManager.setBuildManager(buildManager);

        aborter = new BuildAborterStartupTask();
        aborter.setProjectManager(projectManager);
        aborter.setBuildManager(buildManager);
        aborter.setTransactionContext(new TransactionContext());

        userManager = mock(UserManager.class);
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
        Project project = createProject();
        BuildResult result = createResult(project);
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
        Project project = createProject();
        BuildResult result = createResult(project);
        result.commence(10);

        projectManager.save(project);
        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());
        aborter.execute();
        verify(buildManager, times(1)).abortUnfinishedBuilds(project, BuildAborterStartupTask.ABORT_MESSAGE);
    }

    public void testCompletePersonalBuild()
    {
        Project project = createProject();
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
        Project project = createProject();
        User user = newUser();
        BuildResult result = new BuildResult(new PersonalBuildReason(user.getLogin()), user, project, 1);
        result.commence(10);

        buildManager.save(result);

        assertTrue(result.commenced());
        assertFalse(result.completed());

        wireMockUserManager(user);
        aborter.execute();
        verify(buildManager, times(1)).abortUnfinishedBuilds(user, BuildAborterStartupTask.ABORT_MESSAGE);
    }

    private Project createProject()
    {
        ProjectConfiguration config = new ProjectConfiguration();
        Project project = new Project();
        project.setConfig(config);
        return project;
    }

    private BuildResult createResult(Project project)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("test trigger"), project, 1, false);
        result.setId(RandomUtils.randomInt());
        return result;
    }

    private void wireMockUserManager(User... users)
    {
        doReturn(Arrays.asList(users)).when(userManager).getAllUsers();
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
