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
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Map;

/**
 * Represents the details of a single command - tailored for the details tab.
 */
public class CommandDetailsFileObject extends AbstractResultDetailsFileObject implements CommandResultProvider
{
    private static final Logger LOG = Logger.getLogger(CommandDetailsFileObject.class);
    
    public CommandDetailsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
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
        
        String commandName = getName().getBaseName();
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

    @Override
    protected CommandResult getResult() throws FileSystemException
    {
        return getCommandResult();
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> attributes = super.getExtraAttributes();
        attributes.put("commandName", getName().getBaseName());
        return attributes;
    }
}