package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;
import java.io.File;
import java.util.List;
import java.util.Arrays;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.CommandResult;

/**
 * <class comment/>
 */
public class StoredArtifactFileObject extends AbstractPulseFileObject implements StoredArtifactNode
{
    private final long artifactId;

    private File base;

    private String displayName;

    public StoredArtifactFileObject(final FileName name, final long artifactId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.artifactId = artifactId;
    }

    protected void doAttach() throws Exception
    {
        CommandResultNode node = (CommandResultNode) getAncestor(CommandResultNode.class);
        CommandResult commandResult = node.getCommandResult();

        StoredArtifact artifact = getArtifact();

        displayName = artifact.getName();

        File outputDir = commandResult.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
        base = new File(outputDir, artifact.getName());
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File child = new File(base, fileName.getBaseName());
        return objectFactory.buildBean(ArtifactFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, child, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return base.list();
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public List<String> getActions()
    {
        return Arrays.asList("archive");
    }

    public String getDisplayName()
    {
        if (displayName != null)
        {
            return displayName;
        }
        return super.getDisplayName();
    }

    public File toFile()
    {
        return base;
    }

    public StoredArtifact getArtifact()
    {
        return buildManager.getArtifact(getArtifactId());
    }

    public long getArtifactId()
    {
        return artifactId;
    }
}
