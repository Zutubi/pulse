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
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;
import com.zutubi.util.StringUtils;

/**
 * Action for the build artifacts tab.
 */
public class ViewArtifactsAction extends CommandActionBase
{
    private String artifactName;
    private String filter = User.DEFAULT_ARTIFACTS_FILTER;

    public void setArtifactName(String artifactName)
    {
        this.artifactName = artifactName;
    }

    public String getFilter()
    {
        return filter;
    }

    public long getSelectedId()
    {
        long id = 0;

        RecipeResultNode recipeResultNode = getRecipeResultNode();
        if (recipeResultNode != null)
        {
            CommandResult commandResult = getCommandResult();
            if (commandResult != null)
            {
                if (StringUtils.stringSet(artifactName) && commandResult.getArtifact(artifactName) != null)
                {
                    id = commandResult.getArtifact(artifactName).getId();
                }
                else
                {
                    id = commandResult.getId();
                }
            }
            else
            {
                id = recipeResultNode.getResult().getId();
            }
            
        }
        
        return id;
    }

    public String execute()
    {
        // Optional discovery down to the command level.
        getCommandResult();
        // We require at least down to the build level
        getRequiredBuildResult();

        User user = getLoggedInUser();
        if (user != null)
        {
            filter = user.getArtifactsFilter();
        }
        
        return SUCCESS;
    }
}
