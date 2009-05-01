package com.zutubi.pulse.master;

import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.events.build.RecipeDispatchedEvent;
import com.zutubi.util.Pair;
import com.zutubi.util.Sort;

import java.io.File;
import java.util.*;

/**
 * A logger that writes the details out to a log file.
 */
public class DefaultRecipeLogger extends AbstractFileLogger implements RecipeLogger
{
    static final String PRE_RULE = "============================[ command output below ]============================";
    static final String POST_RULE = "============================[ command output above ]============================";

    private int hookCount = 0;

    public DefaultRecipeLogger(File logFile)
    {
        super(logFile);
    }

    public void log(RecipeAssignedEvent event)
    {
        logMarker("Recipe assigned to agent " + event.getAgent().getConfig().getName());
    }

    public void log(RecipeDispatchedEvent event)
    {
        logMarker("Recipe dispatched to agent " + event.getAgent().getConfig().getName());
    }

    public void log(RecipeCommencedEvent event, RecipeResult result)
    {
        logMarker("Recipe '" + result.getRecipeNameSafe() + "' commenced", result.getStamps().getStartTime());
    }

    public void log(CommandCommencedEvent event, CommandResult result)
    {
        logMarker("Command '" + result.getCommandName() + "' commenced", result.getStamps().getStartTime());
        writePreRule();
    }

    public void log(CommandCompletedEvent event, CommandResult result)
    {
        writePostRule();

        logMarker("Command '" + result.getCommandName() + "' completed with status " + result.getState().getPrettyString(), result.getStamps().getEndTime());
        if (result.getProperties().size() > 0)
        {
            List<Pair<String, String>> details = new ArrayList<Pair<String, String>>(result.getProperties().size());
            int longestKey = 0;
            for (Map.Entry property: result.getProperties().entrySet())
            {
                Pair<String, String> detail = new Pair<String, String>(property.getKey().toString(), property.getValue().toString());
                int keyLength = detail.first.length();
                if (keyLength > longestKey)
                {
                    longestKey = keyLength;
                }

                details.add(detail);
            }

            final Comparator<String> stringComparator = new Sort.StringComparator();
            Collections.sort(details, new Comparator<Pair<String, String>>()
            {
                public int compare(Pair<String, String> o1, Pair<String, String> o2)
                {
                    return stringComparator.compare(o1.first, o2.first);
                }
            });

            logMarker("Command details: ");
            for(Pair<String, String> detail: details)
            {
                logMarker(String.format("    %" + longestKey + "s: %s", detail.first, detail.second));
            }
            logMarker("End command details.");
        }
    }

    public void log(RecipeCompletedEvent event, RecipeResult result)
    {
        // Do nothing: completion message comes from complete
    }

    public void log(RecipeStatusEvent event)
    {
        logMarker(event.getMessage());
    }

    public void log(RecipeErrorEvent event, RecipeResult result)
    {
        logMarker("Recipe terminated with an error: " + event.getErrorMessage());
    }

    public void complete(RecipeResult result)
    {
        logMarker("Recipe '" + result.getRecipeNameSafe() + "' completed with status " + result.getState().getPrettyString(), result.getStamps().getEndTime());
    }

    public void collecting(RecipeResult recipeResult, boolean collectWorkingCopy)
    {
        logMarker("Collecting recipe artifacts" + (collectWorkingCopy ? " and working copy..." : "..."));
    }

    public void collectionComplete()
    {
        logMarker("Collection complete");
    }

    public void cleaning()
    {
        logMarker("Cleaning up agent work area...");
    }

    public void cleaningComplete()
    {
        logMarker("Clean up complete.");
    }

    public void postStage()
    {
        hookCount = 0;
        logMarker("Running post stage hooks...");
    }

    public void postStageComplete()
    {
        logMarker(String.format("Post stage hooks complete (%d hook%s run).", hookCount, hookCount == 1 ? "" : "s"));
    }

    public void hookCommenced(String name)
    {
        hookCount++;
        logMarker("Hook '" + name + "' commenced");
        writePreRule();
    }

    public void hookCompleted(String name)
    {
        writePostRule();
        logMarker("Hook '" + name + "' completed");
    }

    void writePreRule()
    {
        if (writer != null)
        {
            writer.println(PRE_RULE);
            writer.flush();
        }
    }

    void writePostRule()
    {
        if (writer != null)
        {
            completeOutput();
            writer.println(POST_RULE);
            writer.flush();
        }
    }
}
