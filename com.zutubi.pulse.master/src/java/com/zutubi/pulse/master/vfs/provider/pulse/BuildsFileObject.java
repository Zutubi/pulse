package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * <class comment/>
 */
public class BuildsFileObject extends AbstractPulseFileObject
{
    public BuildsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        long buildId = convertToBuildId(fileName.getBaseName());
        if (buildId != -1)
        {
            return objectFactory.buildBean(BuildFileObject.class,
                    new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                    new Object[]{fileName, buildId, pfs}
            );
        }
        // need an error place holder.
        return null;
    }

    private long convertToBuildId(String str) throws FileSystemException
    {
        long id = Long.parseLong(str);

        // else, is it a build number?
        ProjectProvider provider = getAncestor(ProjectProvider.class);
        if (provider != null)
        {
            Project project = provider.getProject();

            BuildResult result = buildManager.getByProjectAndNumber(project, id);
            if (result != null)
            {
                return result.getId();
            }
        }

        BuildResult result = buildManager.getBuildResult(id);
        if (result != null)
        {
            return id;
        }
        return -1;
    }

    protected FileType doGetType() throws Exception
    {
        // this object does allow traversal.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not support listing of the builds.
        return new String[0];
    }
}