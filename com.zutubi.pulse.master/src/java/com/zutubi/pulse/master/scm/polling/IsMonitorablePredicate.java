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
}
