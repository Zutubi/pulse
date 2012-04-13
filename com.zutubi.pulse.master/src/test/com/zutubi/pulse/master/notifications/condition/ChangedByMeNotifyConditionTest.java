package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.RandomUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class ChangedByMeNotifyConditionTest extends PulseTestCase
{
    private BuildManager buildManager;
    private ChangedByMeNotifyCondition condition = new ChangedByMeNotifyCondition();

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);

        condition.setBuildManager(buildManager);
        condition.setTransactionContext(new TransactionContext());
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
        me.getPreferences().getAliases().add("my alias");
        assertTrue(condition.satisfied(result, me));
    }

    private BuildResult getBuildWithChanges(PersistentChangelist ...changes)
    {
        BuildResult result = new BuildResult();
        result.setId(RandomUtils.randomInt());
        stub(buildManager.getChangesForBuild(result, 0, false)).toReturn(Arrays.asList(changes));
        result.setRevision(new Revision("1"));
        return result;
    }

    private PersistentChangelist getChangelistBy(String author)
    {
        return new PersistentChangelist(new Revision("1"), 0, author, "comment", Arrays.asList(new PersistentFileChange("file", "1", FileChange.Action.EDIT, false)));
    }
}
