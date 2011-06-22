package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.pulse.server.ActiveBuildsTable;
import com.zutubi.pulse.acceptance.components.pulse.server.QueuedBuildsTable;
import com.zutubi.pulse.acceptance.components.table.ContentTable;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.server.ServerActivityPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Acceptance tests for the server/activity page.
 */
public class ServerActivityAcceptanceTest extends AcceptanceTestBase
{
    private static final String REVISION_WAIT_ANT = "29";

    private static final int TIMEOUT = 90000;
    public static final String BUILD_REASON = "trigger via remote api by admin";

    private Map<Pair<String, Integer>, File> waitFiles = new HashMap<Pair<String,Integer>, File>();

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
        rpcClient.TestApi.ensureQueuesRunning();
        removeNonMasterAgents();
        getBrowser().loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.TestApi.ensureQueuesRunning();

        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        for (File f: waitFiles.values())
        {
            if (!f.delete())
            {
                f.deleteOnExit();
            }
        }

        super.tearDown();
    }

    public void testEmptyActivityTables()
    {
        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        assertEquals(0, activityPage.getQueued().getRowCount());
        assertEquals(0, activityPage.getActive().getBuildCount());
    }

    public void testToggleBuildQueue()
    {
        final ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        assertEquals("running", activityPage.getBuildQueueStatus());
        assertTrue(activityPage.canPauseBuildQueue());
        activityPage.clickBuildQueueToggle();
        
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return activityPage.canResumeBuildQueue();
            }
        }, TIMEOUT, "build queue to pause");
        
        assertEquals("ignoring all triggers", activityPage.getBuildQueueStatus());
        
        activityPage.clickBuildQueueToggle();
        
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return activityPage.canPauseBuildQueue();
            }
        }, TIMEOUT, "build queue to resume");
        assertEquals("running", activityPage.getBuildQueueStatus());
    }
    
    public void testToggleStageQueue()
    {
        final ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        assertEquals("running", activityPage.getStageQueueStatus());
        assertTrue(activityPage.canPauseStageQueue());
        activityPage.clickStageQueueToggle();
        
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return activityPage.canResumeStageQueue();
            }
        }, TIMEOUT, "stage queue to pause");
        
        assertEquals("paused", activityPage.getStageQueueStatus());
        
        activityPage.clickStageQueueToggle();
        
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return activityPage.canPauseStageQueue();
            }
        }, TIMEOUT, "stage queue to resume");
        assertEquals("running", activityPage.getStageQueueStatus());
    }
    
    public void testActiveBuilds() throws Exception
    {
        String project1 = random + "1";
        String project2 = random + "2";
        
        createAndTriggerProjectBuild(project1, true);

        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);

        ActiveBuildsTable active = activityPage.getActive();
        assertEquals(1, active.getBuildCount());
        ActiveBuildsTable.ActiveBuild build = active.getBuild(0);
        assertActiveBuild(build, project1, 1, ResultState.IN_PROGRESS, REVISION_WAIT_ANT, BUILD_REASON, true);
        assertEquals(1, build.stages.size());
        ActiveBuildsTable.ActiveStage stage = build.stages.get(0);
        assertActiveStage(stage, "default", ResultState.IN_PROGRESS, "[default]", AgentManager.MASTER_AGENT_NAME);

        createAndTriggerProjectBuild(project2, false);

        activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);

        active = activityPage.getActive();
        assertEquals(2, active.getBuildCount());
        build = active.getBuild(0);
        assertActiveBuild(build, project2, 1, ResultState.PENDING, "[floating]", BUILD_REASON, true);
        assertEquals(1, build.stages.size());
        stage = build.stages.get(0);
        assertActiveStage(stage, "default", ResultState.PENDING, "[default]", null);
        build = active.getBuild(1);
        assertActiveBuild(build, project1, 1, ResultState.IN_PROGRESS, REVISION_WAIT_ANT, BUILD_REASON, true);
        
        waitForBuildToComplete(project1, 1);
        waitForBuildToComplete(project2, 1);
        
        waitForQueueCount(activityPage, active, 0);
    }

    public void testCancelBuild() throws Exception
    {
        createAndTriggerProjectBuild(random, true);

        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        activityPage.getActive().clickCancel(0);

        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);
        getBrowser().openAndWaitFor(BuildSummaryPage.class, random, 1L);
        assertTrue(getBrowser().isTextPresent("Forceful termination requested by 'admin'"));
    }

    /**
     * Simple verification that queued builds are correctly displayed in the
     * build queue.
     *
     * Single project.
     *
     * @throws Exception on error.
     */
    public void testBuildQueue() throws Exception
    {
        // build 1 becomes active.
        createAndTriggerProjectBuild(random, true);
        // build 2 goes into the build queue.
        triggerBuild(random, false);

        verifyQueuedBuildViaRemoteApi();

        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        ActiveBuildsTable active = activityPage.getActive();
        waitForQueueCount(activityPage, active, 1);

        QueuedBuildsTable queued = activityPage.getQueued();
        waitForQueueCount(activityPage, queued, 1);
        QueuedBuildsTable.QueuedBuild build = queued.getBuild(0);
        assertEquals(random, build.owner);
        assertEquals("[floating]", build.revision);
        assertEquals(BUILD_REASON, build.reason);
        assertTrue(build.cancelPermitted);

        waitForBuildToComplete(random, 1);

        waitForQueueCount(activityPage, queued, 0);
        waitForQueueCount(activityPage, active, 1);

        waitForBuildToComplete(random, 2);
    }

    public void testCancelQueuedBuild() throws Exception
    {
        createAndTriggerProjectBuild(random, true);
        triggerBuild(random, false);
        verifyQueuedBuildViaRemoteApi();

        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        QueuedBuildsTable queued = activityPage.getQueued();
        assertEquals(1, queued.getRowCount());

        queued.clickCancel(0);

        waitForQueueCount(activityPage, queued, 0);
        waitForBuildToComplete(random, 1);
    }
    
    public void testCancelQueuedBuildViaRemoteApi() throws Exception
    {
        createAndTriggerProjectBuild(random, true);
        rpcClient.RemoteApi.triggerBuild(random);

        String id = verifyQueuedBuildViaRemoteApi();
        rpcClient.RemoteApi.cancelQueuedBuildRequest(id);
        assertEquals(0, rpcClient.RemoteApi.getBuildQueueSnapshot().size());
        
        waitForBuildToComplete(random, 1);
    }
    
    public void testActionPermissions() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        
        rpcClient.RemoteApi.insertTrivialUser(user);
        createAndTriggerProjectBuild(project, true);
        triggerBuild(project, false);
        
        getBrowser().logout();
        getBrowser().login(user, "");

        ServerActivityPage activityPage = getBrowser().openAndWaitFor(ServerActivityPage.class);
        assertFalse(activityPage.canPauseStageQueue());
        assertFalse(activityPage.canResumeStageQueue());

        assertFalse(activityPage.getQueued().getBuild(0).cancelPermitted);
        ActiveBuildsTable.ActiveBuild activeBuild = activityPage.getActive().getBuild(0);
        assertFalse(activeBuild.canCancel);

        waitForBuildToComplete(project, 1);
        waitForBuildToComplete(project, 2);
    }

    private void assertActiveBuild(ActiveBuildsTable.ActiveBuild build, String owner, int id, ResultState status, String revision, String reason, boolean cancelPermitted)
    {
        assertEquals(owner, build.owner);
        assertEquals(id, build.id);
        assertEquals(status, build.status);
        assertEquals(revision, build.revision);
        assertEquals(reason, build.reason);
        assertEquals(cancelPermitted, build.canCancel);
    }

    private void assertActiveStage(ActiveBuildsTable.ActiveStage stage, String name, ResultState status, String recipe, String agent)
    {
        assertEquals(name, stage.name);
        assertEquals(status, stage.status);
        assertEquals(recipe, stage.recipe);
        assertEquals(agent, stage.agent);
    }

    private String verifyQueuedBuildViaRemoteApi() throws Exception
    {
        int size;
        long startTime = System.currentTimeMillis();
        Vector<Hashtable<String, Object>> queueSnapshot;
        do
        {
            if (System.currentTimeMillis() - startTime > TIMEOUT)
            {
                fail("Timed out waiting for queued build request");
            }

            queueSnapshot = rpcClient.RemoteApi.getBuildQueueSnapshot();
            size = queueSnapshot.size();
            Thread.sleep(100);
        }
        while (size != 1);

        Hashtable<String, Object> request = queueSnapshot.get(0);
        assertNotNull(request.get("queuedTime"));
        assertEquals(random, request.get("owner"));
        assertEquals(random, request.get("project"));
        assertEquals(false, request.get("isPersonal"));
        assertEquals(false, request.get("isReplaceable"));
        assertEquals("", request.get("revision"));
        assertEquals(BUILD_REASON, request.get("reason"));
        assertEquals("remote api", request.get("requestSource"));

        String id = (String) request.get("id");
        assertNotNull(id);
        return id;
    }

    private void createAndTriggerProjectBuild(String project, boolean waitForInProgress) throws Exception
    {
        Hashtable<String, Object> svn = rpcClient.RemoteApi.getSubversionConfig(Constants.WAIT_ANT_REPOSITORY);
        Hashtable<String,Object> ant = rpcClient.RemoteApi.getAntConfig();
        ant.put(Constants.Project.AntCommand.ARGUMENTS, getFileArgument());
        rpcClient.RemoteApi.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, svn, ant);

        triggerBuild(project, waitForInProgress);
    }

    private void triggerBuild(String project, boolean waitForInProgress) throws Exception
    {
        int thisBuild = rpcClient.RemoteApi.getNextBuildNumber(project);
        File waitFile = new File(FileSystemUtils.getSystemTempDir(), project + thisBuild);
        if (waitFile.exists() && !waitFile.delete())
        {
            throw new RuntimeException("Wait file '" + waitFile.getAbsolutePath() + "' already exists and can't be removed");
        }
        waitFiles.put(asPair(project, thisBuild), waitFile);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("wait.file", waitFile.getAbsolutePath().replace("\\", "/"));
        rpcClient.RemoteApi.triggerBuild(project, "", properties);

        if (waitForInProgress)
        {
            rpcClient.RemoteApi.waitForBuildInProgress(project, thisBuild, TIMEOUT);
        }
    }

    private void waitForQueueCount(final ServerActivityPage page, final ContentTable queueTable, final int count)
    {
        getBrowser().refreshUntil(TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                page.waitFor();
                return queueTable.getDataLength() == count;
            }
        }, "queue to have " + count + " entries");
    }

    private void waitForBuildToComplete(String project, int buildId) throws Exception
    {
        File waitFile = waitFiles.get(asPair(project, buildId));
        if (!waitFile.isFile())
        {
            FileSystemUtils.createFile(waitFile, "test");
        }
        rpcClient.RemoteApi.waitForBuildToComplete(project, buildId);
    }

    private String getFileArgument()
    {
        return "-Dfile=${wait.file}";
    }
}
