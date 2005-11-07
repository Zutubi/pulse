package com.cinnamonbob.local;

import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.BuildEvent;
import com.cinnamonbob.core.CommandCommencedEvent;
import com.cinnamonbob.core.CommandCompletedEvent;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;

import java.util.List;
import java.io.File;

/**
 * Prints status information to standard out while doing a local build.
 */
public class BuildStatusPrinter implements EventListener
{
    private Indenter indenter;

    
    public BuildStatusPrinter()
    {
        indenter = new Indenter(System.out, "  ");
    }


    public void handleEvent(Event event)
    {
        BuildEvent buildEvent = (BuildEvent)event;

        if(buildEvent instanceof CommandCommencedEvent)
        {
            CommandResult result = getLastCommandResult(buildEvent);
            indenter.println("[" + result.getCommandName() + "]");
            indenter.indent();
            indenter.println("commenced: " + result.getStamps().getPrettyStartTime());
        }
        else if(buildEvent instanceof CommandCompletedEvent)
        {
            CommandResult result = getLastCommandResult(buildEvent);
            indenter.println("completed: " + result.getStamps().getPrettyEndTime());
            indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
            indenter.println("result   : " + result.getState().getPrettyString());

            List<StoredArtifact> artifacts = result.getArtifacts();
            if(artifacts.size() > 0)
            {
                indenter.println("artifacts:");
                indenter.indent();
                for(StoredArtifact artifact: artifacts)
                {
                    File file = new File(artifact.getFile());

                    indenter.println(file.getPath());
                }
                indenter.dedent();
            }

            indenter.println();
            indenter.dedent();
        }
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
