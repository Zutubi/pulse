package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ChangedByMeSinceSuccessNotificationConditionTest extends PulseTestCase
{
    private static final UserConfiguration USER_A = new UserConfiguration("a", "a");
    private static final UserConfiguration USER_B = new UserConfiguration("b", "b");
    private static final UserConfiguration USER_C = new UserConfiguration("c", "c");

    private BuildResult builds[] = new BuildResult[4];
    private BuildManager buildManager;
    private ChangedByMeSinceSuccessNotifyCondition condition = new ChangedByMeSinceSuccessNotifyCondition();

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);

        builds[0] = createSuccessfulBuild(1);
        builds[1] = createFailedBuild(2);
        builds[2] = createSuccessfulBuild(3);
        builds[3] = createFailedBuild(4);

        setPreviousSuccess(builds[0], null);
        setPreviousSuccess(builds[1], builds[0]);
        setPreviousSuccess(builds[2], builds[0]);
        setPreviousSuccess(builds[3], builds[2]);

        setChanges(builds[0], USER_C);
        setChanges(builds[1], USER_B);
        setChanges(builds[2], USER_A, USER_C);
        setChanges(builds[3], USER_A, USER_C);

        stub(buildManager.queryBuilds((Project) anyObject(), (ResultState[]) anyObject(), anyLong(), anyLong(), anyInt(), anyInt(), anyBoolean(), anyBoolean())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long lowestNumber = (Long) invocationOnMock.getArguments()[2];
                Long highestNumber = (Long) invocationOnMock.getArguments()[2];

                List<BuildResult> result = new LinkedList<BuildResult>();
                for (long i = lowestNumber - 1; i < highestNumber; i++)
                {
                    if (i >= 0 && i < builds.length)
                    {
                        result.add(builds[((int) i)]);
                    }
                }

                return result;
            }
        });

        condition.setBuildManager(buildManager);
        condition.setTransactionContext(new TransactionContext());
    }

    private void setChanges(BuildResult build, UserConfiguration... authors)
    {
        stub(buildManager.getChangesForBuild(build, 0, false)).toReturn(CollectionUtils.map(authors, new Mapping<UserConfiguration, PersistentChangelist>()
        {
            public PersistentChangelist map(UserConfiguration author)
            {
                return new PersistentChangelist(new Revision("1"), 0, author.getLogin(), "comment", Arrays.asList(new PersistentFileChange("file", "1", FileChange.Action.EDIT, false)));
            }
        }));
    }

    private BuildResult createSuccessfulBuild(long number)
    {
        BuildResult build = new BuildResult(new UnknownBuildReason(), null, number, false);
        build.setId(number);
        build.commence();
        build.complete();
        return build;
    }

    private BuildResult createFailedBuild(long number)
    {
        BuildResult build = createSuccessfulBuild(number);
        build.failure();
        return build;
    }

    private void setPreviousSuccess(BuildResult build, BuildResult previous)
    {
        stub(buildManager.getPreviousBuildResultWithRevision(eq(build), (ResultState[]) anyObject())).toReturn(previous);
    }

    public void testDirectlyChanged()
    {
        assertTrue(condition.satisfied(builds[0], USER_C));
    }

    public void testDirectlyChangedByOther()
    {
        assertFalse(condition.satisfied(builds[0], USER_A));
    }

    public void testSuccessRightBeforeHasNoImpact()
    {
        assertFalse(condition.satisfied(builds[1], USER_C));
    }

    public void testChangedInPreviousFailure()
    {
        assertTrue(condition.satisfied(builds[2], USER_B));
    }

    public void testLooksBackToOnlyMostRecentSuccess()
    {
        assertFalse(condition.satisfied(builds[3], USER_B));
    }
}