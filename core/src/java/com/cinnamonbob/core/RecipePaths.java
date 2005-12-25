package com.cinnamonbob.core;

import java.io.File;

/**
 * The RecipePaths interface provides an interface that provides access to a recipies
 * work and output directories.
 *
 * 
 */
public interface RecipePaths
{
    /**
     * The work directory is, as the name suggests, the directory in which all of
     * the work is done.
     *
     * Everything in the working directory is considered transient and will be deleted
     * after the execution of the recipe is complete.
     *
     * @return the working directory
     */
    File getWorkDir();

    /**
     * The output directory is the directory that contains all of the recipe processing
     * output. This includes all of the recipe artifacts and various log files generated
     * by the recipe execution.
     *
     * Everything in the output directory is archived so that it can be used later.
     *
     * @return the output directory.
     */
    File getOutputDir();
}
