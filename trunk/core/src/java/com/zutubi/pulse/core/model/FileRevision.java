package com.zutubi.pulse.core.model;

/**
 * A representation of a file revision in an SCM.  In some SCMs, this is the
 * same as the repository revision (e.g. Subversion).  In others, files have
 * their own revision numbers (e.g. CVS, Perforce).
 */
public abstract class FileRevision extends Entity
{
    /**
     * @return the previous revision on the same branch, or null if this is
     * the first revision on the branch
     */
    public abstract FileRevision getPrevious();

    /**
     * @return a string representation of this revision
     */
    public abstract String getRevisionString();
}
