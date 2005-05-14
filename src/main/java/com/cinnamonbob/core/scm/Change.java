package com.cinnamonbob.core.scm;

/**
 * Represents a change to a single file in an SCM.
 * 
 * @author jsankey
 */
public interface Change
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
    
    /**
     * @return the name of the file that was changed as a repository path
     */
    String getFilename();
    
    /**
     * @return the revision number of the change, which will be a file revision
     *         if applicable or the change list revision otherwise
     */
    Revision getRevision();
    
    /**
     * @return the action performed on the file
     */
    Action getAction();
}
