package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asMap;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a single artifact in an artifacts tree.
 */
public class ArtifactFileObject extends AbstractPulseFileObject implements ArtifactProvider, AddressableFileObject
{
    public static final String CLASS_PREFIX = "artifact-";
    public static final String CLASS_SUFFIX_LINK = "link";
    public static final String CLASS_SUFFIX_UNKNOWN = "unknown";
    public static final String CLASS_SUFFIX_BROKEN = "broken";
    public static final String CLASS_SUFFIX_HTML = "html";
    public static final String CLASS_SUFFIX_IN_PROGRESS = "inprogress";

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

    public AbstractPulseFileObject createFile(final FileName fileName)
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
            return NO_CHILDREN;
        }

        File base = getArtifactBase();
        if(base.isDirectory())
        {
            return UriParser.encode(base.list());
        }
        return NO_CHILDREN;
    }

    @Override
    public String getIconCls()
    {
        String suffix = null;
        if (isLinkArtifact)
        {
            suffix = CLASS_SUFFIX_LINK;
        }
        else if (getArtifactBase() == null)
        {
            suffix = CLASS_SUFFIX_UNKNOWN;
        }
        else if(!getArtifactBase().isDirectory())
        {
            suffix = CLASS_SUFFIX_BROKEN;
        }
        else if (isHtmlArtifact)
        {
            suffix = CLASS_SUFFIX_HTML;
        }

        if (suffix == null)
        {
            // Just show the normal folder/file icons.
            return null;
        }
        else
        {
            return CLASS_PREFIX + suffix;
        }
    }

    public String getDisplayName()
    {
        return getArtifact().getName();
    }

    public List<FileAction> getActions()
    {
        List<FileAction> actions = new ArrayList<FileAction>(3);
        if (isLinkArtifact)
        {
            actions.add(new FileAction(FileAction.TYPE_LINK, getUrlPath()));
        }
        else if (getArtifactBase().isDirectory())
        {
            if (isHtmlArtifact)
            {
                actions.add(new FileAction(FileAction.TYPE_VIEW, getUrlPath()));
            }
            actions.add(new FileAction(FileAction.TYPE_ARCHIVE, "/zip.action?path=" + getName().getURI()));
        }
        return actions;
    }

    public boolean isLocal()
    {
        return !isLinkArtifact;
    }

    public boolean isExplicit()
    {
        return getArtifact().isExplicit();
    }

    public boolean isFeatured()
    {
        return getArtifact().isFeatured();
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

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        return asMap(CollectionUtils.<String, Object>asPair("actions", getActions()));
    }
}
