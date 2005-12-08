package com.cinnamonbob;

/**
 * A request to execute a specific recipe.  Includes details about how to
 * bootstrap this step of the build (e.g. by SCM checkout, or by using a
 * working directory left by a previous recipe).
 */
public class RecipeRequest
{
    /**
     * Used to bootstrap the working directory.
     */
    private Bootstrapper bootstrapper;
    /**
     * Path of the bobfile relative to the working root (after bootstrap).
     */
    private String bobFile;
    /**
     * The name of the recipe to execute, or null to execute the default.
     */
    private String recipeName;

}
