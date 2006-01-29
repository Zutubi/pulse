package com.cinnamonbob.slave.command;

import com.cinnamonbob.slave.SlaveRecipeProcessor;
import com.cinnamonbob.util.logging.Logger;

/**
 */
public class TerminateRecipeCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(TerminateRecipeCommand.class);

    private long recipeId;
    private SlaveRecipeProcessor recipeProcessor;

    public TerminateRecipeCommand(long recipeId)
    {
        this.recipeId = recipeId;
    }

    public void run()
    {
        recipeProcessor.terminateRecipe();
    }

    public void setRecipeProcessor(SlaveRecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }
}
