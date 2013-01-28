package com.zutubi.pulse.core.scm.patch;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A data object holding meta-information about the files in a patch archive.
 * Using a separate type helps simplify serialisation.
 *
 * @see PatchArchive
 */
public class PatchMetadata
{
    private List<FileStatus> fileStatuses;

    /**
     * Creates metadata to go in a patch archive.
     *
     * @param fileStatuses information about changed files in this patch
     */
    public PatchMetadata(List<FileStatus> fileStatuses)
    {
        this.fileStatuses = new LinkedList<FileStatus>(fileStatuses);
    }

    /**
     * @return status information for all files changed by the patch
     */
    public List<FileStatus> getFileStatuses()
    {
        return Collections.unmodifiableList(fileStatuses);
    }

    /**
     * Looks up the status for a particular path.
     *
     * @param path path of the status to find, relative to the base of the
     *             patch
     * @return the status for the path, or null if no status has been recorded
     *         for the path
     */
    public FileStatus getFileStatus(final String path)
    {
        return find(fileStatuses, new Predicate<FileStatus>()
        {
            public boolean apply(FileStatus fileStatus)
            {
                return fileStatus.getPath().equals(path);
            }
        }, null);
    }
}
