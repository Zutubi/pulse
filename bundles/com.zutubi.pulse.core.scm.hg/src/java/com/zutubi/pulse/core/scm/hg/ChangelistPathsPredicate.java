package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.FilterPathsPredicate;
import com.zutubi.util.Predicate;

import java.util.List;

/**
 * A predicate that only accepts changelists that have some non-filtered path.
 */
public class ChangelistPathsPredicate implements Predicate<Changelist>
{
    private FilterPathsPredicate pathPredicate;

    public ChangelistPathsPredicate(List<String> includedPaths, List<String> excludedPaths)
    {
        pathPredicate = new FilterPathsPredicate(includedPaths, excludedPaths);
    }

    public boolean satisfied(Changelist changelist)
    {
        for (FileChange change: changelist.getChanges())
        {
            if (pathPredicate.satisfied(change.getPath()))
            {
                return true;
            }
        }

        return false;
    }
}
