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

package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file object that can be used to insert an arbitrary text message into a
 * tree, with an arbitrary icon.
 */
public class TextMessageFileObject extends AbstractPulseFileObject
{
    private final String iconCls;

    public TextMessageFileObject(final FileName name, final String iconCls, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.iconCls = iconCls;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return NO_CHILDREN;
    }

    public String getIconCls()
    {
        return iconCls;
    }
}
