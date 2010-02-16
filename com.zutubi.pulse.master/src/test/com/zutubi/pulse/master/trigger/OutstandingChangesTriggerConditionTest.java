package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.OutstandingChangesTriggerConditionConfiguration;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class OutstandingChangesTriggerConditionTest extends PulseTestCase
{
    private Project project;
    private Revision notLatestRevision = new Revision(1);
    private Revision latestRevision = new Revision(2);
    private BuildManager buildManager;
    private List<BuildRequestEvent> queuedEvents;
    private FatController fatController;
    private ScmClient scmClient;
    private ScmManager scmManager;
    private OutstandingChangesTriggerConditionConfiguration config;
    private OutstandingChangesTriggerCondition condition;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        project = new Project();
        project.setId(1);
        project.setConfig(new ProjectConfiguration("test"));

        buildManager = mock(BuildManager.class);
        stub(buildManager.getPreviousRevision(project)).toReturn(null);

        queuedEvents = new LinkedList<BuildRequestEvent>();
        fatController = mock(FatController.class);
        stub(fatController.getRequestsForEntity(project)).toReturn(queuedEvents);

        scmClient = mock(ScmClient.class);
        stub(scmClient.getCapabilities((ScmContext) anyObject())).toReturn(EnumSet.allOf(ScmCapability.class));
        stub(scmClient.getRevisions((ScmContext) anyObject(), eq(latestRevision), (Revision) isNull())).toReturn(Collections.<Revision>emptyList());
        stub(scmClient.getRevisions((ScmContext) anyObject(), eq(notLatestRevision), (Revision) isNull())).toReturn(asList(latestRevision));

        scmManager = mock(ScmManager.class);
        stub(scmManager.createContext((ProjectConfiguration) anyObject())).toReturn(new ScmContextImpl());
        stub(scmManager.createClient((ScmConfiguration) anyObject())).toReturn(scmClient);

        config = new OutstandingChangesTriggerConditionConfiguration();
        condition = new OutstandingChangesTriggerCondition(config);
        condition.setBuildManager(buildManager);
        condition.setFatController(fatController);
        condition.setScmManager(scmManager);
    }

    public void testNoPreviousBuildsNoRevisions()
    {
        assertTrue(condition.satisfied(project));
    }

    public void testPreviousBuildLatestRevision()
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(latestRevision);
        assertFalse(condition.satisfied(project));
    }

    public void testPreviousBuildNotLatestRevision()
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(notLatestRevision);
        assertTrue(condition.satisfied(project));
    }

    public void testQueuedBuildNotLatest()
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(notLatestRevision);
        queuedEvents.add(makeRequest(notLatestRevision, true));
        assertTrue(condition.satisfied(project));
    }

    public void testQueuedBuildLatest()
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(notLatestRevision);
        queuedEvents.add(makeRequest(latestRevision, true));
        assertFalse(condition.satisfied(project));
    }

    public void testQueuedBuildFloating()
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(notLatestRevision);
        queuedEvents.add(makeRequest(notLatestRevision, false));
        assertFalse(condition.satisfied(project));
    }
    
    private BuildRequestEvent makeRequest(Revision revision, boolean fixed)
    {
        BuildRevision buildRevision;
        if (fixed)
        {
            buildRevision = new BuildRevision(revision, false);
        }
        else
        {
            buildRevision = new BuildRevision();
        }
        return new SingleBuildRequestEvent(this, project, buildRevision, new TriggerOptions(null, null));
    }
}
