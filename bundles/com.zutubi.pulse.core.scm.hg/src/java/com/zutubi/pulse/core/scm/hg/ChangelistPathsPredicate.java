package com.zutubi.pulse.core.scm.hg;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.FilterPathsPredicate;

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

    public boolean apply(Changelist changelist)
    {
        for (FileChange change: changelist.getChanges())
        {
            if (pathPredicate.apply(change.getPath()))
            {
                return true;
            }
        }

        return false;
    }
}
