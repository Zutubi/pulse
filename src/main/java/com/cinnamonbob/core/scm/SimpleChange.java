package com.cinnamonbob.core.scm;

/**
 * A trivial implementation of the Change interface.
 * 
 * @author jsankey
 */
public class SimpleChange implements Change
{
    private String filename;
    private Revision revision;
    private Action action;
    
    
    public SimpleChange(String filename, Revision revision, Action action)
    {
        this.filename = filename;
        this.revision = revision;
        this.action = action;
    }
    
    public String getFilename()
    {
        return filename;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public Action getAction()
    {
        return action;
    }
}
