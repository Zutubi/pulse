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
        getProperties().put(REVISION_NUMBER, revisionNumber);
    }

    public long getRevisionNumber()
    {
        return (Long)getProperties().get(REVISION_NUMBER);
    }
}
