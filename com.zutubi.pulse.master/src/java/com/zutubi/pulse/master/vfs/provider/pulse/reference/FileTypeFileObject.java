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

package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.core.marshal.doc.ElementDocs;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a type of tove file (e.g. a pulse file) in the tove file doc
 * tree.
 */
public class FileTypeFileObject extends AbstractReferenceFileObject
{
    private String root;
    private String displayName;
    private ToveFileDocManager toveFileDocManager;

    public FileTypeFileObject(final FileName name, final AbstractFileSystem fs, String displayName)
    {
        super(name, fs);
        this.root = name.getBaseName();
        this.displayName = displayName;
    }

    protected String[] getDynamicChildren()
    {
        return new String[]{root};
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        ElementDocs child = toveFileDocManager.lookupRoot(fileName.getBaseName());
        if (child == null)
        {
            return null;
        }

        return objectFactory.buildBean(ElementFileObject.class, fileName, getFileSystem(), child);
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String getIconCls()
    {
        return "reference-filetype-icon";
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
