package com.zutubi.pulse.core.scm.api;

import com.zutubi.diff.PatchType;
import com.zutubi.util.FileSystemUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds status information about a file or directory in a working copy.  Used
 * as part of a report of the status of a working copy, and to help apply the
 * same changes to another working copy (known as a "target" working copy).
 *
 * @see com.zutubi.pulse.core.scm.api.WorkingCopyStatus
 */
public class FileStatus
{
    /**
     * If set, specifies how line endings should be translated for the file.
     * Possible values are described in the {@link EOLStyle} enum (the property
     * value is just the enum value converted to a string).
     *
     * @see EOLStyle
     */
    public static final String PROPERTY_EOL_STYLE = "eol";
    /**
     * If set, and the target system supports an executable permission, force
     * the executable permission for the file to be on if this property is
     * "true" and off for any other property value.  If not set, the executable
     * permission is left unchanged.
     */
    public static final String PROPERTY_EXECUTABLE = "executable";

    /**
     * Types of payloads that are carried for files in patches.
     */
    public enum PayloadType
    {
        /**
         * The payload is a unified diff representing changes to a text file.
         */
        DIFF,
        /**
         * No payload is present for the file.
         */
        NONE,
        /**
         * The payload is the full file content.
         */
        FULL
    }

    /**
     * Indicates the state that a path is in.  For example, if local edits have
     * been made, the state will be {@link #MODIFIED}.  If no changes have been
     * made, the state will be {@link #UNCHANGED}.
     * <p/>
     * Generally, implementations should provide states that are as specific as
     * possible, and matching the native terminology of the SCM as closely as
     * possible.  For example, when a file is to be added to version control,
     * it is always safe to report it as {@link #ADDED}, but is more accurate
     * to report it as {@link #RENAMED} if it is an existing file that has been
     * renamed.
     */
    public enum State
    {
        /**
         * The file has been marked to be added when the local changes are
         * committed.  That is, the file is new and yet to be added to version
         * control.
         * <p/>
         * Use {@link #BRANCHED} for files that have been added by branching an
         * existing file, if that can be determined.
         * <p/>
         * Use {@link #RENAMED} for files that have been added by renaming an
         * existing file, if that can be determined.
         * <p/>
         * Use {@link #REPLACED} for new files added in the same operation as
         * an existing file with the same path has been deleted.
         */
        ADDED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.FULL;
            }
        },
        /**
         * The file has been branched from an existing version controlled file,
         * and the new branched copy will be added when the local changes are
         * committed.
         */
        BRANCHED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.FULL;
            }
        },
        /**
         * The file has been marked for deletion in the local working copy.
         */
        DELETED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * The file exists in the local working copy, but is not version
         * controlled.
         */
        IGNORED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return false;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * The file exists and is version controlled, but its content or state
         * is incomplete.  The local working copy is not consistent until this
         * is resolved.
         */
        INCOMPLETE
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * Local changes exist in the file due to a merge which has not yet
         * been committed.
         * <p/>
         * Use {@link #UNRESOLVED} for outstanding merges that have unresolved
         * conflicts.
         * <p/>
         * Use {@link #MODIFIED} for normal outstanding edits.
         */
        MERGED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.DIFF;
            }
        },
        /**
         * A file is expected to exist at this path in the local working copy,
         * but no file was found.  The working copy is in an inconsistent
         * state.
         * <p/>
         * Use {@link #OBSTRUCTED} if a file of an unexpected type exists in
         * place of an expected file.
         */
        MISSING
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * Local edits have been made to the file content.  There may
         * additionally be edits to the file metadata.
         * <p/>
         * Use {@link #METADATA_MODIFIED} if only the metadata has been changed
         * and not the file content itself.
         */
        MODIFIED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.DIFF;
            }
        },
        /**
         * Local edits have been made to the file metadata, but the file
         * content itself is unchanged.
         * <p/>
         * Use {@link #MODIFIED} if changes are detected to both the file
         * content and the file metadata.
         */
        METADATA_MODIFIED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * A version-controlled file is meant to exist at this path, but a file
         * of a different or unexpected type was found instead.  The working
         * copy is in an inconsistent state.
         * <p/>
         * Use {@link #MISSING} for files that are expected at a path but do
         * not exist at all.
         */
        OBSTRUCTED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * A new file has been marked for add at this path, replacing an
         * existing file at the same path in a single operation (commit).  The
         * two files have the same path but the new file will not carry over
         * the history of the deleted file.
         */
        REPLACED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.FULL;
            }
        },
        /**
         * A new file created by renaming an existing file has been marked for
         * add at this path.  Usually the existing file will have state
         * {@link #DELETED} in this case.
         */
        RENAMED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.FULL;
            }
        },
        /**
         * The file at this path has unresolved conflicts from an outstanding
         * merge operation.  The working copy is in an incosistent state.
         */
        UNRESOLVED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * The type of file at this path is not supported by the SCM, or is not
         * supported by Pulse.  The working copy is in an inconsistent state.
         */
        UNSUPPORTED
        {
            public boolean isConsistent()
            {
                return false;
            }

            public boolean isInteresting()
            {
                return true;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        },
        /**
         * The path has no outstanding changes.
         */
        UNCHANGED
        {
            public boolean isConsistent()
            {
                return true;
            }

            public boolean isInteresting()
            {
                return false;
            }

            public PayloadType preferredPayloadType()
            {
                return PayloadType.NONE;
            }
        };

        /**
         * Indicates if the path is in an inconsistent state: that is, one
         * which prevents either a commit or replication of this state in
         * another working copy.  For example, a file with unresolved merge
         * conflicts cannot be committed.
         *
         * @return true if the file state is in an inconsistent state
         */
        public abstract boolean isConsistent();

        /**
         * Indicates if this file status information is necessary to replicate
         * the working copy state in another working copy.  If a local file is
         * changed in any way, the state is interesting.  Only unchanged files
         * or those not under version control are not interesting.
         *
         * @return true if the file status information would need to be applied
         *         to a target working copy to replicate the local working copy
         *         state
         */
        public abstract boolean isInteresting();

        /**
         * Indicates the preferred type of payload required to replicate the
         * local file state in a target working copy.  For example, if a file
         * has just been added or changed, the new file content will need
         * replication in the target working copy.  If a file is marked for
         * deletion, however, the data is not required as the target file can
         * just be deleted.  If a file has been changed, the preferred payload
         * is just the differences.
         * <p/>
         * The actual payload type may differ from the preferred type, for
         * example if a binary file is changed, a unified diff is not possible,
         * so the entire file content is used.
         *
         * @return true if the file data is required to replicate changes to
         *         the local path in a remote working copy
         */
        public abstract PayloadType preferredPayloadType();

        /**
         * Converts from a patch type to a state.
         *
         * @param type the patch type to convert
         * @return the corresponding state
         */
        public static State valueOf(PatchType type)
        {
            switch (type)
            {
                case ADD:
                case COPY:
                    return ADDED;
                case DELETE:
                    return DELETED;
                case EDIT:
                    return MODIFIED;
                case METADATA:
                    return METADATA_MODIFIED;
                case RENAME:
                    return RENAMED;
            }

            throw new IllegalArgumentException("Unrecognised patch type '" + type + "'");
        }
    }

    private String path;
    private FileStatus.State state;
    private boolean directory;
    private PayloadType payloadType;
    private String targetPath;
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Creates a new file status for the given file.
     *
     * @param path      path of the file in the working copy, relative to the
     *                  base directory of the working copy status, may not be
     *                  null
     * @param state     state of the file in the working copy, may not be null
     * @param directory true to indicate the path refers to a directory, false
     *                  for a regular file
     *
     * @throws NullPointerException is path or state is null
     */
    public FileStatus(String path, FileStatus.State state, boolean directory)
    {
        this(path, state, directory, null);
    }

    /**
     * Creates a new file status for the given file, with a custom target path.
     *
     * @param path       path of the file in the working copy, relative to the
     *                   base directory of the working copy status, may not be
     *                   null
     * @param state      state of the file in the working copy, may not be null
     * @param directory  true to indicate the path refers to a directory, false
     *                   for a regular file
     *@param targetPath path to which the status should be applied in a target
     *                   working copy, or null to use the same path as in the
     *                   local working copy
     *  @throws NullPointerException is path or state is null
     */
    public FileStatus(String path, FileStatus.State state, boolean directory, String targetPath)
    {
        if (path == null)
        {
            throw new NullPointerException("path must not be null");
        }

        if (state == null)
        {
            throw new NullPointerException("state must not be null");
        }

        this.path = FileSystemUtils.normaliseSeparators(path);
        this.state = state;
        this.directory = directory;
        this.payloadType = state.preferredPayloadType();

        if (targetPath == null)
        {
            this.targetPath = path;
        }
        else
        {
            this.targetPath = FileSystemUtils.normaliseSeparators(targetPath);
        }
    }

    /**
     * @return the path of the file this status refers to in the local working
     *         copy, relative to the base of the working copy status.
     *         Separators are normalised to forward slashes.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return the state of the file in the local working copy
     */
    public FileStatus.State getState()
    {
        return state;
    }

    /**
     * @return true if the path refers to a directory, false if it refers to a
     *         regular file
     */
    public boolean isDirectory()
    {
        return directory;
    }

    /**
     * @return the type of payload stored for this status, i.e. no content,
     *         the entire file or a diff from the base revision file
     */
    public PayloadType getPayloadType()
    {
        return payloadType;
    }

    /**
     * Sets the payload type.  Use to override the preferred payload type when
     * it cannot be used: for example if the preferred is a diff but the file
     * is binary.
     *
     * @param payloadType the new payload type
     *
     * @see #getPayloadType()
     */
    public void setPayloadType(PayloadType payloadType)
    {
        this.payloadType = payloadType;
    }

    /**
     * @return the path to which this status should apply in a target working
     *         copy, relative to the base of that working copy.  This is the
     *         same as the path in the local working copy by default.
     *         Separators are normalised to forward slashes.
     */
    public String getTargetPath()
    {
        return targetPath;
    }

    /**
     * @return a read-nly map of extra file properties, used to carry metadata
     *         about the file which may affect how this status is applied.  See
     *         the PROPERTY_* constants for available properties and their
     *         meanings.
     */
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Retrieves the value of a named property if it has been set.
     *
     * @param name the name of the property to retrieve the value of (see the
     *        PROPERTY_* constants)
     * @return the value of the property, or null if it has not been set
     *
     * @see #getProperties()
     */
    public String getProperty(String name)
    {
        return properties.get(name);
    }

    /**
     * Sets the value of a named property.  Any existing value is overwritten
     * and returned.
     *
     * @param name  the name of the property to set (see the PROPERTY_*
     *              constants)
     * @param value the new value for the property
     * @return the previous value of the property, or null if it had not
     *         previously been set
     * 
     * @see #getProperties()
     */
    public String setProperty(String name, String value)
    {
        return properties.put(name, value);
    }

    /**
     * @return true if the path referred to by this status is deemed to be in
     *         an interesting state: either the state itself is interesting or
     *         at least one property has been set.  Interesting statuses must
     *         be applied to a target working copy, non-interesting ones would
     *         have no effect.
     */
    public boolean isInteresting()
    {
        return state.isInteresting() || properties.size() > 0;
    }

    public String toString()
    {
        return String.format("%-12s %s", state.toString(), path);
    }
}
