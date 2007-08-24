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
    private Action action;

    private String revisionString;

    protected Change()
    {

    }

    public Change(String filename, String revisionString, Action action)
    {
        this.filename = filename;
        this.revisionString = revisionString;
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
     * @return the action performed on the file
     */
    public Action getAction()
    {
        return action;
    }

    public String getRevisionString()
    {
        return revisionString;
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

    /**
     * Used by hibernate.
     *
     * @param revisionString string
     */
    private void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(filename);
        if (revisionString != null)
        {
            buffer.append("#").append(revisionString);
        }
        if (action != null)
        {
            buffer.append("-").append(action.toString());
        }
        return buffer.toString();
    }
}
