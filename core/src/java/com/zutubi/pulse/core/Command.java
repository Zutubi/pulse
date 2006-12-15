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

    List<Artifact> getArtifacts();

    /**
     * The name of the command is used to identify it.
     *
     * @return name of the command.
     */
    @Required String getName();

    void setName(String name);

    void terminate();
}
