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
    String OUTPUT_ARTIFACT_NAME = "command output";
    String OUTPUT_FILENAME = "output.txt";

    void execute(CommandContext context, CommandResult result);

    List<String> getArtifactNames();

    @Required String getName();

    void setName(String name);

    boolean isForce();

    void terminate();
}
