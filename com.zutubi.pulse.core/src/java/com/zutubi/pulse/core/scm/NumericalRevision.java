package com.zutubi.pulse.core.scm;

/**
 * A subversion revision, which is just a revision number.
 *
 * @author jsankey
 */
public class NumericalRevision
{
    private String revisionString;

    protected NumericalRevision()
    {
    }

    public NumericalRevision(long revisionNumber)
    {
        setRevisionString(Long.toString(revisionNumber));
    }

    public NumericalRevision(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public long getRevisionNumber()
    {
        try
        {
            return Long.parseLong(getRevisionString());
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
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

        NumericalRevision that = (NumericalRevision) o;
        return !(revisionString != null ? !revisionString.equals(that.revisionString) : that.revisionString != null);
    }

    public int hashCode()
    {
        return (revisionString != null ? revisionString.hashCode() : 0);
    }
}
