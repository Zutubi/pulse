package com.cinnamonbob.core.config;

import java.io.File;

import com.cinnamonbob.model.CommandResult;

/**
 * 
 *
 */
public interface Command
{
    CommandResult execute(File outputDir) throws CommandException;
}
