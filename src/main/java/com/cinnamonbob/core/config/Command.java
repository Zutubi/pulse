package com.cinnamonbob.core.config;

import java.io.File;

import com.cinnamonbob.model.CommandResult;

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
