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

import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.FileObjectUtils;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.*;

/**
 * Implementation note: This class had to be copied and modified because it did not allow for extension in
 * the maner required.  Primarily, we could not replace the default 'file' instance which is private.
 *
 * The modified code is marked to help with future upgrades.
 */
public class LocalFile extends AbstractFileObject implements FileObject
{
    private File file;

    private LocalFileSystem fs;

    /**
     * Creates a non-root file.
     */
    protected LocalFile(final LocalFileSystem fileSystem,
                        final FileName name) throws FileSystemException
    {
        super(name, fileSystem);
        this.fs = fileSystem;
    }

    /**
     * Returns the local file that this file object represents.
     */
    protected File getLocalFile()
    {
        return file;
    }

    /**
     * Attaches this file object to its file resource.
     */
    // MODIFIED
    protected void doAttach() throws Exception
    {
        if (file == null)
        {
            file = getFileInfo();
        }
    }

    // MODIFIED
    protected FileType doGetType() throws Exception
    {
        if (file.isFile())
        {
            return FileType.FILE;
        }
        else if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        return FileType.IMAGINARY;
    }
    
    // MODIFIED
    protected String[] doListChildren() throws Exception
    {
        String path = getName().getPathDecoded();
        if (fs.isWindows() && path.equals(FileName.ROOT_PATH))
        {
            return fs.getRoots();
        }
        return UriParser.encode(FileSystemUtils.list(file));
    }
    
    protected File getFileInfo() throws FileSystemException
    {
        String path = getName().getPathDecoded();
        if (isRoot())
        {
            // need to munge it a little, must add the trailing slash if we want
            // the correct file.
            return new File(path + "/");
        }
        return new File(path);
    }

    protected boolean isRoot() throws FileSystemException
    {
        String path = getName().getPathDecoded();
        return (fs.isWindows() && fs.isRoot(path));
    }

    /**
     * Deletes this file, and all children.
     */
    protected void doDelete()
        throws Exception
    {
        if (!file.delete())
        {
            throw new FileSystemException("vfs.provider.local/delete-file.error", file);
        }
    }

    /**
     * rename this file
     */
    protected void doRename(final FileObject newfile) throws Exception
    {
        LocalFile newLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(newfile);

        try
        {
            FileSystemUtils.robustRename(file, newLocalFile.getLocalFile());
        }
        catch (IOException e)
        {
            throw new FileSystemException("vfs.provider.local/rename-file.error",
                new String[]{file.toString(), newfile.toString()}, e);
        }
    }

    /**
     * Creates this folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        if (!file.mkdirs())
        {
            throw new FileSystemException("vfs.provider.local/create-folder.error", file);
        }
    }

    /**
     * Determines if this file can be written to.
     */
    protected boolean doIsWriteable() throws FileSystemException
    {
        return file.canWrite();
    }

    /**
     * Determines if this file is hidden.
     */
    // MODIFIED
    protected boolean doIsHidden() throws FileSystemException
    {
        if (isRoot())
        {
            return false;
        }
        return file.isHidden();
    }

    /**
     * Determines if this file can be read.
     */
    protected boolean doIsReadable() throws FileSystemException
    {
        return file.canRead();
    }

    /**
     * Gets the last modified time of this file.
     */
    protected long doGetLastModifiedTime() throws FileSystemException
    {
        return file.lastModified();
    }

    /**
     * Sets the last modified time of this file.
     */
    protected void doSetLastModifiedTime(final long modtime)
        throws FileSystemException
    {
        file.setLastModified(modtime);
    }

    /**
     * Creates an input stream to read the content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        return new FileInputStream(file);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend)
        throws Exception
    {
        return new FileOutputStream(file.getPath(), bAppend);
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        return file.length();
    }

    // MODIFIED
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
//        return new LocalFileRandomAccessContent(file, mode);
          return null;
    }

    protected boolean doIsSameFile(FileObject destFile) throws FileSystemException
    {
        if (!FileObjectUtils.isInstanceOf(destFile, LocalFile.class))
        {
            return false;
        }

        LocalFile destLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(destFile);
        if (!exists() || !destLocalFile.exists())
        {
            return false;
        }

        try
        {
            return file.getCanonicalPath().equals(destLocalFile.file.getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }

    }
}