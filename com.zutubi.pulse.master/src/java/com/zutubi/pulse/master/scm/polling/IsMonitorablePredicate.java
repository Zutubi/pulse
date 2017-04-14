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

package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;

/**
 * This predicate is satisfied by any project that should be monitored.
 *
 * This means the projects' scm is pollable and the isMonitor flag is true.
 *
 * @see Pollable#isMonitor()  
 */
public class IsMonitorablePredicate implements Predicate<Project>
{
    public boolean apply(Project project)
    {
        ScmConfiguration scm = project.getConfig().getScm();
        if (scm == null || !(scm instanceof Pollable))
        {
            return false;
        }

        return ((Pollable) scm).isMonitor();
    }

    @Override
    public String toString()
    {
        return "IsMonitorable";
    }
}
