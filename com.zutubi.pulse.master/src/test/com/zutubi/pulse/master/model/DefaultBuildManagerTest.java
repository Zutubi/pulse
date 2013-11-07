package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import static com.zutubi.util.RandomUtils.insecureRandomInt;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class DefaultBuildManagerTest extends PulseTestCase
{
    private DefaultBuildManager buildManager;
    private BuildResultDao buildResultDao;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildResultDao = mock(BuildResultDao.class);

        buildManager = new DefaultBuildManager();
        buildManager.setBuildResultDao(buildResultDao);
    }

    public void testAbortUnfinishedBuilds()
    {
        Project project = new Project();

        BuildResult result = createResult(project);
        result.commence(10);

        stub(buildResultDao.queryBuilds((Project[])anyObject(), (ResultState[]) anyObject(), anyLong(), anyLong(), anyInt(), anyInt(), anyBoolean())).toReturn(asList(result));

        assertTrue(result.commenced());
        assertFalse(result.completed());

        buildManager.abortUnfinishedBuilds(project, "some message");

        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().equals("some message"));
    }

    public void testIncompletePersonalBuild()
    {
        Project project = new Project();
        User user = newUser();
        BuildResult result = createResult(user, project);
        result.commence(10);

        stub(buildResultDao.getLatestByUser((User)anyObject(), (ResultState[])anyObject(), anyInt())).toReturn(asList(result));

        assertTrue(result.commenced());
        assertFalse(result.completed());

        buildManager.abortUnfinishedBuilds(user, "some message");

        assertTrue(result.errored());
        assertTrue(result.getFeatures(Feature.Level.ERROR).get(0).getSummary().contains("some message"));
    }

    private User newUser()
    {
        UserConfiguration config = new UserConfiguration("test", "test");
        User user = new User();
        user.setId(insecureRandomInt());
        user.setConfig(config);
        return user;
    }

    private BuildResult createResult(Project project)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("test trigger"), project, 1, false);
        result.setId(insecureRandomInt());
        return result;
    }

    private BuildResult createResult(User user, Project project)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("test trigger"), user, project, 1);
        result.setId(insecureRandomInt());
        return result;
    }

}
