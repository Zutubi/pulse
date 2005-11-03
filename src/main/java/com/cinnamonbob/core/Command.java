package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;

import java.io.File;

/**
 * 
 *
 */
public interface Command
{
    void execute(File outputDir, CommandResult result);

    String getName();
    void setName(String name);
}
