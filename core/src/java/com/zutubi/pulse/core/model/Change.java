package com.zutubi.pulse.core.model;

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
        ADD
                {
                    public String getPrettyString()
                    {
                        return "added";
                    }
                },
        /**
         * The file was branched somewhere.
         */
        BRANCH
                {
                    public String getPrettyString()
                    {
                        return "branched";
                    }
                },
        /**
         * The file was deleted from source control.
         */
        DELETE
                {
                    public String getPrettyString()
                    {
                        return "deleted";
                    }
                },
        /**
         * The file contents were edited.
         */
        EDIT
                {
                public String getPrettyString()
                    {
                        return "edited";
                    }
                },
        /**
         * Perforce-specific: the file was integrated.
         */
        INTEGRATE
                {
                public String getPrettyString()
                    {
                        return "integrated";
                    }
                },
        /**
         * The file was merged with another.
         */
        MERGE
                {
                    public String getPrettyString()
                    {
                        return "merged";
                    }
                }
        ,
        /**
         * The file was moved.
         */
        MOVE
                {
                    public String getPrettyString()
                    {
                        return "moved";
                    }
                }
        ,
        /**
         * An unrecognised action.
         */
        UNKNOWN
                {
                    public String getPrettyString()
                    {
                        return "unknown";
                    }
                };

        public abstract String getPrettyString();
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

    /**
     * Used by hibernate
     *
     * @param filename string
     */
    private void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Used by hibernate
     *
     * @param revision string
     */
    private void setRevision(FileRevision revision)
    {
        this.revision = revision;
    }

    /**
     * Used by hibernate
     *
     * @return action name
     */
    private String getActionName()
    {
        return action.toString();
    }

    /**
     * Used by hibernate.
     *
     * @param action string
     */
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
