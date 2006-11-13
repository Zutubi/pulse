package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.BuildScmDetails;
import com.zutubi.pulse.model.MockBuildManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.NumericalFileRevision;

/**
 */
public class ChangedByMeNotificationConditionTest extends PulseTestCase
{
    private MockBuildManager buildManager = new MockBuildManager();
    private ChangedByMeNotifyCondition condition = new ChangedByMeNotifyCondition();

    protected void setUp() throws Exception
    {
        condition.setBuildManager(buildManager);
    }

    public void testChangedByMe()
    {
        BuildResult result = getBuildWithChanges(getChangelistBy("me"));
        User me = new User("me", "Your Overlord");
        assertTrue(condition.satisfied(result, me));
    }

    public void testChangedOther()
    {
        BuildResult result = getBuildWithChanges(getChangelistBy("you"));
        User me = new User("me", "Your Overlord");
        assertFalse(condition.satisfied(result, me));
    }

    public void testChangedByNobody()
    {
        BuildResult result = getBuildWithChanges();
        User me = new User("me", "Your Overlord");
        assertFalse(condition.satisfied(result, me));
    }

    public void testChangedByAlias()
    {
        BuildResult result = getBuildWithChanges(getChangelistBy("my alias"));
        User me = new User("me", "Your Overlord");
        me.addAlias("my alias");
        assertTrue(condition.satisfied(result, me));
    }

    private BuildResult getBuildWithChanges(Changelist ...changes)
    {
        BuildResult result = new BuildResult();
        for(Changelist list: changes)
        {
            list.addResultId(result.getId());
            buildManager.save(list);
        }
        BuildScmDetails details = new BuildScmDetails(new Revision());
        result.setScmDetails(details);
        return result;
    }

    private Changelist getChangelistBy(String author)
    {
        Changelist change = new Changelist("uid", new Revision(author, "comment", 0, "rev"));
        change.addChange(new Change("file", new NumericalFileRevision(1), Change.Action.EDIT));
        return change;
    }

}
