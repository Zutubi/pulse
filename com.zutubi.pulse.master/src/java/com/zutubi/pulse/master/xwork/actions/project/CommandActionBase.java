/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;

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
            if (StringUtils.stringSet(commandName))
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
