package com.zutubi.pulse.master.vfs.provider.agent;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;

import java.io.InputStream;

/**
 * <class comment/>
 */
public class AgentFileObject extends AbstractFileObject
{
    private static final Logger LOG = Logger.getLogger(AgentFileObject.class);

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
        String path = getName().getPathDecoded();
        if (fs.isWindows() && path.equals(FileName.ROOT_PATH))
        {
            return fs.getRoots();
        }
        FileInfo info = getFileInfo();
        return UriParser.encode(info.list());
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
        String path = getName().getPathDecoded();
        try
        {
            if (fs.isWindows() && fs.isRoot(path))
            {
                // need to munge it a little, must add the trailing slash if we want
                // the correct file.
                return fs.getProxy().getFileInfo(fs.getToken(), path + "/");
            }
            return fs.getProxy().getFileInfo(fs.getToken(), path);
        }
        catch (HessianRuntimeException e)
        {
            LOG.warning(e);
            throw new FileSystemException(e);
        }
    }
}
