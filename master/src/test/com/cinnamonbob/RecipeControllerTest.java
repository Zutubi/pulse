package com.cinnamonbob;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.events.build.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.util.*;

/**
 */
public class RecipeControllerTest extends BobTestCase
{
    private MockRecipeResultCollector resultCollector;
    private MockRecipeQueue recipeQueue;
    private MockBuildManager buildManager;
    private MockBuildService buildService;

    private RecipeResult rootResult;
    private RecipeResultNode rootNode;
    private RecipeResult childResult;
    private RecipeResultNode childNode;
    private RecipeDispatchRequest dispatchRequest;
    private RecipeRequest recipeRequest;
    private RecipeController recipeController;

    protected void setUp() throws Exception
    {
        super.setUp();
        resultCollector = new MockRecipeResultCollector();
        recipeQueue = new MockRecipeQueue();
        buildManager = new MockBuildManager();
        buildService = new MockBuildService();

        rootResult = new RecipeResult("root recipe");
        rootResult.setId(100);
        rootNode = new RecipeResultNode(rootResult);
        rootNode.setId(101);
        childResult = new RecipeResult("child recipe");
        childResult.setId(102);
        childNode = new RecipeResultNode(childResult);
        childNode.setId(103);
        rootNode.addChild(childNode);

        recipeRequest = new RecipeRequest(rootResult.getId(), rootResult.getRecipeName());
        dispatchRequest = new RecipeDispatchRequest(new MasterBuildHostRequirements(), recipeRequest);
        recipeController = new RecipeController(rootNode, dispatchRequest, resultCollector, recipeQueue, buildManager);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testIgnoresOtherRecipes()
    {
        assertFalse(recipeController.handleRecipeEvent(new RecipeCommencedEvent(this, rootResult.getId() + 1, "yay", 0)));
    }

    public void testDispatchRequest()
    {
        // Initialising should cause a dispatch request, and should initialise the bootstrapper
        ScmBootstrapper bootstrapper = new ScmBootstrapper(new Svn());
        recipeController.initialise(bootstrapper);
        assertTrue(recipeQueue.hasDispatched(rootResult.getId()));
        RecipeDispatchRequest dispatched = recipeQueue.getRequest(rootResult.getId());
        assertSame(dispatchRequest, dispatched);
        assertSame(dispatchRequest.getRequest().getBootstrapper(), bootstrapper);
    }

    public void testDispatchedEvent()
    {
        testDispatchRequest();
        buildManager.clear();

        // After dispatching, the controller should handle a dispatched event
        // by recording the build service on the result node.
        RecipeDispatchedEvent event = new RecipeDispatchedEvent(this, rootResult.getId(), buildService);
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
        commandResult.commence(new File("/some/dir"));
        commandResult.complete();
        CommandCompletedEvent event = new CommandCompletedEvent(this, rootResult.getId(), commandResult);

        assertTrue(recipeController.handleRecipeEvent(event));
        List<CommandResult> commandResults = rootResult.getCommandResults();
        assertTrue(commandResults.size() > 0);
        CommandResult result = commandResults.get(commandResults.size() - 1);
        assertTrue(result.completed());
        assertSame(event.getResult(), result);

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

        // Never got dispatched
        assertNull(rootNode.getHost());
        assertFalse(rootResult.getStamps().started());
        assertFalse(rootResult.getStamps().ended());
    }

    public void testErrorBeforeCommenced()
    {
        testDispatchedEvent();
        buildManager.clear();

        sendError();

        // Dispatched, but never started
        assertFalse(rootResult.getStamps().started());
        assertFalse(rootResult.getStamps().ended());
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
        assertEquals(error.getErrorMessage(), rootResult.getErrorMessage());
        assertSame(rootResult, buildManager.getRecipeResult(rootResult.getId()));
    }

    class MockRecipeResultCollector implements RecipeResultCollector
    {
        private Set<Long> preparedRecipes = new TreeSet<Long>();
        private Map<Long, BuildService> collectedRecipes = new TreeMap<Long, BuildService>();
        private Map<Long, BuildService> cleanedRecipes = new TreeMap<Long, BuildService>();

        public void prepare(BuildResult result, long recipeId)
        {
            preparedRecipes.add(recipeId);
        }

        public void collect(BuildResult result, long recipeId, BuildService buildService)
        {
            collectedRecipes.put(recipeId, buildService);
        }

        public void cleanup(BuildResult result, long recipeId, BuildService buildService)
        {
            cleanedRecipes.put(recipeId, buildService);
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

        public boolean hasDispatched(long recipeId)
        {
            return dispatched.containsKey(recipeId);
        }

        public RecipeDispatchRequest getRequest(long recipeId)
        {
            return dispatched.get(recipeId);
        }
    }

    class MockBuildService implements BuildService
    {
        public void build(RecipeRequest request)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void collectResults(long recipeId, File outputDest, File workDest)
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void cleanup(long recipeId)
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
}
