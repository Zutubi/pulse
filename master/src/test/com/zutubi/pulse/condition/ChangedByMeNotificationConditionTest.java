package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.MockBuildManager;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Date;

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
        UserConfiguration me = new UserConfiguration("me", "Your Overlord");
        assertTrue(condition.satisfied(result, me));
    }

    public void testChangedOther()
    {
        BuildResult result = getBuildWithChanges(getChangelistBy("you"));
        UserConfiguration me = new UserConfiguration("me", "Your Overlord");
        assertFalse(condition.satisfied(result, me));
    }

    public void testChangedByNobody()
    {
        BuildResult result = getBuildWithChanges();
        UserConfiguration me = new UserConfiguration("me", "Your Overlord");
        assertFalse(condition.satisfied(result, me));
    }

    public void testChangedByAlias()
    {
        BuildResult result = getBuildWithChanges(getChangelistBy("my alias"));
        UserConfiguration me = new UserConfiguration("me", "Your Overlord");
        me.getPreferences().getSettings().getAliases().add("my alias");
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
        result.setRevision(new Revision(null, null, null, "1"));
        return result;
    }

    private Changelist getChangelistBy(String author)
    {
        Changelist change = new Changelist("uid", new Revision(author, "comment", new Date(0)));
        change.addChange(new Change("file", "1", Change.Action.EDIT));
        return change;
    }

}
