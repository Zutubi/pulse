package com.cinnamonbob.core;

import java.io.File;
import java.util.List;

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
     * @param previousBuild
     *        result of the previous build, or null if not available
     * @return the result of executing the command
     * @throws InternalBuildFailureException
     *         if something catastrophic prevents normal operation
     */
    public CommandResult execute(File outputDir, BuildResult previousBuild) throws InternalBuildFailureException;
    
    /**
     * Indicates the artifacts that will be produced by this command during
     * its execution.  A typical example is the raw command output.
     * 
     * @return a list of specifications describing the artifacts produced by
     *         this command, or null if none are produced
     */
    public List<ArtifactSpec> getArtifacts();
}
