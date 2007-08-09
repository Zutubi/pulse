package com.zutubi.pulse;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.pulse.events.build.CommandOutputEvent;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.events.build.RecipeErrorEvent;
import com.zutubi.pulse.events.build.RecipeStatusEvent;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.svn.SvnClient;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 */
public class RecipeControllerTest extends PulseTestCase
{
    private File recipeDir;

    private MockRecipeQueue recipeQueue;
    private MockBuildManager buildManager;
    private MockAgentService buildService;

    private RecipeResult rootResult;
    private RecipeResultNode rootNode;
    private RecipeDispatchRequest dispatchRequest;
    private RecipeController recipeController;

    protected void setUp() throws Exception
    {
        super.setUp();
        recipeDir = FileSystemUtils.createTempDir(RecipeControllerTest.class.getName(), "");

        MockRecipeResultCollector resultCollector = new MockRecipeResultCollector();
        recipeQueue = new MockRecipeQueue();
        buildManager = new MockBuildManager();
        buildService = new MockAgentService();
        RecipeLogger logger = new MockRecipeLogger();

        rootResult = new RecipeResult("root recipe");
        rootResult.setId(100);
        rootNode = new RecipeResultNode("root stage", 1, rootResult);
        rootNode.setId(101);
        RecipeResult childResult = new RecipeResult("child recipe");
        childResult.setId(102);
        RecipeResultNode childNode = new RecipeResultNode("child stage", 2, childResult);
        childNode.setId(103);
        rootNode.addChild(childNode);

        RecipeRequest recipeRequest = new RecipeRequest("project", rootResult.getId(), rootResult.getRecipeName());
        BuildResult build = new BuildResult();
        dispatchRequest = new RecipeDispatchRequest(new Project(), new MasterBuildHostRequirements(), new BuildRevision(), recipeRequest, null);
        recipeController = new RecipeController(null, build, rootNode, dispatchRequest, new LinkedList<ResourceProperty>(), false, false, null, logger, resultCollector);
        recipeController.setRecipeQueue(recipeQueue);
        recipeController.setBuildManager(buildManager);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(recipeDir);
        super.tearDown();
    }

    public void testIgnoresOtherRecipes()
    {
        assertFalse(recipeController.handleRecipeEvent(new RecipeCommencedEvent(this, rootResult.getId() + 1, "yay", 0)));
    }

    public void testDispatchRequest()
    {
        // Initialising should cause a dispatch request, and should initialise the bootstrapper
        try
        {
            Bootstrapper bootstrapper = new CheckoutBootstrapper("project", new SvnClient(null), new BuildRevision(), false);
            recipeController.initialise(bootstrapper);
            assertTrue(recipeQueue.hasDispatched(rootResult.getId()));
            RecipeDispatchRequest dispatched = recipeQueue.getRequest(rootResult.getId());
            assertSame(dispatchRequest, dispatched);
            assertSame(dispatchRequest.getRequest().getBootstrapper(), bootstrapper);
        }
        catch (ScmException e)
        {
            e.printStackTrace();
        }
    }

    public void testDispatchedEvent()
    {
        testDispatchRequest();
        buildManager.clear();

        // After dispatching, the controller should handle a dispatched event
        // by recording the build service on the result node.
        RecipeDispatchedEvent event = new RecipeDispatchedEvent(this, new RecipeRequest("project", rootResult.getId(), "test"), new MockAgent(buildService));
        assertTrue(recipeController.handleRecipeEvent(event));
        assertEquals(buildService.getHostName(), rootNode.getHost());

        assertSame(rootNode, buildManager.getRecipeResultNode(rootNode.getId()));
    }

    public void testCommencedEvent()
    {
        testDispatchedEvent();
        buildManager.clear();

        // A recipe commence event should change the result state, and record
        // the start time.
        RecipeCommencedEvent event = new RecipeCommencedEvent(this, rootResult.getId(), rootResult.getRecipeName(), 10101);
        assertTrue(recipeController.handleRecipeEvent(event));
        assertEquals(ResultState.IN_PROGRESS, rootResult.getState());

        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    public void testCommandCommencedEvent()
    {
        testCommencedEvent();
        buildManager.clear();

        // A command commenced event should result in a new command result
        // with the correct name, state and start time.
        CommandCommencedEvent event = new CommandCommencedEvent(this, rootResult.getId(), "test command", 555);
        assertTrue(recipeController.handleRecipeEvent(event));

        List<CommandResult> commandResults = rootResult.getCommandResults();
        assertTrue(commandResults.size() > 0);
        CommandResult result = commandResults.get(commandResults.size() - 1);
        assertEquals(ResultState.IN_PROGRESS, result.getState());
        assertEquals(event.getName(), result.getCommandName());

        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    public void testCommandCompletedEvent()
    {
        testCommandCommencedEvent();
        buildManager.clear();

        // A command completed event should result in the command result
        // in the event being applied to the recipe result.
        CommandResult commandResult = new CommandResult("test command");
        commandResult.commence();
        commandResult.setOutputDir("dummy");
        commandResult.complete();
        CommandCompletedEvent event = new CommandCompletedEvent(this, rootResult.getId(), commandResult);

        assertTrue(recipeController.handleRecipeEvent(event));
        List<CommandResult> commandResults = rootResult.getCommandResults();
        assertTrue(commandResults.size() > 0);
        CommandResult result = commandResults.get(commandResults.size() - 1);
        assertTrue(result.completed());
        assertEquals("dummy", result.getOutputDir());

        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    public void testRecipeCompletedEvent()
    {
        testCommandCompletedEvent();
        buildManager.clear();

        // A recipe completed event should result in the recipe result
        // details being applied, and the controller should then be
        // finished
        RecipeResult result = new RecipeResult(rootResult.getRecipeName());
        result.setId(rootResult.getId());
        result.commence(1234);
        result.complete();
        RecipeCompletedEvent event = new RecipeCompletedEvent(this, result);

        assertTrue(recipeController.handleRecipeEvent(event));
        assertEquals(ResultState.SUCCESS, rootResult.getState());
        assertTrue(recipeController.isFinished());

        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    public void testErrorBeforeDispatched()
    {
        testDispatchRequest();
        buildManager.clear();

        sendError();

        assertNull(rootNode.getHost());
        assertTrue(rootResult.getStamps().started());
        assertTrue(rootResult.getStamps().ended());
    }

    public void testErrorBeforeCommenced()
    {
        testDispatchedEvent();
        buildManager.clear();

        sendError();

        assertTrue(rootResult.getStamps().started());
        assertTrue(rootResult.getStamps().ended());
    }

    public void testErrorAfterCommenced()
    {
        testCommencedEvent();
        buildManager.clear();

        sendError();

        // Should have start and end times.
        assertTrue(rootResult.getStamps().started());
        assertTrue(rootResult.getStamps().ended());
    }

    public void testErrorMidCommand()
    {
        testCommandCommencedEvent();
        buildManager.clear();

        sendError();

        // Command should be aborted
        List<CommandResult> results = rootResult.getCommandResults();
        assertTrue(results.size() > 0);
        CommandResult lastResult = results.get(results.size() - 1);
        assertEquals(ResultState.ERROR, lastResult.getState());
    }

    public void testErrorBetweenCommands()
    {
        testCommandCompletedEvent();
        buildManager.clear();

        sendError();

        // Command should not be affected
        List<CommandResult> results = rootResult.getCommandResults();
        assertTrue(results.size() > 0);
        CommandResult lastResult = results.get(results.size() - 1);
        assertEquals(ResultState.SUCCESS, lastResult.getState());
    }

    private RecipeErrorEvent sendError()
    {
        RecipeErrorEvent error = new RecipeErrorEvent(this, rootResult.getId(), "test error message");
        assertTrue(recipeController.handleRecipeEvent(error));
        assertErrorDetailsSaved(error);
        return error;
    }

    private void assertErrorDetailsSaved(RecipeErrorEvent error)
    {
        assertEquals(ResultState.ERROR, rootResult.getState());
        assertEquals(error.getErrorMessage(), rootResult.getFeatures(Feature.Level.ERROR).get(0).getSummary());
        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    class MockRecipeResultCollector implements RecipeResultCollector
    {
        private Set<Long> preparedRecipes = new TreeSet<Long>();
        private Map<Long, AgentService> collectedRecipes = new TreeMap<Long, AgentService>();
        private Map<Long, AgentService> cleanedRecipes = new TreeMap<Long, AgentService>();

        public void prepare(BuildResult result, long recipeId)
        {
            preparedRecipes.add(recipeId);
        }

        public void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, boolean incremental, AgentService agentService)
        {
            collectedRecipes.put(recipeId, agentService);
        }

        public void cleanup(BuildResult result, long recipeId, boolean incremental, AgentService agentService)
        {
            cleanedRecipes.put(recipeId, agentService);
        }

        public File getRecipeDir(BuildResult result, long recipeId)
        {
            return recipeDir;
        }

        public boolean hasPrepared(long recipeId)
        {
            return preparedRecipes.contains(recipeId);
        }

        public boolean hasCollected(long recipeId)
        {
            return collectedRecipes.containsKey(recipeId);
        }

        public boolean hasCleaned(long recipeId)
        {
            return cleanedRecipes.containsKey(recipeId);
        }
    }

    class MockRecipeQueue implements RecipeQueue
    {
        private Map<Long, RecipeDispatchRequest> dispatched = new TreeMap<Long, RecipeDispatchRequest>();

        public void enqueue(RecipeDispatchRequest request)
        {
            dispatched.put(request.getRequest().getId(), request);
        }

        public List<RecipeDispatchRequest> takeSnapshot()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean cancelRequest(long id)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void start()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void stop()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean isRunning()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean hasDispatched(long recipeId)
        {
            return dispatched.containsKey(recipeId);
        }

        public RecipeDispatchRequest getRequest(long recipeId)
        {
            return dispatched.get(recipeId);
        }
    }

    class MockAgent implements Agent
    {
        private AgentService service;

        public MockAgent(AgentService service)
        {
            this.service = service;
        }

        public long getId()
        {
            return 0;
        }

        public AgentService getService()
        {
            return service;
        }

        public boolean isOnline()
        {
            return true;
        }

        public boolean isEnabled()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean isDisabled()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean isUpgrading()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean isFailedUpgrade()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean isAvailable()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public AgentState.EnableState getEnableState()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void setAgentState(AgentState agentState)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public Status getStatus()
        {
            return Status.IDLE;
        }

        public String getLocation()
        {
            return "mock";
        }

        public void updateStatus(SlaveStatus status)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void setStatus(Status status)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public void upgradeStatus(UpgradeState state, int progress, String message)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public AgentConfiguration getConfig()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public AgentState getState()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public String getName()
        {
            return "mock";
        }
    }

    class MockAgentService implements AgentService
    {
        public int ping()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public SlaveStatus getStatus(String masterLocation)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public List<Resource> discoverResources()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public SystemInfo getSystemInfo()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public List<CustomLogRecord> getRecentMessages()
        {
            throw new RuntimeException("Method not yet implemented.");
        }

        public boolean hasResource(String resource, String version)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public boolean build(RecipeRequest request, BuildContext context)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void collectResults(String project, long recipeId, boolean incremental, File outputDest, File workDest)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void cleanup(String project, long recipeId, boolean incremental)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void terminateRecipe(long recipeId)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public String getHostName()
        {
            return "mock build service";
        }

        public String getUrl()
        {
            throw new RuntimeException("Method not implemented.");
        }
    }

    public class MockRecipeLogger implements RecipeLogger
    {
        public void log(RecipeDispatchedEvent event)
        {
        }

        public void log(RecipeCommencedEvent event, RecipeResult result)
        {
        }

        public void log(CommandCommencedEvent event, CommandResult result)
        {
        }

        public void log(CommandOutputEvent event)
        {
        }

        public void log(CommandCompletedEvent event, CommandResult result)
        {
        }

        public void log(RecipeCompletedEvent event, RecipeResult result)
        {
        }

        public void log(RecipeStatusEvent event)
        {
        }

        public void log(RecipeErrorEvent event, RecipeResult result)
        {
        }

        public void prepare()
        {
        }

        public void complete(RecipeResult result)
        {
        }

        public void collecting(RecipeResult recipeResult, boolean collectWorkingCopy)
        {
        }

        public void collectionComplete()
        {
        }
    }
}
