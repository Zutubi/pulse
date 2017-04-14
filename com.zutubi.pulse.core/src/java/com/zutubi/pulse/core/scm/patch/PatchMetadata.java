/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
