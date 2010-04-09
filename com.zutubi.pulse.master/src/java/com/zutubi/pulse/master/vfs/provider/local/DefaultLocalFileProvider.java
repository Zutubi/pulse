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
