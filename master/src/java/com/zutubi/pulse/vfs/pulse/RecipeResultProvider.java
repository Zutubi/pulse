package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 * A provider interface that indicates the current node represents a recipe result instance.
 *
 * @see com.zutubi.pulse.core.model.RecipeResult
 */
public interface RecipeResultProvider
{
    RecipeResult getRecipeResult();

    long getRecipeResultId();
}
