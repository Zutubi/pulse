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

import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.ResultCustomFields;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Map;
import java.util.Stack;

/**
 * Helper base class for build tabs that show build status information.
 * This includes the summary, details and test tabs.
 */
public class BuildStatusActionBase extends CommandActionBase
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 100;

    private static final Logger LOG = Logger.getLogger(BuildStatusActionBase.class);

    /**
     * Insanity to work around lack of locals in velocity.
     */
    private Stack<String> pathStack = new Stack<String>();

    protected MasterConfigurationManager configurationManager;

    public boolean haveRecipeResultNode()
    {
        return getRecipeResultNode() != null;
    }

    public boolean haveCommandResult()
    {
        return getCommandResult() != null;
    }

    public BuildResult getResult()
    {
        return getBuildResult();
    }

    public Map<String, String> getCustomFields(Result result)
    {
        ResultCustomFields customFields = new ResultCustomFields(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()));
        return customFields.load();
    }

    public int getFailureLimit()
    {
        int limit = DEFAULT_FAILURE_LIMIT;
        String property = System.getProperty(FAILURE_LIMIT_PROPERTY);
        if(property != null)
        {
            try
            {
                limit = Integer.parseInt(property);
            }
            catch(NumberFormatException e)
            {
                LOG.warning(e);
            }
        }

        return limit;
    }

    public String execute()
    {
        final BuildResult result = getRequiredBuildResult();

        // Initialise detail down to the command level (optional)
        getCommandResult();

        File dataDir = configurationManager.getDataDirectory();
        result.loadFeatures(dataDir);
        result.loadFailedTestResults(dataDir, getFailureLimit());

        return SUCCESS;
    }

    public String pushSuite(PersistentTestSuiteResult suite)
    {
        if(pathStack.empty())
        {
            return pathStack.push(uriComponentEncode(suite.getName()));
        }
        else
        {
            return pathStack.push(pathStack.peek() + "/" + uriComponentEncode(suite.getName()));
        }
    }

    public void popSuite()
    {
        pathStack.pop();
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}