package com.cinnamonbob.local;

import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.ForkOutputStream;
import com.cinnamonbob.core.util.TimeStamps;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
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
    private RecipeResult result;

    public BuildStatusPrinter(File work, OutputStream logStream)
    {
        ForkOutputStream fork = new ForkOutputStream(System.out, logStream);
        indenter = new Indenter(new PrintStream(fork), "  ");
        workDir = work.getAbsolutePath() + File.separatorChar;
        result = new RecipeResult();
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
        else if (recipeEvent instanceof RecipeErrorEvent)
        {
            handleRecipeError((RecipeErrorEvent) recipeEvent);
        }
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        String recipeName = event.getName();

        if (recipeName == null)
        {
            recipeName = "<default>";
        }

        result.commence(event.getName(), event.getStartTime());

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
        CommandResult commandResult = event.getResult();
        result.add(commandResult);

        indenter.println("completed: " + commandResult.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + commandResult.getStamps().getPrettyElapsed());
        indenter.println("result   : " + commandResult.getState().getPrettyString());

        showMessages(commandResult);

        List<StoredArtifact> artifacts = commandResult.getArtifacts();
        if (artifacts.size() > 0)
        {
            showArtifacts(commandResult, artifacts);
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
        result.update(event.getResult());
        complete();
    }

    private void handleRecipeError(RecipeErrorEvent event)
    {
        result.error(event.getErrorMessage());
        complete();
    }

    private void complete()
    {
        result.complete();

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
