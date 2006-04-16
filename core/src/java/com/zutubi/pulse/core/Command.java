/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public interface Command
{
    void execute(long recipeId, RecipePaths paths, File outputDir, CommandResult result);

    List<String> getArtifactNames();

    String getName();

    void setName(String name);

    void terminate();
}
