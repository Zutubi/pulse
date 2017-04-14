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

import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.util.Os;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a modified version of the default LocalFileSystem implementation, that, in conjunction with
 * the LocalFile object, allows for natural browsing of the root drives on a windows machine. That is,
 * the default resolution of "file:///" will generate an exception on Windows because it does not define
 * a drive.  With this file system installed, that same resolution will return a FileObject whose children
 * represent the the available drives.
 *
 */
public class LocalFileSystem extends org.apache.commons.vfs.provider.local.LocalFileSystem
{
    private static final Logger LOG = Logger.getLogger(LocalFileSystem.class);

    private List<FileName> rootNames;

    public LocalFileSystem(final FileName rootName, final String rootFile, final FileSystemOptions opts)
    {
        super(rootName, rootFile, opts);
    }

    public void init() throws FileSystemException
    {
        super.init();

        loadFileSystemRoots();
    }

    protected FileObject createFile(final FileName name) throws FileSystemException
    {
        return new LocalFile(this, name);
    }

    private void loadFileSystemRoots() throws FileSystemException
    {
        rootNames = new LinkedList<FileName>();
        for (File root : File.listRoots())
        {
            // munge these roots.
            rootNames.add(getFileSystemManager().resolveName(getRootName(), root.getAbsolutePath()));
        }
    }

    protected String[] getRoots()
    {
        List<String> roots = new LinkedList<String>();
        for (FileName rootName : rootNames)
        {
            roots.add(rootName.getPath());
        }
        return roots.toArray(new String[roots.size()]);
    }

    protected boolean isRoot(String name)
    {
        try
        {
            FileName rootName = getFileSystemManager().resolveName(getRootName(), name);
            if (rootNames.contains(rootName))
            {
                return true;
            }
        }
        catch (FileSystemException e)
        {
            LOG.severe(e);
        }
        return false;
    }

    protected boolean isWindows()
    {
        return (Os.isFamily(Os.OS_FAMILY_WINDOWS));
    }
}
