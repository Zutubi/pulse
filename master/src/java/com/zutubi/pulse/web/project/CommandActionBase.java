package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.web.LookupErrorException;

/**
 * Helper base class for actions that may drill down to the command level.
 */
public class CommandActionBase extends StageActionBase
{
    private String commandName;
    private CommandResult commandResult;

    public String getCommandName()
    {
        return commandName;
    }

    public void setCommandName(String commandName)
    {
        this.commandName = commandName;
    }

    public String getu_commandName()
    {
        return uriComponentEncode(commandName);
    }

    public String geth_commandName()
    {
        return htmlEncode(commandName);
    }

    public CommandResult getCommandResult()
    {
        RecipeResultNode recipeResultNode = getRecipeResultNode();
        if (commandResult == null && recipeResultNode != null)
        {
            if (TextUtils.stringSet(commandName))
            {
                RecipeResult recipeResult = recipeResultNode.getResult();
                commandResult = recipeResult == null ? null : recipeResult.getCommandResult(commandName);
                if(commandResult == null)
                {
                    throw new LookupErrorException("Unknown command [" + commandName + "] for stage [" + recipeResultNode.getStageName() + "] of build [" + getBuildResult().getNumber() + "] of project [" + getProject().getName() + "]");
                }
            }
        }

        return commandResult;
    }

    public CommandResult getRequiredCommandResult()
    {
        CommandResult result = getCommandResult();
        if(result == null)
        {
            throw new LookupErrorException("Command name is required");
        }

        return commandResult;
    }
}
