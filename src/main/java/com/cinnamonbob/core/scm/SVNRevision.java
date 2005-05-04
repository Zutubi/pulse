package com.cinnamonbob.core.scm;

/**
 * A subversion revision, which is just a revision number.
 * 
 * @author jsankey
 */
public class SVNRevision implements Revision
{
    private long revisionNumber;
    
    
    public SVNRevision(long revisionNumber)
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
