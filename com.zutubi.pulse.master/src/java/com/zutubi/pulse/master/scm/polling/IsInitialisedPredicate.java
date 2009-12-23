package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Predicate;

/**
 * A predicate that is satisfied if and only if the project has been
 * initialised.
 *
 * @see com.zutubi.pulse.master.model.Project#isInitialised() 
 */
public class IsInitialisedPredicate implements Predicate<Project>
{
    public boolean satisfied(Project project)
    {
        return project.isInitialised();
    }
}
