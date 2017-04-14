/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

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

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        return objectFactory.buildBean(NamedArtifactFileObject.class, fileName, name, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        // support navigation but not listing for now.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not support listing for now.
        return NO_CHILDREN;
    }

    public CommandResult getCommandResult() throws FileSystemException
    {
        RecipeResultProvider provider = getAncestor(RecipeResultProvider.class);
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
