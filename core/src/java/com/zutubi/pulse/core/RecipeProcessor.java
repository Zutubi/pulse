package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private EventManager eventManager;
    private Lock runningLock = new ReentrantLock();
    private long runningRecipe = 0;
    private Command runningCommand = null;
    private boolean terminating = false;
    private FileLoader fileLoader;

    public RecipeProcessor()
    {
    }

    public void init()
    {
        
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    public void build(RecipeRequest request, RecipePaths paths, ResourceRepository resourceRepository, boolean capture, BuildContext context)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult result = new RecipeResult(request.getRecipeName());
        TestSuiteResult testResults = new TestSuiteResult();
        result.setId(request.getId());
        result.commence();

        runningRecipe = request.getId();
        eventManager.publish(new RecipeCommencedEvent(this, request.getId(), request.getRecipeName(), result.getStamps().getStartTime()));

        try
        {
            long recipeStartTime = result.getStamps().getStartTime();

            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());
            CommandResult bootstrapResult = new CommandResult(bootstrapCommand.getName());
            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(0, bootstrapResult));
            Scope globalScope = new Scope();

            if(executeCommand(request.getId(), globalScope, recipeStartTime, bootstrapResult, paths, commandOutput, testResults, bootstrapCommand, capture, context) &&
               bootstrapResult.succeeded())
            {
                // Now we can load the recipe from the pulse file
                PulseFile pulseFile = loadPulseFile(request, paths.getBaseDir(), resourceRepository, globalScope, context, recipeStartTime);
                Recipe recipe;

                String recipeName = request.getRecipeName();
                if (recipeName == null)
                {
                    recipeName = pulseFile.getDefaultRecipe();
                    if (recipeName == null)
                    {
                        throw new BuildException("Please specify a default recipe for your project.");
                    }
                }

                recipe = pulseFile.getRecipe(recipeName);
                if (recipe == null)
                {
                    throw new BuildException("Undefined recipe '" + recipeName + "'");
                }

                build(request.getId(), globalScope, recipeStartTime, recipe, paths, testResults, capture, context);
            }
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            result.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            writeTestResults(paths, testResults);
            result.setTestSummary(testResults.getSummary());
            result.complete();
            RecipeCompletedEvent completedEvent = new RecipeCompletedEvent(this, result);
            if(context != null)
            {
                completedEvent.setBuildVersion(context.getBuildVersion());
            }
            
            eventManager.publish(completedEvent);

            runningLock.lock();
            runningRecipe = 0;
            if (terminating)
            {
                terminating = false;
            }
            runningLock.unlock();
        }
    }

    private void writeTestResults(RecipePaths paths, TestSuiteResult testResults)
    {
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
    }

    public void build(long recipeId, Scope globalScope, long recipeStartTime, Recipe recipe, RecipePaths paths, TestSuiteResult testResults, boolean capture, BuildContext context) throws BuildException
    {
        // TODO: support continuing build when errors occur. Take care: exceptions.
        int i = 1;
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(i, result));

            if(!executeCommand(recipeId, globalScope, recipeStartTime, result, paths, commandOutput, testResults, command, capture, context))
            {
                return;
            }

            switch (result.getState())
            {
                case FAILURE:
                case ERROR:
                    return;
            }
            i++;
        }
    }

    private boolean executeCommand(long recipeId, Scope globalScope, long recipeStartTime, CommandResult result, RecipePaths paths, File commandOutput, TestSuiteResult testResults, Command command, boolean capture, BuildContext context)
    {
        runningLock.lock();
        if (terminating)
        {
            runningLock.unlock();
            return false;
        }

        runningCommand = command;
        runningLock.unlock();

        result.commence();
        result.setOutputDir(commandOutput.getPath());
        eventManager.publish(new CommandCommencedEvent(this, recipeId, result.getCommandName(), result.getStamps().getStartTime()));
        CommandOutputStream outputStream = null;

        try
        {
            if (!commandOutput.mkdirs())
            {
                throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
            }

            CommandContext commandContext = new CommandContext(paths, commandOutput, testResults);
            commandContext.setGlobalScope(globalScope);
            commandContext.setRecipeStartTime(recipeStartTime);

            if (context != null && context.getBuildNumber() != -1)
            {
                commandContext.setBuildContext(context);
            }
            if(capture)
            {
                outputStream = new CommandOutputStream(eventManager, recipeId, true);
                commandContext.setOutputStream(outputStream);
            }

            command.execute(commandContext, result);
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            result.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            IOUtils.close(outputStream);
            result.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, result));
        }

        return true;
    }

    private PulseFile loadPulseFile(RecipeRequest request, File baseDir, ResourceRepository resourceRepository, Scope globalScope, BuildContext buildContext, long recipeStartTime) throws BuildException
    {
        globalScope.add(new Property("base.dir", baseDir.getAbsolutePath()));
        globalScope.add(new Property("recipe.timestamp", BuildContext.PULSE_BUILD_TIMESTAMP_FORMAT.format(new Date(recipeStartTime))));
        globalScope.add(new Property("recipe.timestamp.millis", Long.toString(recipeStartTime)));
        if(buildContext != null)
        {
            globalScope.add(new Property("build.number", Long.toString(buildContext.getBuildNumber())));
            globalScope.add(new Property("build.revision", buildContext.getBuildRevision()));
            globalScope.add(new Property("build.timestamp", BuildContext.PULSE_BUILD_TIMESTAMP_FORMAT.format(new Date(buildContext.getBuildTimestamp()))));
            globalScope.add(new Property("build.timestamp.millis", Long.toString(buildContext.getBuildTimestamp())));
        }

        addEnvironment(globalScope);
        if(request.getProperties() != null)
        {
            globalScope.add(request.getProperties());
        }
        
        importResources(resourceRepository, request.getResourceRequirements(), globalScope);

        InputStream stream = null;

        try
        {
            // CIB-286: special case empty file for better reporting
            String pulseFileSource = request.getPulseFileSource();
            if(pulseFileSource.trim().length() == 0)
            {
                throw new ParseException("File is empty");
            }

            stream = new ByteArrayInputStream(pulseFileSource.getBytes());
            PulseFile result = new PulseFile();
            fileLoader.load(stream, result, globalScope, resourceRepository, new RecipeLoadPredicate(result, request.getRecipeName()));
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

    private void addEnvironment(Scope globalScope)
    {
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            globalScope.addEnvironmentProperty(var.getKey(), var.getValue());
        }
    }

    private void importResources(ResourceRepository resourceRepository, List<ResourceRequirement> resourceRequirements, Scope scope)
    {
        if (resourceRequirements != null)
        {
            for(ResourceRequirement requirement: resourceRequirements)
            {
                Resource resource = resourceRepository.getResource(requirement.getResource());
                if(resource == null)
                {
                    throw new BuildException("Unable to import required resource '" + requirement.getResource() + "'");
                }

                scope.add(resource.getProperties().values());

                String importVersion = requirement.getVersion();
                if(importVersion == null)
                {
                    importVersion = resource.getDefaultVersion();
                }

                if(TextUtils.stringSet(importVersion))
                {
                    ResourceVersion version = resource.getVersion(importVersion);
                    if(version == null)
                    {
                        throw new BuildException("Reference to non-existant version '" + importVersion + "' of resource '" + requirement.getResource() + "'");
                    }

                    scope.add(version.getProperties().values());
                }
            }
        }
    }

    /**
     * The event manager is a required reference.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public FileLoader getFileLoader()
    {
        return fileLoader;
    }

    public void setFileLoader(FileLoader fileLoader)
    {
        this.fileLoader = fileLoader;
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

    public long getBuildingRecipe()
    {
        return runningRecipe;
    }
}
