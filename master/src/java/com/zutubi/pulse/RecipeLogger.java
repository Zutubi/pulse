package com.zutubi.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.events.build.*;

/**
 * Creates the combined log of a recipe's execution.
 */
public interface RecipeLogger
{
    void prepare();

    void log(RecipeDispatchedEvent event);
    void log(RecipeCommencedEvent event, RecipeResult result);
    void log(CommandCommencedEvent event, CommandResult result);
    void log(OutputEvent event);
    void log(CommandCompletedEvent event, CommandResult result);
    void log(RecipeCompletedEvent event, RecipeResult result);
    void log(RecipeStatusEvent event);
    void log(RecipeErrorEvent event, RecipeResult result);

    void complete(RecipeResult result);

    void collecting(RecipeResult recipeResult, boolean collectWorkingCopy);
    void collectionComplete();

    void cleaning();
    void cleaningComplete();

    void postStage();
    void postStageComplete();

    void done();
}
