package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * The Artifact File Object represents a StoredArtifact instance.
 * 
 */
public class ArtifactFileObject extends AbstractPulseFileObject implements ArtifactProvider, AddressableFileObject
{
    private final long artifactId;

    private boolean isHtmlArtifact;
    private File artifactBase;

    public ArtifactFileObject(final FileName name, final long artifactId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.artifactId = artifactId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File base = getArtifactBase();
        File newBase = new File(base, fileName.getBaseName());

        return objectFactory.buildBean(FileArtifactFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, newBase, pfs}
        );
    }

    public File toFile()
    {
        return getArtifactBase();
    }

    protected void doAttach() throws Exception
    {
        StoredArtifact artifact = getArtifact();
        isHtmlArtifact = artifact.hasIndexFile() && !getArtifact().isSingleFile();
    }

    private File getArtifactBase()
    {
        if(artifactBase == null)
        {
            CommandResult result = getCommandResult();
            StoredArtifact artifact = getArtifact();

            File outputDir = result.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
            artifactBase = new File(outputDir, artifact.getName());
        }

        return artifactBase;
    }

    protected FileType doGetType() throws Exception
    {
        if (isHtmlArtifact || !getArtifactBase().isDirectory())
        {
            return FileType.FILE;
        }
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        if (isHtmlArtifact)
        {
            return new String[0];
        }

        File base = getArtifactBase();
        if(base.isDirectory())
        {
            return base.list();
        }
        return new String[0];
    }


    public String getFileType() throws FileSystemException
    {
        if(!getArtifactBase().isDirectory())
        {
            return FileTypeConstants.BROKEN;
        }
        else if (isHtmlArtifact)
        {
            return FileTypeConstants.HTML_REPORT;
        }

        return super.getFileType();
    }

    public String getDisplayName()
    {
        return getArtifact().getName();
    }

    public List<String> getActions()
    {
        List<String> actions = new LinkedList<String>();
        if (getArtifactBase().isDirectory())
        {
            if (isHtmlArtifact)
            {
                actions.add("download");
            }
            actions.add("archive");
        }
        return actions;
    }

    public String getUrlPath()
    {
        if (isHtmlArtifact)
        {
            return "/file/artifacts/" + artifactId + "/" + getArtifact().findIndexFile();
        }
        return null;
    }

    public StoredArtifact getArtifact()
    {
        return buildManager.getArtifact(artifactId);
    }

    public long getArtifactId()
    {
        return artifactId;
    }

    public CommandResult getCommandResult()
    {
        return buildManager.getCommandResultByArtifact(artifactId);
    }

    public long getCommandResultId()
    {
        return getCommandResult().getId();
    }
}
