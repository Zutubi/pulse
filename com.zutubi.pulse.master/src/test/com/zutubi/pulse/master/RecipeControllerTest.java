package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.scm.MockScmClient;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.AnyCapableAgentRequirements;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.servercore.CheckoutBootstrapper;
import com.zutubi.pulse.servercore.agent.Status;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.FileSystemUtils;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.*;

public class RecipeControllerTest extends PulseTestCase
{
    private File recipeDir;

    private MockRecipeQueue recipeQueue;
    private MockBuildManager buildManager;
    private AgentService buildService;

    private RecipeResult rootResult;
    private RecipeResultNode rootNode;
    private RecipeAssignmentRequest assignmentRequest;
    private RecipeController recipeController;

    protected void setUp() throws Exception
    {
        super.setUp();
        recipeDir = FileSystemUtils.createTempDir(RecipeControllerTest.class.getName(), "");

        MockRecipeResultCollector resultCollector = new MockRecipeResultCollector();
        recipeQueue = new MockRecipeQueue();
        buildManager = new MockBuildManager();
        buildService = mock(AgentService.class);
        RecipeLogger logger = mock(RecipeLogger.class);

        rootResult = new RecipeResult("root recipe");
        rootResult.setId(100);
        rootNode = new RecipeResultNode("root stage", 1, rootResult);
        rootNode.setId(101);
        RecipeResult childResult = new RecipeResult("child recipe");
        childResult.setId(102);
        RecipeResultNode childNode = new RecipeResultNode("child stage", 2, childResult);
        childNode.setId(103);
        rootNode.addChild(childNode);

        RecipeRequest recipeRequest = new RecipeRequest(makeContext("project", rootResult.getId(), rootResult.getRecipeName()));
        Project project = new Project();
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setType(new CustomTypeConfiguration());
        project.setConfig(projectConfig);
        BuildResult build = new BuildResult(new ManualTriggerBuildReason("user"), project, 1, false);
        assignmentRequest = new RecipeAssignmentRequest(project, new AnyCapableAgentRequirements(), null, new BuildRevision(new Revision("0"), false), recipeRequest, null);
        MasterConfigurationManager configurationManager = new SimpleMasterConfigurationManager()
        {
            public File getDataDirectory()
            {
                return new File("test");
            }

            public MasterUserPaths getUserPaths()
            {
                return new Data(getDataDirectory());
            }
        };

        ScmManager scmManager = mock(ScmManager.class);
        stub(scmManager.createClient((ScmConfiguration) anyObject())).toReturn(new MockScmClient());
        recipeController = new RecipeController(projectConfig, build, rootNode, assignmentRequest, new PulseExecutionContext(), null, logger, resultCollector);
        recipeController.setRecipeQueue(recipeQueue);
        recipeController.setBuildManager(buildManager);
        recipeController.setEventManager(new DefaultEventManager());
        recipeController.setConfigurationManager(configurationManager);
        recipeController.setResourceManager(new DefaultResourceManager());
        recipeController.setRecipeDispatchService(mock(RecipeDispatchService.class));
        recipeController.setScmManager(scmManager);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(recipeDir);
        super.tearDown();
    }

    public void testIgnoresOtherRecipes()
    {
        assertFalse(recipeController.matchesRecipeEvent(new RecipeCommencedEvent(this, rootResult.getId() + 1, "yay", 0)));
    }

    public void testDispatchRequest()
    {
        // Initialising should cause a dispatch request, and should initialise the bootstrapper
        Bootstrapper bootstrapper = new CheckoutBootstrapper("project", new BuildRevision());
        recipeController.initialise(bootstrapper);
        assertTrue(recipeQueue.hasDispatched(rootResult.getId()));
        RecipeAssignmentRequest dispatched = recipeQueue.getRequest(rootResult.getId());
        assertSame(assignmentRequest, dispatched);
        assertSame(assignmentRequest.getRequest().getBootstrapper(), bootstrapper);
    }

    public void testAssignedEvent()
    {
        testDispatchRequest();
        buildManager.clear();

        Agent agent = mock(Agent.class);
        stub(agent.getService()).toReturn(buildService);
        stub(agent.isOnline()).toReturn(true);
        stub(agent.getStatus()).toReturn(Status.IDLE);
        stub(agent.getConfig()).toReturn(new AgentConfiguration());

        // After dispatching, the controller should handle a dispatched event
        // by recording the build service on the result node.
        RecipeAssignedEvent event = new RecipeAssignedEvent(this, new RecipeRequest(makeContext("project", rootResult.getId(), "test")), agent);
        assertTrue(recipeController.matchesRecipeEvent(event));
        recipeController.handleRecipeEvent(event);
        assertEquals(buildService.getHostName(), rootNode.getHost());

        assertSame(rootNode, buildManager.getRecipeResultNode(rootNode.getId()));
    }

    public void testCommencedEvent()
    {
        testAssignedEvent();
        buildManager.clear();

        // A recipe commence event should change the result state, and record
        // the start time.
        RecipeCommencedEvent event = new RecipeCommencedEvent(this, rootResult.getId(), rootResult.getRecipeName(), 10101);
        assertTrue(recipeController.matchesRecipeEvent(event));
        recipeController.handleRecipeEvent(event);
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
        assertTrue(recipeController.matchesRecipeEvent(event));
        recipeController.handleRecipeEvent(event);

        List<CommandResult> commandResults = rootResult.getCommandResults();
        assertTrue(commandResults.size() > 0);
        CommandResult result = commandResults.get(commandResults.size() - 1);
        result.setOutputDir("dummy");
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

        assertTrue(recipeController.matchesRecipeEvent(event));
        recipeController.handleRecipeEvent(event);
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

        assertTrue(recipeController.matchesRecipeEvent(event));
        recipeController.handleRecipeEvent(event);
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
        testAssignedEvent();
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

    private PulseExecutionContext makeContext(String project, long id, String recipeName)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_PROJECT, project);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(id));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, recipeName);
        return context;
    }

    private RecipeErrorEvent sendError()
    {
        RecipeErrorEvent error = new RecipeErrorEvent(this, rootResult.getId(), "test error message");
        assertTrue(recipeController.matchesRecipeEvent(error));
        recipeController.handleRecipeEvent(error);
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
        private Map<Long, RecipeAssignmentRequest> dispatched = new TreeMap<Long, RecipeAssignmentRequest>();

        public void enqueue(RecipeAssignmentRequest request)
        {
            dispatched.put(request.getRequest().getId(), request);
        }

        public List<RecipeAssignmentRequest> takeSnapshot()
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

        public RecipeAssignmentRequest getRequest(long recipeId)
        {
            return dispatched.get(recipeId);
        }
    }
}
