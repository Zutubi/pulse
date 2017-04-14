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
 * <class comment/>
 */
public class NamedArtifactsFileObject extends AbstractPulseFileObject
{
    public NamedArtifactsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String artifactName = fileName.getBaseName();
        return objectFactory.buildBean(NamedArtifactFileObject.class, fileName, artifactName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not list
        return NO_CHILDREN;
    }
}
