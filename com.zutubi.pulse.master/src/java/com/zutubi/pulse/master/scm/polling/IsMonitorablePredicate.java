package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Predicate;

public class IsMonitorablePredicate implements Predicate<Project>
{
    public boolean satisfied(Project project)
    {
        ScmConfiguration scm = project.getConfig().getScm();
        if (scm == null || !(scm instanceof Pollable))
        {
            return false;
        }

        return ((Pollable) scm).isMonitor();
    }
}
