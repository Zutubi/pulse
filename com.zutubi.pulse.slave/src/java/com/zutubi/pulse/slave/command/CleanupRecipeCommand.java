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

package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 */
public class CleanupRecipeCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(CleanupRecipeCommand.class);

    private AgentRecipeDetails recipeDetails;
    private ConfigurationManager configurationManager;

    public CleanupRecipeCommand(AgentRecipeDetails recipeDetails)
    {
        this.recipeDetails = recipeDetails;
    }

    public void run()
    {
        ServerRecipePaths recipeProcessorPaths = new ServerRecipePaths(recipeDetails, configurationManager.getUserPaths().getData());
        File recipeRoot = recipeProcessorPaths.getRecipeRoot();
        try
        {
            FileSystemUtils.rmdir(recipeRoot);
        }
        catch (IOException e)
        {
            LOG.warning("Unable to remove recipe root directory: " + e.getMessage(), e);
        }

        try
        {
            FileSystemUtils.rmdir(recipeProcessorPaths.getTempDir());
        }
        catch (IOException e)
        {
            LOG.warning("Unable to remove recipe temp directory: " + e.getMessage(), e);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
