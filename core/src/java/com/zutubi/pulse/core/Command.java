package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * 
 *
 */
public interface Command
{
    void execute(long recipeId, CommandContext context, CommandResult result);

    List<String> getArtifactNames();

    @Required String getName();

    void setName(String name);

    void terminate();
}
