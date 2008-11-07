package com.zutubi.pulse.core.scm.api;

/**
 * Represents an atomic revision in an SCM server.  Different SCMs denote
 * revisions in different ways.  For example, in Subversion revisions are
 * simply numbers, but in Git a unique hash is used.  Each implementation is
 * responsible for being able to serialise revisions to a simple string form.
 */
public class Revision
{
    /**
     * Represents whatever the latest revision is at the time.
     */
    public static final Revision HEAD = null;

    private String revisionString;

    // Used by hessian
    Revision()
    {
    }
    
    /**
     * Creates a new revision from the serialised form.
     *
     * @param revisionString a serialised form of the revision, the content of
     *                       which is implementation-dependent
     * @throws NullPointerException if revisionString is null
     */
    public Revision(String revisionString)
    {
        if (revisionString == null)
        {
            throw new NullPointerException("Revision string may not be null");
        }

        this.revisionString = revisionString;
    }

    /**
     * Creates a new revision based in the given revision number.  This is a
     * convenience constructor for those scms that use numeric revisions.
     *
     * @param revision a numeric revision string.
     */
    public Revision(long revision)
    {
        this.revisionString = Long.toString(revision);
    }

    /**
     * @return a serialised version of the revision
     */
    public String getRevisionString()
    {
        return revisionString;
    }

    /**
     * Convenience method to return the previous revision when this revision is
     * numerical.
     *
     * @return the previous numerical revision, or null if this is revision 1
     * @throws NumberFormatException if our revision string cannot be parsed as
     *         a lon 
     */
    public Revision getPreviousNumericalRevision()
    {
        long number = Long.valueOf(revisionString);
        if(number > 0)
        {
            return new Revision(String.valueOf(number - 1));
        }
        return null;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Revision revision = (Revision) o;
        return revisionString.equals(revision.revisionString);
    }

    public int hashCode()
    {
        return revisionString.hashCode();
    }

    public String toString()
    {
        return revisionString;
    }
}
