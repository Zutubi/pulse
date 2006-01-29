package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public interface Command
{
    void execute(File workDir, File outputDir, CommandResult result);

    List<String> getArtifactNames();

    String getName();

    void setName(String name);

    void terminate();
}
