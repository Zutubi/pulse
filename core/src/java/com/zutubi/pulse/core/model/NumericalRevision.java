package com.zutubi.pulse.core.model;

import java.util.Date;

/**
 * A subversion revision, which is just a revision number.
 *
 * @author jsankey
 */
public class NumericalRevision extends Revision
{
    protected NumericalRevision()
    {
    }

    public NumericalRevision(long revisionNumber)
    {
        setRevisionString(Long.toString(revisionNumber));
    }

    public NumericalRevision(String author, String comment, Date date, long revisionNumber)
    {
        super(author, comment, date);
        setRevisionString(Long.toString(revisionNumber));
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

    public Revision copy()
    {
        NumericalRevision copy = new NumericalRevision();
        copyCommon(copy);
        return copy;
    }

    public boolean isHead()
    {
        return false;
    }
}
