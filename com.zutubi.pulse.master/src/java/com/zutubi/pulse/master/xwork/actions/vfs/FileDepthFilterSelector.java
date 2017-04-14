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

package com.zutubi.pulse.master.xwork.actions.vfs;/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.vfs.FileDepthSelector;
import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.util.Messages;

/**
 * A {@link org.apache.commons.vfs.FileSelector} that selects all children of the given fileObject.<br />
 * This is to mimic the {@link java.io.FileFilter} interface
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 */
public class FileDepthFilterSelector extends FileDepthSelector
{
    private FileFilter fileFilter;

    public FileDepthFilterSelector(int maxDepth)
    {
        super(1, maxDepth);
    }

    public FileDepthFilterSelector(FileFilter fileFilter, int maxDepth)
    {
        this(maxDepth);
        this.fileFilter = fileFilter;
    }

    /**
     * Determines if a file or folder should be selected.
     */
    public boolean includeFile(final FileSelectInfo fileInfo)
    {
        if (!super.includeFile(fileInfo))
        {
            return false;
        }

        return accept(fileInfo);
    }

    public boolean accept(final FileSelectInfo fileInfo)
    {
        if (fileFilter != null)
        {
            return fileFilter.accept(fileInfo);
        }

        throw new IllegalArgumentException(Messages.getString("vfs.selectors/filefilter.missing.error"));
    }
}
