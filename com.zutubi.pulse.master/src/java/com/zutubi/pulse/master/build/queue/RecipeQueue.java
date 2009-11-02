package com.zutubi.pulse.master.build.queue;

import java.util.List;

/**
 */
public interface RecipeQueue
{
    void enqueue(RecipeAssignmentRequest request);

    List<RecipeAssignmentRequest> takeSnapshot();

    /**
     * Attempts to cancel the request for the given recipe.
     *
     * @param id the id of the recipe to cancel the request for
     * @return true if the request was cancelled, false if it was missed
     */
    boolean cancelRequest(long id);

    void start();

    void stop();

    boolean isRunning();
}
