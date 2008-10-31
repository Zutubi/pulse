package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.RecipeResult;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a recipe result instance.
 *
 * @see com.zutubi.pulse.core.model.RecipeResult
 */
public interface RecipeResultProvider
{
    RecipeResult getRecipeResult() throws FileSystemException;

    long getRecipeResultId() throws FileSystemException;
}
