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

package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

import java.text.Collator;
import java.util.Comparator;

/**
 * A comparator for sorting file objects with directories before files and
 * alphabetical sorting by name within each of those categories.
 */
public class DirectoryComparator implements Comparator<FileObject>
{
    private static final Logger LOG = Logger.getLogger(DirectoryComparator.class);

    private Collator c = Collator.getInstance();
    private boolean useDisplayName = true;

    public int compare(FileObject o1, FileObject o2)
    {
        // folders first.
        try
        {
            FileType t1 = o1.getType();
            FileType t2 = o2.getType();

            if ((t1 == FileType.FOLDER || t1 == FileType.IMAGINARY) &&
                    (t2 != FileType.FOLDER && t2 != FileType.IMAGINARY))
            {
                return -1;
            }
            if ((t2 == FileType.FOLDER || t2 == FileType.IMAGINARY) &&
                    (t1 != FileType.FOLDER && t1 != FileType.IMAGINARY))
            {
                return 1;
            }

            // then sort alphabetically
            if(useDisplayName && o1 instanceof AbstractPulseFileObject)
            {
                return c.compare(((AbstractPulseFileObject)o1).getDisplayName(), ((AbstractPulseFileObject)o2).getDisplayName());
            }

            return c.compare(o1.getName().getPath(), o2.getName().getPath());
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return 0;
        }
    }

    public void setUseDisplayName(boolean useDisplayName)
    {
        this.useDisplayName = useDisplayName;
    }
}
