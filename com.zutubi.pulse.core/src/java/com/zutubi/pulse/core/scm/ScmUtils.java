package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Helpers shared amongst SCM implementations.
 */
public class ScmUtils
{
    public static File[] specToFiles(File base, String... spec) throws ScmException
    {
        if(spec.length == 0)
        {
            return null;
        }
        
        File[] result = new File[spec.length];
        for(int i = 0; i < spec.length; i++)
        {
            result[i] = new File(base, spec[i]);
            if(!result[i].exists())
            {
                throw new ScmException("File '" + spec[i] + "' does not exist");
            }
        }

        return result;
    }

    public static List<Changelist> filterExcludes(List<Changelist> changelists, FilepathFilter filter)
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
