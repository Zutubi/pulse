package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class ArtifactCommandFileObject extends PulseFileObject implements CommandResultNode
{
    private final long commandResultId;

    private String displayName;

    public ArtifactCommandFileObject(final FileName name, final long commandResultId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.commandResultId = commandResultId;
    }

    public PulseFileObject createFile(final FileName fileName) throws Exception
    {
        long artifactId = Long.parseLong(fileName.getBaseName());

        StoredArtifact artifact = pfs.getBuildManager().getArtifact(artifactId);

        return new StoredArtifactFileObject(fileName, artifactId, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected void doAttach() throws Exception
    {
        CommandResult commandResult = getCommandResult();
        displayName = String.format("command :: %s", commandResult.getCommandName());
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> children = new LinkedList<String>();

        for (StoredArtifact artifact : getCommandResult().getArtifacts())
        {
            children.add(Long.toString(artifact.getId()));
        }
        return children.toArray(new String[children.size()]);
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public CommandResult getCommandResult()
    {
        return pfs.getBuildManager().getCommandResult(getCommandResultId());
    }

    public long getCommandResultId()
    {
        return commandResultId;
    }
}
