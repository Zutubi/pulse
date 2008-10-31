package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;

import java.util.Iterator;
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
     * @param filter the filter that determines which paths are accepted and which are filtered.
     *
     * @return the filtered list of changelists.
     */
    public static List<Changelist> filter(List<Changelist> changelists, PathFilter filter)
    {
        Iterator<Changelist> changelist = changelists.iterator();
        while (changelist.hasNext())
        {
            Changelist ch = changelist.next();
            Iterator<FileChange> i = ch.getChanges().iterator();
            while (i.hasNext())
            {
                FileChange c = i.next();
                if (filter != null && !filter.accept(c.getPath()))
                {
                    i.remove();
                }
            }
            if (ch.getChanges().size() == 0)
            {
                changelist.remove();
            }
        }
        return changelists;
    }

}
