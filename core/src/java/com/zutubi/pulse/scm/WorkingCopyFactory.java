package com.zutubi.pulse.scm;

import com.zutubi.pulse.scm.svn.SvnWorkingCopy;

import java.io.File;

/**
 * Not entirely a classic factory: this factory knows how to create a working
 * copy object based on a directory on the file system.  For example, if it
 * finds a .svn subdirectory, it presumes a Subversion working copy.
 */
public class WorkingCopyFactory
{
    public static WorkingCopy create(File base)
    {
        // Is this a subversion working copy?
        File test = new File(base, ".svn");
        if(test.isDirectory())
        {
            return new SvnWorkingCopy(base);
        }

        return null;
    }
}
