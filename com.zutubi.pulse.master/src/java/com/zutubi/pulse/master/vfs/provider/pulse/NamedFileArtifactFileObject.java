package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;

/**
 * <class comment/>
 */
public class NamedFileArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    private File file;

    public NamedFileArtifactFileObject(final FileName name, final File file, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.file = file;
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        File newFile = new File(file, name);
        
        return objectFactory.buildBean(NamedFileArtifactFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, newFile, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        if (file.isFile())
        {
            return FileType.FILE;
        }
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath() throws FileSystemException
    {
        if (file.isDirectory())
        {
            throw new FileSystemException("Displaying of artifact directories is not supported.");
        }

        String basePath = getArtifactBase().getAbsolutePath();
        String artifactPath = file.getAbsolutePath();
        String path = artifactPath.substring(basePath.length() + 1);
        path = path.replace('\\', '/');

        return "/file/artifacts/" + getArtifact().getId() + "/" + path;
    }

    private File getArtifactBase() throws FileSystemException
    {
        CommandResult result = getCommandResult();
        StoredArtifact artifact = getArtifact();

        File outputDir = result.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
        return new File(outputDir, artifact.getName());
    }

    protected StoredArtifact getArtifact() throws FileSystemException
    {
        ArtifactProvider provider = getAncestor(ArtifactProvider.class);
        return provider.getArtifact();
    }

    protected CommandResult getCommandResult() throws FileSystemException
    {
        CommandResultProvider provider = getAncestor(CommandResultProvider.class);
        return provider.getCommandResult();
    }
}
