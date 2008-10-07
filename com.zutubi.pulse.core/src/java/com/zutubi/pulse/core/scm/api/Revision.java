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

    /**
     * Creates a new revision from the serialised form.
     *
     * @param revisionString a serialised form of the revision, the content of
     *                       which is implemented-dependent
     */
    public Revision(String revisionString)
    {
        this.revisionString = revisionString;
    }

    /**
     * @return a serialised version of the revision
     */
    public String getRevisionString()
    {
        return revisionString;
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
