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

package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;

import java.io.File;

/**
 */
public class DefaultRecipeResultCollector implements RecipeResultCollector
{
    private MasterBuildPaths paths;
    private ProjectConfiguration projectConfig;

    public DefaultRecipeResultCollector(MasterConfigurationManager configManager)
    {
        this.paths = new MasterBuildPaths(configManager);
    }

    public void prepare(BuildResult result, long recipeId)
    {
        // ensure that we have created the necessary directories.
        File recipeDir = paths.getRecipeDir(result, recipeId);
        if (!recipeDir.mkdirs())
        {
            throw new BuildException("Failed to create the '" + recipeDir + "' directory.");
        }
    }

    public void collect(BuildResult result, long recipeId, ExecutionContext context, AgentService agentService)
    {
        if (agentService != null)
        {
            File outputDest = paths.getOutputDir(result, recipeId);
            agentService.collectResults(new AgentRecipeDetails(context), outputDest);
        }
    }

    public void cleanup(long recipeId, ExecutionContext context, AgentService agentService)
    {
        if (agentService != null)
        {
            agentService.cleanup(new AgentRecipeDetails(context));
        }
    }

    private AgentRecipeDetails getRecipeDetails(AgentConfiguration agent, long stageHandle, String stage, long recipeId, boolean incremental, boolean update)
    {
        AgentRecipeDetails details = new AgentRecipeDetails();
        details.setAgentHandle(agent.getHandle());
        details.setAgent(agent.getName());
        details.setAgentDataPattern(agent.getStorage().getDataDirectory());
        details.setProjectHandle(projectConfig.getHandle());
        details.setProject(projectConfig.getName());
        details.setProjectPersistentPattern(projectConfig.getBootstrap().getPersistentDirPattern());
        details.setProjectTempPattern(projectConfig.getBootstrap().getTempDirPattern());
        details.setStageHandle(stageHandle);
        details.setStage(stage);
        details.setRecipeId(recipeId);
        details.setIncremental(incremental);
        details.setUpdate(update);
        return details;
    }

    public File getRecipeDir(BuildResult result, long recipeId)
    {
        return paths.getRecipeDir(result, recipeId);
    }

    public void setProjectConfig(ProjectConfiguration projectConfig)
    {
        this.projectConfig = projectConfig;
    }
}
