package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

import com.zutubi.pulse.model.BuildResult;

/**
 * <class comment/>
 */
public class BuildFileObject extends AbstractPulseFileObject implements BuildResultNode
{
    private final long buildId;

    public BuildFileObject(final FileName name, final long buildId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.buildId = buildId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String base = fileName.getBaseName();
        if (base.endsWith("wc"))
        {
            return new WorkingCopyRootFileObject(fileName, pfs);
        }
        if (base.endsWith("artifacts"))
        {
            return new ArtifactsRootFileObject(fileName, pfs);
        }
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"wc", "artifacts"};
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    //---( this node is backed by a build result object. )---
    
    public BuildResult getBuildResult()
    {
        return pfs.getBuildManager().getBuildResult(getBuildResultId());
    }

    public long getBuildResultId()
    {
        return buildId;
    }
}
