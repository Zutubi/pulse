package com.cinnamonbob.core2.config;

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
