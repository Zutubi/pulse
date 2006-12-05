package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;

import com.zutubi.pulse.model.BuildResult;

/**
 * <class comment/>
 */
public class BuildFileObject extends AbstractPulseFileObject implements BuildResultProvider, AddressableFileObject
{
    private final long buildId;

    public BuildFileObject(final FileName name, final long buildId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.buildId = buildId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String name = fileName.getBaseName();
        if (name.equals("wc"))
        {
            return objectFactory.buildBean(WorkingCopyContextFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        if (name.equals("artifacts"))
        {
            return objectFactory.buildBean(ArtifactsContextFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
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

    public String getUrlPath()
    {
        return "/viewBuild.action?id=" + buildId;
    }

    //---( this node is backed by a build result object. )---
    
    public BuildResult getBuildResult()
    {
        return buildManager.getBuildResult(getBuildResultId());
    }

    public long getBuildResultId()
    {
        return buildId;
    }
}
