package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

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
     * @param filter the filter that determines which paths are accepted and which are filtered.
     *
     * @return the filtered list of changelists.
     */
    public static List<Changelist> filter(List<Changelist> changelists, PathFilter filter)
    {
        List<Changelist> filteredChangelists = new LinkedList<Changelist>();

        Iterator<Changelist> changelist = changelists.iterator();
        while (changelist.hasNext())
        {
            List<FileChange> changes = new LinkedList<FileChange>();
            Changelist ch = changelist.next();
            for (FileChange c : ch.getChanges())
            {
                if (filter == null || filter.accept(c.getPath()))
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
