package com.cinnamonbob;

import java.util.List;

/**
 */
public interface RecipeQueue
{
    void enqueue(RecipeDispatchRequest request);

    List<RecipeDispatchRequest> takeSnapshot();
}
