package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

/**
 * <class comment/>
 */
public class RootFileObject extends AbstractPulseFileObject
{
    public RootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"projects", "agents"};
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String path = fileName.getPath();
        if (path.endsWith("projects"))
        {
            return objectFactory.buildBean(ProjectsFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        if (path.endsWith("agents"))
        {
            return objectFactory.buildBean(AgentsFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }
}
