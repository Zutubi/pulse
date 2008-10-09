package com.zutubi.pulse.core.scm.api;

/**
 * Represents a change to a single file as part of a larger Changelist.  For
 * example, the file may have been edited, added or deleted.
 *
 * @see com.zutubi.pulse.core.scm.api.Changelist
 */
public class FileChange
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
        },
        /**
         * The file was moved.
         */
        MOVE
        {
            public String getPrettyString()
            {
                return "moved";
            }
        },
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

    private String path;
    private Action action;
    private boolean directory;
    private String revisionString;

    /**
     * Creates a new file change for a regular file with the given details.
     *
     * @param path           path of the changed file: the format of which is
     *                       SCM-specific
     * @param revisionString freeform file revision string.  For SCMs that
     *                       track individual file revisions, this string
     *                       should contain that revision, otherwise it may be
     *                       identical to the changelist's revision string.
     * @param action         the action that was performed on the file
     */
    public FileChange(String path, String revisionString, Action action)
    {
        this(path, revisionString, action, false);
    }

    /**
     * Creates a new file change for a file with the given details, indicating
     * if the file is a directory.
     *
     * @param path           path of the changed file: the format of which is
     *                       SCM-specific
     * @param revisionString freeform file revision string.  For SCMs that
     *                       track individual file revisions, this string
     *                       should contain that revision, otherwise it may be
     *                       identical to the changelist's revision string.
     * @param action         the action that was performed on the file
     * @param directory      set to true if the path denotes a directory
     */
    public FileChange(String path, String revisionString, Action action, boolean directory)
    {
        this.path = path;
        this.action = action;
        this.directory = directory;
        this.revisionString = revisionString;
    }

    /**
     * @return the path of the file that was changed as an SCM-specific
     *         repository path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return a freeform string containing the file revision, which may be the
     *         same as the changelist revision for SCMs that do not track
     *         separate revisions for individual files
     */
    public String getRevisionString()
    {
        return revisionString;
    }

    /**
     * @return the action that was performed on the file
     */
    public Action getAction()
    {
        return action;
    }

    /**
     * @return true iff the changed path denotes a directory
     */
    public boolean isDirectory()
    {
        return directory;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        FileChange change = (FileChange) o;
        if (directory != change.directory)
        {
            return false;
        }
        if (action != change.action)
        {
            return false;
        }
        if (path != null ? !path.equals(change.path) : change.path != null)
        {
            return false;
        }

        return !(revisionString != null ? !revisionString.equals(change.revisionString) : change.revisionString != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (directory ? 1 : 0);
        result = 31 * result + (revisionString != null ? revisionString.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(path);
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
