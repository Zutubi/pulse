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

package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.Constants;

import java.util.LinkedList;
import java.util.List;

/**
 * A retain rule is used to exclude matching builds from cleanup rules.
 */
@SymbolicName("zutubi.retainConfig")
@Form(fieldOrder = {"name", "retain", "unit", "states", "statuses"})
public class RetainConfiguration extends AbstractCleanupConfiguration
{
    @Override
    public List<BuildResult> getMatchingResults(Project project, BuildResultDao dao)
    {
        ResultState[] allowedStates = resolveAllowedStates();
        String[] allowedStatuses = resolveAllowedStatuses();

        List<BuildResult> results = new LinkedList<BuildResult>();
        if(unit == CleanupUnit.BUILDS)
        {
            results.addAll(dao.queryBuilds(new Project[]{project}, allowedStates, allowedStatuses, 0, 0, 0, retain, true, false));
        }
        else if (unit == CleanupUnit.DAYS)
        {
            long startTime = System.currentTimeMillis() - retain * Constants.DAY;
            results.addAll(dao.queryBuilds(new Project[]{project}, allowedStates, allowedStatuses, startTime, 0, -1, -1, true, false));
        }

        return results;
    }

    @Override
    public String summarise()
    {
        return "retain " + summariseFilter() + " for up to " + getRetain() + " " + getUnit().toString().toLowerCase();
    }
}
