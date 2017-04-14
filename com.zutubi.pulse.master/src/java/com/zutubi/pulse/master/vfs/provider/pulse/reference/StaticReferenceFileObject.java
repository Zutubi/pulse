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

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 */
public class StaticReferenceFileObject extends AbstractReferenceFileObject
{
    public StaticReferenceFileObject(FileName fileName, AbstractFileSystem fileSystem)
    {
        super(fileName, fileSystem);
    }

    @Override
    protected String[] getDynamicChildren() throws FileSystemException
    {
        return new String[0];
    }

    @Override
    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        return null;
    }

    @Override
    public String getIconCls()
    {
        return "reference-static-icon";
    }

    @Override
    public String getDisplayName()
    {
        return getName().getBaseName().replace('-', ' ');
    }
}
