package com.cinnamonbob.scm;

/**
 * A subversion revision, which is just a revision number.
 * 
 * @author jsankey
 */
public class NumericalRevision implements Revision
{
    private long revisionNumber;
    
    
    public NumericalRevision(long revisionNumber)
    {
        this.revisionNumber = revisionNumber;
    }
    
    
    public long getRevisionNumber()
    {
        return revisionNumber;
    }
    
    
    public String toString()
    {
        return Long.toString(revisionNumber);
    }
}
