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

package com.zutubi.pulse.master.vfs.provider.local;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.local.GenericFileNameParser;
import org.apache.commons.vfs.provider.local.LocalFileName;
import org.apache.commons.vfs.provider.local.WindowsFileNameParser;

/**
 * An override of the vfs {@link DefaultLocalFileProvider} that uses a {@link GenericFileNameParser}
 * instead of a {@link WindowsFileNameParser} on windows file systems. 
 */
public class DefaultLocalFileProvider extends org.apache.commons.vfs.provider.local.DefaultLocalFileProvider
{
    public DefaultLocalFileProvider()
    {
        super();

        // ignore the WindowsFileNameParser used by default on windows boxes since it does not take
        // kindly to the paths that we now support in our custom file system. Namely, file:/// for listing
        // the roots.
        
        setFileNameParser(new GenericFileNameParser());
    }

    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // Create the file system
        final LocalFileName rootName = (LocalFileName) name;
        return new LocalFileSystem(rootName, rootName.getRootFile(), fileSystemOptions);
    }
}
