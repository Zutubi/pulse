package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The LocalArtifact File Object represents a StoredArtifact instance.
 */
public class ArtifactFileObject extends AbstractPulseFileObject implements ArtifactProvider, AddressableFileObject
{
    private final long artifactId;

    private boolean isHtmlArtifact;
    private boolean isLinkArtifact;
    private File artifactBase;
    private StoredArtifact artifact;
    private CommandResult commandResult;

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
        if (artifact != null)
        {
            isHtmlArtifact = artifact.hasIndexFile() && !artifact.isSingleFile();
            isLinkArtifact = artifact.isLink();
        }
    }

    private File getArtifactBase()
    {
        if(artifactBase == null)
        {
            CommandResult result = getCommandResult();
            StoredArtifact artifact = getArtifact();
            if (result == null || artifact == null)
            {
                // this artifact does not exist, we should not be talking to this object.  However, since we
                // are, we should ensure that we behave in a sensible maner.
                return null;
            }

            File outputDir = result.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
            artifactBase = new File(outputDir, artifact.getName());
        }

        return artifactBase;
    }

    protected FileType doGetType() throws Exception
    {
        if (isHtmlArtifact || isLinkArtifact || !getArtifactBase().isDirectory())
        {
            return FileType.FILE;
        }
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        if (isHtmlArtifact || isLinkArtifact)
        {
            return new String[0];
        }

        File base = getArtifactBase();
        if(base.isDirectory())
        {
            return UriParser.encode(base.list());
        }
        return new String[0];
    }


    public String getFileType() throws FileSystemException
    {
        if(isLinkArtifact)
        {
            return FileTypeConstants.LINK;
        }
        else if (getArtifactBase() == null)
        {
            return FileTypeConstants.UNKNOWN;
        }
        else if(!getArtifactBase().isDirectory())
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

    public List<FileAction> getActions()
    {
        List<FileAction> actions = new ArrayList<FileAction>(3);
        if(isLinkArtifact)
        {
            actions.add(new FileAction(FileAction.TYPE_LINK, getUrlPath()));
        }
        else if (getArtifactBase().isDirectory())
        {
            if (isHtmlArtifact)
            {
                actions.add(new FileAction(FileAction.TYPE_DOWNLOAD, getUrlPath()));
            }
            actions.add(new FileAction(FileAction.TYPE_ARCHIVE, "/zip.action?path=" + getName().getURI()));
        }
        return actions;
    }

    public boolean isLocal()
    {
        return !isLinkArtifact;
    }

    public String getUrlPath()
    {
        if (isHtmlArtifact)
        {
            return "/file/artifacts/" + artifactId + "/" + getArtifact().findIndexFile();
        }
        else if(isLinkArtifact)
        {
            return getArtifact().getUrl();
        }
        
        return null;
    }

    public StoredArtifact getArtifact()
    {
        if(artifact == null)
        {
            artifact = buildManager.getArtifact(artifactId);
        }
        return artifact;
    }

    public long getArtifactId()
    {
        return artifactId;
    }

    public CommandResult getCommandResult()
    {
        if (commandResult == null)
        {
            commandResult = buildManager.getCommandResultByArtifact(artifactId);
        }
        return commandResult;
    }

    public long getCommandResultId()
    {
        return getCommandResult().getId();
    }
}
