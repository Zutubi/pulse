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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper base class for implementing file objects that just map from names
 * to a type of child file object.
 */
public abstract class StaticMappingFileObject extends AbstractPulseFileObject
{
    protected static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();

    public StaticMappingFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> rootPaths = nodesDefinitions.keySet();
        return rootPaths.toArray(new String[rootPaths.size()]);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class<? extends AbstractPulseFileObject> clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }
}
