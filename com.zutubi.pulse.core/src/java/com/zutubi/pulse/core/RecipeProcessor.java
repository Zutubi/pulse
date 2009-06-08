package com.zutubi.pulse.core;

import com.zutubi.events.EventManager;
import static com.zutubi.pulse.core.RecipeUtils.addResourceProperties;
import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The recipe processor, as the name suggests, is responsible for running recipies.
 *
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private static final String LABEL_EXECUTE = "execute";

    private EventManager eventManager;
    private Lock runningLock = new ReentrantLock();
    private long runningRecipe = 0;
    private Command runningCommand = null;
    private boolean terminating = false;
    private PulseFileLoaderFactory fileLoaderFactory;

    public RecipeProcessor()
    {
    }

    public void init()
    {
        
    }

    public void build(RecipeRequest request)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult recipeResult = new RecipeResult(request.getRecipeName());
        recipeResult.setId(request.getId());
        recipeResult.commence();
        
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();
        Map<String, String> customFields = new HashMap<String, String>();

        runningRecipe = recipeResult.getId();
        eventManager.publish(new RecipeCommencedEvent(this, recipeResult.getId(), recipeResult.getRecipeName(), recipeResult.getStartTime()));

        PulseExecutionContext context = request.getContext();
        long recipeStartTime = recipeResult.getStartTime();
        pushRecipeContext(context, request, testResults, customFields, recipeStartTime);
        try
        {
            execute(request);
        }
        catch (BuildException e)
        {
            recipeResult.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            recipeResult.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            RecipePaths paths = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class);
            storeTestResults(paths, recipeResult, testResults);
            storeCustomFields(paths, customFields);
            compressResults(paths, context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_COMPRESS_ARTIFACTS, false), context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_COMPRESS_WORKING_DIR, false));

            recipeResult.complete();
            RecipeCompletedEvent completedEvent = new RecipeCompletedEvent(this, recipeResult);
            if(context.getVersion() != null)
            {
                completedEvent.setBuildVersion(context.getVersion());
            }

            eventManager.publish(completedEvent);

            context.pop();

            runningLock.lock();
            runningRecipe = 0;
            if (terminating)
            {
                terminating = false;
            }
            runningLock.unlock();
        }
    }

    private void execute(RecipeRequest request)
    {
        PulseExecutionContext context = request.getContext();
        File outputDir = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class).getOutputDir();
        Recipe recipe = null;

        context.push();
        try
        {
            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());
            CommandResult bootstrapResult = new CommandResult(bootstrapCommand.getName());
            if (pushContextAndExecute(context, bootstrapCommand, outputDir, 0, null, bootstrapResult))
            {
                return;
            }

            // Now we can load the recipe from the pulse file
            PulseFile pulseFile = loadPulseFile(request, context);
            String recipeName = request.getRecipeName();
            if (!TextUtils.stringSet(recipeName))
            {
                recipeName = pulseFile.getDefaultRecipe();
                if (!TextUtils.stringSet(recipeName))
                {
                    throw new BuildException("Please specify a default recipe for your project.");
                }
            }

            recipe = pulseFile.getRecipe(recipeName);
            if (recipe == null)
            {
                throw new BuildException("Undefined recipe '" + recipeName + "'");
            }

            boolean success = commandSucceeded(bootstrapResult);
            LinkedList<Pair<Command, Scope>> commands = recipe.getCommandScopePairs();
            for (int i = 0; i < commands.size(); i++)
            {
                Pair<Command, Scope> pair = commands.get(i);
                Command command = pair.first;
                Scope scope = pair.second;

                if (success || command.isForce())
                {
                    CommandResult result = new CommandResult(command.getName());
                    boolean recipeTerminated = pushContextAndExecute(context, command, outputDir, i + 1, scope, result);

                    if(recipeTerminated)
                    {
                        return;
                    }

                    success = success && commandSucceeded(result);
                }
            }
        }
        finally
        {
            context.pop();
            if (recipe != null && recipe.getVersion() != null)
            {
                context.setVersion(recipe.getVersion().getValue());
            }
        }
    }

    private boolean commandSucceeded(CommandResult result)
    {
        switch (result.getState())
        {
            case FAILURE:
            case ERROR:
                return false;
        }
        return true;
    }

    private boolean pushContextAndExecute(PulseExecutionContext context, Command command, File outputDir, int commandIndex, Scope scope, CommandResult result)
    {
        File commandOutput = new File(outputDir, Recipe.getCommandDirName(commandIndex, result));
        if (!commandOutput.mkdirs())
        {
            throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
        }

        context.setLabel(LABEL_EXECUTE);
        context.push();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, commandOutput.getAbsolutePath());

        if (scope != null)
        {
            context.getScope().add((PulseScope) scope);
        }

        boolean recipeTerminated = !executeCommand(context, commandOutput, result, command);
        context.popTo(LABEL_EXECUTE);
        return recipeTerminated;
    }

    private boolean executeCommand(ExecutionContext context, File commandOutput, CommandResult commandResult, Command command)
    {
        runningLock.lock();
        if (terminating)
        {
            runningLock.unlock();
            return false;
        }

        runningCommand = command;
        runningLock.unlock();

        commandResult.commence();
        commandResult.setOutputDir(commandOutput.getPath());
        long recipeId = context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, 0);
        eventManager.publish(new CommandCommencedEvent(this, recipeId, commandResult.getCommandName(), commandResult.getStartTime()));

        try
        {
            executeAndProcess(context, commandResult, command);
        }
        catch (BuildException e)
        {
            commandResult.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            commandResult.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            runningLock.lock();
            runningCommand = null;
            runningLock.unlock();

            flushOutput(context);
            commandResult.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, commandResult));
        }

        return true;
    }

    public static void executeAndProcess(ExecutionContext context, CommandResult commandResult, Command command)
    {
        try
        {
            command.execute(context, commandResult);
        }
        finally
        {
            // still need to process any available artifacts, even in the event of an error.
            processArtifacts(command, context, commandResult);
        }
    }

    static void processArtifacts(Command command, ExecutionContext context, CommandResult result)
    {
        for (Artifact artifact : command.getArtifacts())
        {
            try
            {
                artifact.capture(result, context);
            }
            catch (Exception e)
            {
                String message = "Unexpected error capturing artifact '" + artifact.getName() + "': " + e.getMessage();
                LOG.severe(message, e);
                result.error(message + " (check agent logs)");
            }
        }
    }

    private void flushOutput(ExecutionContext context)
    {
        OutputStream outputStream = context.getOutputStream();
        if(outputStream != null)
        {
            try
            {
                outputStream.flush();
            }
            catch (IOException e)
            {
                LOG.severe(e);
            }
        }
    }

    private void compressResults(RecipePaths paths, boolean compressArtifacts, boolean compressWorkingCopy)
    {
        if (compressArtifacts)
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compressing recipe artifacts..."));
            if(zipDir(paths.getOutputDir()))
            {
                eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Artifacts compressed."));
            }
        }

        if(compressWorkingCopy)
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compressing working copy snapshot..."));
            if(zipDir(paths.getBaseDir()))
            {
                eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Working copy snapshot compressed."));
            }
        }
    }

    private boolean zipDir(File dir)
    {
        try
        {
            File zipFile = new File(dir.getAbsolutePath() + ".zip");
            ZipUtils.createZip(zipFile, dir, null);
            return true;
        }
        catch (IOException e)
        {
            LOG.severe(e);
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compression failed: " + e.getMessage() + "."));
            return false;
        }
    }

    private void storeTestResults(RecipePaths paths, RecipeResult recipeResult, PersistentTestSuiteResult testResults)
    {
        eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Storing test results..."));
        try
        {
            TestSuitePersister persister = new TestSuitePersister();
            File testDir = new File(paths.getOutputDir(), RecipeResult.TEST_DIR);
            FileSystemUtils.createDirectory(testDir);
            persister.write(testResults, testDir);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write out test results", e);
        }

        recipeResult.setTestSummary(testResults.getSummary());
        eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Test results stored."));
    }

    private void storeCustomFields(RecipePaths paths, Map<String, String> customFields)
    {
        if (customFields.size() > 0)
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Storing custom fields..."));
            RecipeCustomFields recipeCustomFields = new RecipeCustomFields(paths.getOutputDir());
            recipeCustomFields.store(customFields);
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Custom fields stored."));
        }
    }

    private void pushRecipeContext(PulseExecutionContext context, RecipeRequest request, PersistentTestSuiteResult testResults, Map<String, String> customFields, long recipeStartTime)
    {
        context.push();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BASE_DIR, context.getWorkingDir().getAbsolutePath());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT_STRING).format(new Date(recipeStartTime)));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, Long.toString(recipeStartTime));
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_CUSTOM_FIELDS, customFields);

        if(context.getString(NAMESPACE_INTERNAL, PROPERTY_RECIPE) == null)
        {
            context.addString(PROPERTY_RECIPE, "[default]");
        }

        PulseScope scope = context.getScope();
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            scope.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        addResourceProperties(context, request.getResourceRequirements(), context.getValue(PROPERTY_RESOURCE_REPOSITORY, ResourceRepository.class));
        for(ResourceProperty property: request.getProperties())
        {
            context.add(property);
        }
    }

    private PulseFile loadPulseFile(RecipeRequest request, PulseExecutionContext context) throws BuildException
    {
        context.setLabel(SCOPE_RECIPE);
        PulseScope globalScope = new PulseScope(context.getScope());

        // CIB-286: special case empty file for better reporting
        PulseFileSource pulseFileSource = request.getPulseFileSource();
        if(!TextUtils.stringSet(pulseFileSource.getFileContent()))
        {
            throw new BuildException("Unable to parse pulse file: File is empty");
        }

        // load the pulse file from the source.
        InputStream stream = null;
        try
        {
            stream = new ByteArrayInputStream(pulseFileSource.getFileContent().getBytes());

            ResourceRepository resourceRepository = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, ResourceRepository.class);
            PulseFile result = new PulseFile();
            PulseFileLoader fileLoader = fileLoaderFactory.createLoader();
            FileResolver fileResolver = new RelativeFileResolver(pulseFileSource.getPath(), new LocalFileResolver(context.getWorkingDir()));
            fileLoader.load(stream, result, globalScope, fileResolver, resourceRepository, new RecipeLoadPredicate(result, request.getRecipeName()));
            return result;
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to parse pulse file: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public void terminateRecipe(long id) throws InterruptedException
    {
        // Preconditions:
        //   - this call is only made after the processor has sent the recipe
        //     commenced event
        // Responsibilities of this method:
        //   - after this call, no further command should be started
        //   - if a command is running during this call, it should be
        //     terminated
        runningLock.lock();
        try
        {
            // Check the id as it is possible for a request to come in after
            // the recipe has completed (which does no harm so long as we
            // don't terminate the next recipe!).
            if (runningRecipe == id)
            {
                terminating = true;
                if (runningCommand != null)
                {
                    runningCommand.terminate();
                }
            }
        }
        finally
        {
            runningLock.unlock();
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
