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

package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.Project;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;

/**
 * Provides a set of pre-defined expressions that can be applied to queries that target build results.
 */
public class HibernateBuildResultExpressions
{
    public static Criterion projectEq(Project project)
    {
        return Restrictions.eq("project", project);
    }

    public static Criterion projectIn(Collection<Project> projects)
    {
        return Restrictions.in("project", projects);
    }

    public static Criterion statesIn(ResultState... states)
    {
        String[] stateNames = new String[states.length];
        for (int i = 0; i < states.length; i++)
        {
            stateNames[i] = states[i].toString();
        }
        return Restrictions.in("stateName", stateNames);
    }

    public static Criterion buildResultCompleted()
    {
        return statesIn(ResultState.getCompletedStates());
    }

    public static Criterion isPersonalBuild(boolean b)
    {
        return (b) ? Restrictions.isNotNull("user") : Restrictions.isNull("user");
    }

    public static Order orderByDescEndDate()
    {
        return Order.desc("stamps.endTime");
    }

    public static Order orderByDescId()
    {
        return Order.desc("id");
    }
}
