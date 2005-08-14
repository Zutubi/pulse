package com.cinnamonbob.model;

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
    private String revision;
    private Action action;

    protected Change()
    {

    }

    public Change(String filename, String revision, Action action)
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
     * @return the revision number of the change, which will be a file revision
     *         if applicable or the change list revision otherwise
     */
    public String getRevision()
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
