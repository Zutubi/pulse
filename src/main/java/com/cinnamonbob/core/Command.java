package com.cinnamonbob.core;

import java.io.File;

/**
 * A command is a single step in building a project.
 */
public interface Command
{    
    /**
     * Executes this command and returns the result.
     * 
     * @param outputDir
     *        the directory in which to store command output
     * @return the result of executing the command
     * @throws InternalBuildFailureException
     *         if something catastrophic prevents normal operation
     */
    public abstract CommandResult execute(File outputDir) throws InternalBuildFailureException;
}
