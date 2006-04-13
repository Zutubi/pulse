/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.local;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.util.ForkOutputStream;
import com.zutubi.pulse.core.util.TimeStamps;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.*;

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
    private String baseDir;
    private RecipeResult result;

    public BuildStatusPrinter(File base, OutputStream logStream)
    {
        ForkOutputStream fork = new ForkOutputStream(System.out, logStream);
        indenter = new Indenter(new PrintStream(fork), "  ");
        baseDir = base.getAbsolutePath() + File.separatorChar;
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
        if (result.hasDirectMessages(Feature.Level.ERROR))
        {
            indenter.println("errors   :");
            showMessages(result, Feature.Level.ERROR);
        }

        if (result.hasDirectMessages(Feature.Level.WARNING))
        {
            indenter.println("warnings :");
            showMessages(result, Feature.Level.WARNING);
        }

        if (result.hasDirectMessages(Feature.Level.INFO))
        {
            indenter.println("info     :");
            showMessages(result, Feature.Level.INFO);
        }
    }

    private void showMessages(Result result, Feature.Level level)
    {
        indenter.indent();
        for (Feature feature : result.getFeatures(level))
        {
            indenter.println(feature.getSummary());
        }
        indenter.dedent();
    }

    private void showArtifacts(CommandResult result, List<StoredArtifact> artifacts)
    {
        indenter.println("artifacts:");
        indenter.indent();
        for (StoredArtifact artifact : artifacts)
        {
            indenter.println("* " + artifact.getName());
            indenter.indent();

            for (StoredFileArtifact fileArtifact : artifact.getChildren())
            {
                indenter.println("* " + getFilePath(result, fileArtifact.getPath()));

                for (Feature.Level level : Feature.Level.values())
                {
                    List<Feature> features = fileArtifact.getFeatures(level);
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
        indenter.dedent();
    }

    private String getFilePath(CommandResult commandResult, String name)
    {
        File path = new File(commandResult.getOutputDir(), name);
        String result = path.getPath();

        if (result.startsWith(baseDir))
        {
            result = result.substring(baseDir.length());
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
