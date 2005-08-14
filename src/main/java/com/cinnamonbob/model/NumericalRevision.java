package com.cinnamonbob.model;

/**
 * A subversion revision, which is just a revision number.
 * 
 * @author jsankey
 */
public class NumericalRevision extends Revision
{
    private static final String REVISION_NUMBER = "number";

    protected NumericalRevision()
    {
    }

    public NumericalRevision(long revisionNumber)
    {
        setRevisionNumber(revisionNumber);
    }

    private void setRevisionNumber(long revisionNumber)
    {
        getProperties().setProperty(REVISION_NUMBER, Long.toString(revisionNumber));
    }

    public long getRevisionNumber()
    {
        String revisionNumber = getProperties().getProperty(REVISION_NUMBER);
        try
        {
            return Long.parseLong(revisionNumber);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    public String toString()
    {
        return getProperties().getProperty(REVISION_NUMBER);
    }
}
