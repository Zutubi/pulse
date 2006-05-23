package com.zutubi.pulse.condition;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.BuildScmDetails;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Change;

import java.util.Arrays;

/**
 */
public class ChangedByMeNotificationConditionTest extends PulseTestCase
{
    private ChangedByMeNotifyCondition condition = new ChangedByMeNotifyCondition();

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
        BuildScmDetails details = new BuildScmDetails(new Revision(), Arrays.asList(changes));
        result.setScmDetails(details);
        return result;
    }

    private Changelist getChangelistBy(String author)
    {
        Changelist change = new Changelist("uid", new Revision(author, "comment", 0, "rev"));
        change.addChange(new Change("file", "rev", Change.Action.EDIT));
        return change;
    }
}
