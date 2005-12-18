package com.cinnamonbob.local;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.ForkOutputStream;
import com.cinnamonbob.events.build.*;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Prints status information to standard out while doing a local build.
 */
public class BuildStatusPrinter implements EventListener
{
    private Indenter indenter;
    private String workDir;


    public BuildStatusPrinter(File work, OutputStream logStream)
    {
        ForkOutputStream fork = new ForkOutputStream(System.out, logStream);
        indenter = new Indenter(new PrintStream(fork), "  ");
        workDir = work.getAbsolutePath() + File.separatorChar;
    }


    public void handleEvent(Event event)
    {
        RecipeEvent recipeEvent = (RecipeEvent) event;

        if (recipeEvent instanceof RecipeCommencedEvent)
        {
            handleBuildCommenced(recipeEvent);
        }
        else
        {
            if (recipeEvent instanceof CommandCommencedEvent)
            {
                handleCommandCommenced(recipeEvent);
            }
            else
            {
                if (recipeEvent instanceof CommandCompletedEvent)
                {
                    handleCommandCompleted(recipeEvent);
                }
                else
                {
                    if (recipeEvent instanceof RecipeCompletedEvent)
                    {
                        handleBuildCompleted(recipeEvent);
                    }
                }
            }
        }
    }

    private void handleBuildCommenced(RecipeEvent recipeEvent)
    {
        RecipeResult result = recipeEvent.getResult();
        String recipeName = result.getRecipeName();

        if (recipeName == null)
        {
            recipeName = "<default>";
        }

        indenter.println("[" + recipeName + "]");
        indenter.indent();
        indenter.println("commenced: " + result.getStamps().getPrettyStartTime());
    }

    private void handleCommandCommenced(RecipeEvent recipeEvent)
    {
        CommandResult result = getLastCommandResult(recipeEvent);

        indenter.println("[" + result.getCommandName() + "]");
        indenter.indent();
        indenter.println("commenced: " + result.getStamps().getPrettyStartTime());
    }

    private void handleCommandCompleted(RecipeEvent recipeEvent)
    {
        CommandResult result = getLastCommandResult(recipeEvent);

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        if (result.errored())
        {
            showErrorDetails(result);
        }

        List<StoredArtifact> artifacts = result.getArtifacts();
        if (artifacts.size() > 0)
        {
            showArtifacts(artifacts);
        }

        indenter.println();
        indenter.dedent();
    }

    private void showErrorDetails(Result result)
    {
        indenter.println("message  :");
        indenter.indent();

        if (result.getErrorMessage() != null)
        {
            indenter.println(result.getErrorMessage());
        }
        else
        {
            indenter.println("<unknown>");
        }

        indenter.dedent();
    }

    private void showArtifacts(List<StoredArtifact> artifacts)
    {
        indenter.println("artifacts:");
        indenter.indent();
        for (StoredArtifact artifact : artifacts)
        {
            File file = new File(artifact.getFile());
            indenter.println("* " + artifact.getTitle() + " (" + getFilePath(file) + ")");

            for (Feature.Level level : Feature.Level.values())
            {
                List<Feature> features = artifact.getFeatures(level);
                if (features.size() > 0)
                {
                    indenter.indent();
                    showFeatures(level, features);
                    indenter.dedent();
                }
            }

        }
        indenter.dedent();
    }

    private String getFilePath(File file)
    {
        String result = file.getPath();
        if (result.startsWith(workDir))
        {
            result = result.substring(workDir.length());
        }

        return result;
    }

    private void showFeatures(Feature.Level level, List<Feature> features)
    {
        indenter.println(level.toString().toLowerCase() + " features:");
        indenter.indent();

        for (Feature f : features)
        {
            indenter.println("* " + f.getSummary());
        }

        indenter.dedent();
    }

    private void handleBuildCompleted(RecipeEvent recipeEvent)
    {
        RecipeResult result = recipeEvent.getResult();

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        if (result.errored())
        {
            showErrorDetails(result);
        }

        indenter.dedent();
    }

    private CommandResult getLastCommandResult(RecipeEvent recipeEvent)
    {
        List<CommandResult> results = recipeEvent.getResult().getCommandResults();
        return results.get(results.size() - 1);
    }


    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeEvent.class};
    }
}
