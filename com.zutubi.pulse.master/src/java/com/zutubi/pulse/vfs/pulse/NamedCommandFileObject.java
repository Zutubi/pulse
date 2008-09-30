package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;

/**
 * <class comment/>
 */
public class NamedCommandFileObject extends AbstractPulseFileObject implements CommandResultProvider
{
    private final String commandName;

    public NamedCommandFileObject(final FileName name, final String commandName, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.commandName = commandName;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String name = fileName.getBaseName();
        return objectFactory.buildBean(NamedArtifactFileObject.class,
                new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                new Object[]{fileName, name, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        // support navigation but not listing for now.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not support listing for now.
        return new String[0];
    }

    public CommandResult getCommandResult() throws FileSystemException
    {
        RecipeResultProvider provider = (RecipeResultProvider) getAncestor(RecipeResultProvider.class);
        if (provider == null)
        {
            throw new FileSystemException("Missing build stage context.");
        }

        RecipeResult result = provider.getRecipeResult();
        if (result == null)
        {
            throw new FileSystemException("No build stage results available.");            
        }
        
        // need to execute this within the context of a transaction.
        CommandResult commandResult = buildManager.getCommandResult(result.getId(), commandName);
        if (commandResult == null)
        {
            throw new FileSystemException(String.format("No command result available for '%s'", commandName));
        }
        return commandResult;
    }

    public long getCommandResultId() throws FileSystemException
    {
        return getCommandResult().getId();
    }
}
