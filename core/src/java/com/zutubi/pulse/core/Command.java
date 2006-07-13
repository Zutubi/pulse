package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.util.List;

/**
 * 
 *
 */
public interface Command
{
    void execute(long recipeId, CommandContext context, CommandResult result);

    List<String> getArtifactNames();

    String getName();

    void setName(String name);

    void terminate();
}
