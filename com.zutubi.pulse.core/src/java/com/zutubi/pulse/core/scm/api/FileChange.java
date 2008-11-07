package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

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
            public String toString()
            {
                return "added";
            }
        },
        /**
         * The file was branched somewhere.
         */
        BRANCH
        {
            public String toString()
            {
                return "branched";
            }
        },
        /**
         * The file was deleted from source control.
         */
        DELETE
        {
            public String toString()
            {
                return "deleted";
            }
        },
        /**
         * The file contents were edited.
         */
        EDIT
        {
            public String toString()
            {
                return "edited";
            }
        },
        /**
         * Perforce-specific: the file was integrated.
         */
        INTEGRATE
        {
            public String toString()
            {
                return "integrated";
            }
        },
        /**
         * The file was merged with another.
         */
        MERGE
        {
            public String toString()
            {
                return "merged";
            }
        },
        /**
         * The file was moved.
         */
        MOVE
        {
            public String toString()
            {
                return "moved";
            }
        },
        /**
         * An unrecognised action.
         */
        UNKNOWN
        {
            public String toString()
            {
                return "unknown";
            }
        };

        /**
         * Inverse of toString: converts the given string back to an action.
         *
         * @param s string to convert
         * @return corresponding action
         * @throws IllegalArgumentException if the string matches no action
         */
        public static Action fromString(final String s)
        {
            Action found = CollectionUtils.find(Action.values(), new Predicate<Action>()
            {
                public boolean satisfied(Action action)
                {
                    return action.toString().equals(s);
                }
            });

            if (found == null)
            {
                throw new IllegalArgumentException("No action found matching '" + s + "'");
            }

            return found;
        }
    }

    private String path;
    private Action action;
    private boolean directory;
    private Revision revision;

    /**
     * Creates a new file change for a regular file with the given details.
     *
     * @param path     path of the changed file: the format of which is
     *                 SCM-specific
     * @param revision freeform file revision.  For SCMs that track individual
     *                 file revisions, this revision's string should contain
     *                 that revision, otherwise it may be identical to the
     *                 changelist's revision.
     * @param action   the action that was performed on the file
     */
    public FileChange(String path, Revision revision, Action action)
    {
        this(path, revision, action, false);
    }

    /**
     * Creates a new file change for a file with the given details, indicating
     * if the file is a directory.
     *
     * @param path     path of the changed file: the format of which is
     *                 SCM-specific
     * @param revision freeform file revision.  For SCMs that track individual
     *                 file revisions, this revision's string should contain
     *                 that revision, otherwise it may be identical to the
     *                 changelist's revision.
     * @param action         the action that was performed on the file
     * @param directory      set to true if the path denotes a directory
     */
    public FileChange(String path, Revision revision, Action action, boolean directory)
    {
        this.path = path;
        this.action = action;
        this.directory = directory;
        this.revision = revision;
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
     * @return a freeform file revision, which may be the same as the
     *         changelist revision for SCMs that do not track separate
     *         revisions for individual files
     */
    public Revision getRevision()
    {
        return revision;
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

        return !(revision != null ? !revision.equals(change.revision) : change.revision != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (directory ? 1 : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(path);
        if (revision != null)
        {
            buffer.append(" #").append(revision);
        }
        if (action != null)
        {
            buffer.append(" - ").append(action.toString());
        }
        return buffer.toString();
    }
}
