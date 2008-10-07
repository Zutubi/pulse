package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.scm.api.Change;

/**
 * An entity wrapped around the {@link com.zutubi.pulse.core.scm.api.Change}
 * data type from the SCM API.
 */
public class PersistentFileChange extends Entity
{
    private String filename;
    private String revisionString;
    private String actionName;
    private boolean directory;

    protected PersistentFileChange()
    {

    }

    public PersistentFileChange(Change data)
    {
        this(data.getFilename(), data.getRevisionString(), data.getAction(), data.isDirectory());
    }

    public PersistentFileChange(String filename, String revisionString, Change.Action action, boolean directory)
    {
        this.filename = filename;
        this.actionName = action.name();
        this.revisionString = revisionString;
        this.directory = directory;
    }

    public Change asChange()
    {
        return new Change(filename, revisionString, Change.Action.valueOf(actionName), directory);
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean directory)
    {
        this.directory = directory;
    }
}
