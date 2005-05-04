package com.cinnamonbob.core.scm;

import java.util.Date;
import java.util.List;

/**
 * Represents a changelist: a set of file changes in an SCM.  This may be
 * emulated for SCMs that do not support changelists.
 * 
 * @author jsankey
 */
public interface Changelist
{
    /**
     * @return the revision number for the changelist, or null if the
     *         changelist is emulated.
     */
    public Revision getRevision();
    
    /**
     * @return the time at which the change occured (for emulated changelists,
     *         this will be the earliest time)
     */
    public Date getDate();
    
    /**
     * @return the name of the user that made the change
     */
    public String getUser();
    
    /**
     * @return the comment or message for the change
     */
    public String getComment();
    
    /**
     * @return the list of file changes in this list
     */
    public List<Change> getChanges();
}
