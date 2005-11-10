package com.cinnamonbob.local;

import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.*;
import com.cinnamonbob.core.model.*;

import java.util.List;
import java.io.File;

/**
 * Prints status information to standard out while doing a local build.
 */
public class BuildStatusPrinter implements EventListener
{
    private Indenter indenter;
    private String workDir;


    public BuildStatusPrinter(File work)
    {
        indenter = new Indenter(System.out, "  ");
        workDir = work.getAbsolutePath() + File.separatorChar;
    }


    public void handleEvent(Event event)
    {
        BuildEvent buildEvent = (BuildEvent)event;

        if(buildEvent instanceof  BuildCommencedEvent)
        {
            handleBuildCommenced(buildEvent);
        }
        else if(buildEvent instanceof CommandCommencedEvent)
        {
            handleCommandCommenced(buildEvent);
        }
        else if(buildEvent instanceof CommandCompletedEvent)
        {
            handleCommandCompleted(buildEvent);
        }
        else if(buildEvent instanceof BuildCompletedEvent)
        {
            handleBuildCompleted(buildEvent);
        }
    }

    private void handleBuildCommenced(BuildEvent buildEvent)
    {
        BuildResult result = buildEvent.getResult();

        indenter.println("[" + result.getProjectName() + "]");
        indenter.indent();
        indenter.println("commenced: " + result.getStamps().getPrettyStartTime());
    }

    private void handleCommandCommenced(BuildEvent buildEvent)
    {
        CommandResult result = getLastCommandResult(buildEvent);

        indenter.println("[" + result.getCommandName() + "]");
        indenter.indent();
        indenter.println("commenced: " + result.getStamps().getPrettyStartTime());
    }

    private void handleCommandCompleted(BuildEvent buildEvent)
    {
        CommandResult result = getLastCommandResult(buildEvent);

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        if(result.errored())
        {
            showErrorDetails(result);
        }

        List<StoredArtifact> artifacts = result.getArtifacts();
        if(artifacts.size() > 0)
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

        if(result.getErrorMessage() != null)
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
        for(StoredArtifact artifact: artifacts)
        {
            File file = new File(artifact.getFile());
            indenter.println("* " + artifact.getTitle() + " (" + getFilePath(file) + ")");

            for(Feature.Level level: Feature.Level.values())
            {
                List<Feature> features = artifact.getFeatures(level);
                if(features.size() > 0)
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
        if(result.startsWith(workDir))
        {
            result = result.substring(workDir.length());
        }

        return result;
    }

    private void showFeatures(Feature.Level level, List<Feature> features)
    {
        indenter.println(level.toString().toLowerCase() + " features:");
        indenter.indent();

        for(Feature f: features)
        {
            indenter.println("* " + f.getSummary());
        }

        indenter.dedent();
    }

    private void handleBuildCompleted(BuildEvent buildEvent)
    {
        BuildResult result = buildEvent.getResult();

        indenter.println("completed: " + result.getStamps().getPrettyEndTime());
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        if(result.errored())
        {
            showErrorDetails(result);
        }

        indenter.dedent();
    }

    private CommandResult getLastCommandResult(BuildEvent buildEvent)
    {
        List<CommandResult> results = buildEvent.getResult().getCommandResults();
        return results.get(results.size() - 1);
    }


    public Class[] getHandledEvents()
    {
        return new Class[]{ BuildEvent.class };
    }
}
