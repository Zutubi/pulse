package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;

/**
 * <class comment/>
 */
public class NamedArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject, ArtifactProvider
{
    private final String artifactName;

    public NamedArtifactFileObject(final FileName name, final String artifactName, final AbstractFileSystem fs)
    {
        super(name, fs);        

        this.artifactName = artifactName;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        String name = fileName.getBaseName();
        File newFile = new File(getArtifactBase(), name);

        return objectFactory.buildBean(NamedFileArtifactFileObject.class, fileName, newFile, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return NO_CHILDREN;
    }

    public boolean isLocal() throws FileSystemException
    {
        return !getArtifact().isLink();
    }

    public String getUrlPath() throws FileSystemException
    {
        // do we have a command result in the context?
        StoredArtifact artifact = getArtifact();
        if (artifact == null)
        {
            throw new FileSystemException(String.format("There is no artifact by name '%s' available.", artifactName));
        }

        // is html artifact.
        if(artifact.isLink())
        {
            return artifact.getUrl();
        }
        else if (artifact.hasIndexFile() && !artifact.isSingleFile())
        {
            return "/file/artifacts/" + artifact.getId() + "/" + artifact.findIndexFile();
        }

        return "";
    }

    private File getArtifactBase() throws FileSystemException
    {
        CommandResult result = getCommandResult();
        StoredArtifact artifact = getArtifact();

        File outputDir = result.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
        return new File(outputDir, artifact.getName());
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider == null)
        {
            throw new FileSystemException("Missing build result context");
        }
        return provider.getBuildResult();
    }

    protected CommandResult getCommandResult() throws FileSystemException
    {
        CommandResultProvider provider = getAncestor(CommandResultProvider.class);
        if (provider == null)
        {
            throw new FileSystemException("Missing command result context");
        }
        return provider.getCommandResult();
    }

    public StoredArtifact getArtifact() throws FileSystemException
    {
        CommandResult commandResult = getCommandResult();
        return buildManager.getCommandResultByArtifact(commandResult.getId(), artifactName);
    }

    public long getArtifactId() throws FileSystemException
    {
        return getArtifact().getId();
    }
}
