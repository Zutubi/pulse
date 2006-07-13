package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
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
        if (fileLoader == null)
        {
            fileLoader = new PulseFileLoader(new ObjectFactory());
        }
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    public void build(RecipeRequest request, RecipePaths paths, ResourceRepository resourceRepository, boolean capture)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult result = new RecipeResult(request.getRecipeName());
        result.setId(request.getId());
        result.commence();

        runningRecipe = request.getId();
        eventManager.publish(new RecipeCommencedEvent(this, request.getId(), request.getRecipeName(), result.getStamps().getStartTime()));

        try
        {
            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());
            CommandResult bootstrapResult = new CommandResult(bootstrapCommand.getName());
            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(0, bootstrapResult));

            executeCommand(request.getId(), bootstrapResult, paths, commandOutput, bootstrapCommand, capture);

            if (bootstrapResult.succeeded())
            {
                // Now we can load the recipe from the pulse file
                PulseFile pulseFile = loadPulseFile(request, paths.getBaseDir(), resourceRepository);
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

                build(request.getId(), recipe, paths, capture);
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
            result.complete();
            eventManager.publish(new RecipeCompletedEvent(this, result));

            runningLock.lock();
            runningRecipe = 0;
            if (terminating)
            {
                terminating = false;
            }
            runningLock.unlock();
        }
    }

    public void build(long recipeId, Recipe recipe, RecipePaths paths, boolean capture) throws BuildException
    {
        // TODO: support continuing build when errors occur. Take care: exceptions.
        int i = 1;
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(i, result));

            runningLock.lock();
            if (terminating)
            {
                runningLock.unlock();
                return;
            }

            runningCommand = command;
            runningLock.unlock();

            executeCommand(recipeId, result, paths, commandOutput, command, capture);

            switch (result.getState())
            {
                case FAILURE:
                case ERROR:
                    return;
            }
            i++;
        }
    }

    private void executeCommand(long recipeId, CommandResult result, RecipePaths paths, File commandOutput, Command command, boolean capture)
    {
        result.commence();
        result.setOutputDir(commandOutput.getPath());
        eventManager.publish(new CommandCommencedEvent(this, recipeId, result.getCommandName(), result.getStamps().getStartTime()));

        try
        {
            if (!commandOutput.mkdirs())
            {
                throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
            }

            CommandContext context = new CommandContext(paths, commandOutput);
            if(capture)
            {
                context.setOutputStream(new CommandOutputStream(eventManager, recipeId, true));
            }

            command.execute(recipeId, context, result);
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
            result.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, result));
        }
    }

    private PulseFile loadPulseFile(RecipeRequest request, File baseDir, ResourceRepository resourceRepository) throws BuildException
    {
        Scope globalScope = new Scope();
        Property property = new Property("base.dir", baseDir.getAbsolutePath());
        globalScope.add(property);

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
                if(requirement.getVersion() != null)
                {
                    ResourceVersion version = resource.getVersion(requirement.getVersion());
                    if(version == null)
                    {
                        throw new BuildException("Reference to non-existant version '" + requirement.getVersion() + "' of resource '" + requirement.getResource() + "'");
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
