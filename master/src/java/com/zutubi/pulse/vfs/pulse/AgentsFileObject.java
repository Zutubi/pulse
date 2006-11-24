package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

/**
 * <class comment/>
 */
public class AgentsFileObject extends PulseFileObject
{
    public AgentsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public PulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }
}
