package com.cinnamonbob.local;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.ForkOutputStream;
import com.cinnamonbob.core.util.TimeStamps;
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
            handleRecipeCommenced((RecipeCommencedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof CommandCommencedEvent)
        {
            handleCommandCommenced((CommandCommencedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof CommandCompletedEvent)
        {
            handleCommandCompleted((CommandCompletedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof RecipeCompletedEvent)
        {
            handleRecipeCompleted((RecipeCompletedEvent) recipeEvent);
        }
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        String recipeName = event.getName();

        if (recipeName == null)
        {
            recipeName = "<default>";
        }

        indenter.println("[" + recipeName + "]");
        indenter.indent();
        indenter.println("commenced: " + TimeStamps.getPrettyTime(event.getStartTime()));
    }

    private void handleCommandCommenced(CommandCommencedEvent event)
    {
        indenter.println("[" + event.getName() + "]");
        indenter.indent();
        indenter.println("commenced: " + TimeStamps.getPrettyTime(event.getStartTime()));
    }

    private void handleCommandCompleted(CommandCompletedEvent event)
    {
        CommandResult result = event.getResult();

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        showMessages(result);

        List<StoredArtifact> artifacts = result.getArtifacts();
        if (artifacts.size() > 0)
        {
            showArtifacts(result, artifacts);
        }

        indenter.println();
        indenter.dedent();
    }

    private void showMessages(Result result)
    {
        if (result.errored())
        {
            showErrorDetails(result.getErrorMessage());
        }
        else if (result.failed())
        {
            showErrorDetails(result.getFailureMessage());
        }
    }

    private void showErrorDetails(String message)
    {
        indenter.println("message  :");
        indenter.indent();

        if (message != null)
        {
            indenter.println(message);
        }
        else
        {
            indenter.println("<unknown>");
        }

        indenter.dedent();
    }

    private void showArtifacts(CommandResult result, List<StoredArtifact> artifacts)
    {
        indenter.println("artifacts:");
        indenter.indent();
        for (StoredArtifact artifact : artifacts)
        {
            indenter.println("* " + artifact.getTitle() + " (" + getFilePath(result, artifact.getFile()) + ")");

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

    private String getFilePath(CommandResult commandResult, String name)
    {
        File path = new File(commandResult.getOutputDir(), name);
        String result = path.getPath();

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

    private void handleRecipeCompleted(RecipeCompletedEvent event)
    {
        RecipeResult result = event.getResult();

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        showMessages(result);

        indenter.dedent();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeEvent.class};
    }
}
