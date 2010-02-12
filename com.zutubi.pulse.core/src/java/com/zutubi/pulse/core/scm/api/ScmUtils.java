package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * Helpers shared amongst SCM implementations.
 */
public class ScmUtils
{
    /**
     * An inplace filter that removes changes from the changelist whose paths are not
     * accepted by the filter.
     *
     * @param changelists the changelists whose changes are analysed
     * @param predicate the filter that determines which paths are accepted and which are filtered.
     *
     * @return the filtered list of changelists.
     */
    public static List<Changelist> filter(List<Changelist> changelists, Predicate<String> predicate)
    {
        List<Changelist> filteredChangelists = new LinkedList<Changelist>();

        for (Changelist ch : changelists)
        {
            List<FileChange> changes = new LinkedList<FileChange>();
            for (FileChange c : ch.getChanges())
            {
                if (predicate == null || predicate.satisfied(c.getPath()))
                {
                    changes.add(c);
                }
            }
            if (changes.size() > 0)
            {
                Changelist filtered = new Changelist(ch.getRevision(), ch.getTime(), ch.getAuthor(), ch.getComment(), changes);
                filteredChangelists.add(filtered);
            }
        }
        return filteredChangelists;
    }
}
