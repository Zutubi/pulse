package com.cinnamonbob.core2.config;

import java.io.File;

/**
 * 
 *
 */
public interface Command
{
    CommandResult execute(File outputDir) throws CommandException;
}
