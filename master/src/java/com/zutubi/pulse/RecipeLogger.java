package com.zutubi.pulse;

import com.zutubi.pulse.events.build.*;

/**
 * Creates the combined log of a recipe's execution.
 */
public interface RecipeLogger
{
    void log(RecipeDispatchedEvent event);
    void log(RecipeCommencedEvent event);
    void log(CommandCommencedEvent event);
    void log(CommandOutputEvent event);
    void log(CommandCompletedEvent event);
    void log(RecipeCompletedEvent event);
    void log(RecipeErrorEvent event);

    void prepare();
    void complete();
}
