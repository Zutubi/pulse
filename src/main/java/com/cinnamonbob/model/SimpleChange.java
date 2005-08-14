package com.cinnamonbob.model;

import com.cinnamonbob.scm.Change;

/**
 * A trivial implementation of the Change interface.
 * 
 * @author jsankey
 */
public class SimpleChange extends Entity implements Change
{
    private String filename;
    private String revision;
    private Action action;

    protected SimpleChange()
    {

    }

    public SimpleChange(String filename, String revision, Action action)
    {
        this.filename = filename;
        this.revision = revision;
        this.action = action;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getRevision()
    {
        return revision;
    }

    public Action getAction()
    {
        return action;
    }

    private void setFilename(String filename)
    {
        this.filename = filename;
    }

    private void setRevision(String revision)
    {
        this.revision = revision;
    }

    private String getActionName()
    {
        return action.toString();
    }

    private void setActionName(String action)
    {
        this.action = Action.valueOf(action);
    }
}
