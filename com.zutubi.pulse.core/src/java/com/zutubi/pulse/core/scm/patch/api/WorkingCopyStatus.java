package com.zutubi.pulse.core.scm.patch.api;


import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to convey the local status of a working copy.  All outstanding changes are recorded as
 * {@link FileStatus} objects.
 */
public class WorkingCopyStatus
{
    private File base;
    private List<FileStatus> fileStatuses = new LinkedList<FileStatus>();
    private String specDescription;

    /**
     * Create a new working copy status for files based at the given directory.  This directory is
     * typically the root of a working copy.  Usually it is identical to the base directory
     * specified by {@link com.zutubi.pulse.core.scm.api.WorkingCopyContext#getBase()}, but it may
     * be different for some implementations (e.g. Perforce, where the base will be the client root
     * regardless of the working directory of the pulse command).
     *
     * @param base base directory under which file statuses are found
     */
    public WorkingCopyStatus(File base)
    {
        this.base = base;
    }

    /**
     * @return the base directory for the status, usually the root of a working
     *         copy
     */
    public File getBase()
    {
        return base;
    }

    /**
     * @return true if all recorded file statuses are in a consistent state (i.e. there are no
     *         missing files, outstanding conflicts etc)
     */
    public boolean inConsistentState()
    {
        for(FileStatus fs: getFileStatuses())
        {
            if(!fs.getState().isConsistent())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if at least one file status has been recorded: i.e. there are local changes
     *         under the base directory
     */
    public boolean hasStatuses()
    {
        return fileStatuses.size() > 0;
    }

    /**
     * @return file status records for all local changes found under the base directory
     */
    public List<FileStatus> getFileStatuses()
    {
        return Collections.unmodifiableList(fileStatuses);
    }

    /**
     * Records a new file status record for a local change.
     *
     * @param status the new record to add
     */
    public void addFileStatus(FileStatus status)
    {
        fileStatuses.add(status);
    }

    /**
     * Finds an existing file status record by path, if one exists.
     *
     * @param path the path to find the status for
     * @return the file status for the given path, or null if no such status has been recorded
     */
    public FileStatus getFileStatus(final String path)
    {
        return CollectionUtils.find(fileStatuses, new Predicate<FileStatus>()
        {
            public boolean satisfied(FileStatus fileStatus)
            {
                return fileStatus.getPath().equals(path);
            }
        });
    }

    /**
     * Gives a human-readable description of the spec specified by the user to
     * indicate the status to find.
     *
     * @return a description of the spec, may be null
     */
    public String getSpecDescription()
    {
        return specDescription;
    }

    /**
     * Sets the description of the spec used in generating this status.
     *
     * @param specDescription a human-readable spec description
     */
    public void setSpecDescription(String specDescription)
    {
        this.specDescription = specDescription;
    }
}
