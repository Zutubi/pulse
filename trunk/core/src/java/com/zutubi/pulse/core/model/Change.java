package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.model.FileRevision;

/**
 * A trivial implementation of the Change interface.
 *
 * @author jsankey
 */
public class Change extends Entity
{
    /**
     * Types of actions that can be performed on an SCM file.
     */
    public enum Action
    {
        /**
         * The file was added to source control.
         */
        ADD,
        /**
         * The file was branched somewhere.
         */
        BRANCH,
        /**
         * The file was deleted from source control.
         */
        DELETE,
        /**
         * The file contents were edited.
         */
        EDIT,
        /**
         * Perforce-specific: the file was integrated.
         */
        INTEGRATE,
        /**
         * The file was merged with another.
         */
        MERGE,
        /**
         * The file was moved.
         */
        MOVE,
        /**
         * An unrecognised action.
         */
        UNKNOWN
    }

    private String filename;
    private FileRevision revision;
    private Action action;

    protected Change()
    {

    }

    public Change(String filename, FileRevision revision, Action action)
    {
        this.filename = filename;
        this.revision = revision;
        this.action = action;
    }

    /**
     * @return the name of the file that was changed as a repository path
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @return the revision number of the change
     */
    public FileRevision getRevision()
    {
        return revision;
    }

    /**
     * @return the action performed on the file
     */
    public Action getAction()
    {
        return action;
    }

    private void setFilename(String filename)
    {
        this.filename = filename;
    }

    private void setRevision(FileRevision revision)
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

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(filename);
        if (revision != null)
        {
            buffer.append("#").append(revision);
        }
        if (action != null)
        {
            buffer.append("-").append(action.toString());
        }
        return buffer.toString();
    }
}
