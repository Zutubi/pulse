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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.project.BuildStageModel;

/**
 * JSON model for executing build stages.  Extends the regular stage model with
 * information about the build the stage is within, as these models are shown
 * alone (not as part of a full build).
 */
public class ExecutingStageModel extends BuildStageModel
{
    private long number;
    private boolean personal;
    private String project;
    private String owner;
    
    public ExecutingStageModel(BuildResult buildResult, RecipeResultNode stageResult, Urls urls)
    {
        super(buildResult, stageResult, urls, false);

        number = buildResult.getNumber();
        personal = buildResult.isPersonal();
        project = buildResult.getProject().getName();
        owner = buildResult.getOwner().getName();
    }

    public long getNumber()
    {
        return number;
    }

    public boolean isPersonal()
    {
        return personal;
    }

    public String getProject()
    {
        return project;
    }

    public String getOwner()
    {
        return owner;
    }
    
    public String getBuildLink()
    {
        return getLink();
    }
}
