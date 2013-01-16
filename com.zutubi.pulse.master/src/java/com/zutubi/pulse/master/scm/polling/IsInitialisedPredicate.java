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
        // CIB-2987: there's usually no point polling a project that is due to reinitialise (and
        // indeed it may be counter-productive).  Reinitialisation is commonly due to non-contiguous
        // change history.
        return project.isInitialised() && project.getState() != Project.State.INITIALISE_ON_IDLE;
    }
}
