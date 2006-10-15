package com.zutubi.pulse.vfs.agent;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;

import java.io.InputStream;

import com.zutubi.pulse.filesystem.FileInfo;

/**
 * <class comment/>
 */
public class AgentFileObject extends AbstractFileObject
{
    private AgentFileSystem fs;

    public AgentFileObject(final FileName name, final AgentFileSystem fs)
    {
        super(name, fs);

        this.fs = fs;
    }

    protected FileType doGetType() throws Exception
    {
        FileInfo file = getFileInfo();
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

    protected String[] doListChildren() throws Exception
    {
        String path = getName().getPath();
        if (path.equals(FileName.ROOT_PATH))
        {
            return fs.getRoots();
        }
        return UriParser.encode(getFileInfo().list());
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    protected FileInfo getFileInfo() throws FileSystemException
    {
        String path = getName().getPath();
        if (fs.isRoot(path))
        {
            // need to munge it a little, must add the trailing slash if we want
            // the correct file.
            return fs.getProxy().getFileInfo(fs.getToken(), path + "/");
        }
        return fs.getProxy().getFileInfo(fs.getToken(), path);
    }
}
