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

import com.google.common.base.Function;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;

/**
 * Represents the details of a single stage in a single build - tailored to
 * provide information for the details tab.
 */
public class StageDetailsFileObject extends AbstractResultDetailsFileObject implements RecipeResultProvider
{
    public StageDetailsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        return objectFactory.buildBean(CommandDetailsFileObject.class, fileName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<CommandResult> commandResults = getRecipeResult().getCommandResults();
        return transform(commandResults, new Function<CommandResult, String>()
        {
            public String apply(CommandResult commandResult)
            {
                return commandResult.getCommandName();
            }
        }).toArray(new String[commandResults.size()]);
    }

    public RecipeResult getRecipeResult() throws FileSystemException
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            throw new FileSystemException("No build result available.");
        }

        String stageName = getName().getBaseName();
        RecipeResultNode node = result.findResultNode(stageName);
        if (node == null)
        {
            throw new FileSystemException(String.format("No stage by the name '%s' is available.", stageName));
        }
        
        RecipeResult recipeResult = node.getResult();
        if (recipeResult == null)
        {
            throw new FileSystemException("No recipe result is available.");
        }
        
        return recipeResult;
    }

    public long getRecipeResultId() throws FileSystemException
    {
        return getRecipeResult().getId();
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider == null)
        {
            throw new FileSystemException("Missing build result context.");
        }
        
        return provider.getBuildResult();
    }

    @Override
    protected RecipeResult getResult() throws FileSystemException
    {
        return getRecipeResult();
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> attributes = super.getExtraAttributes();
        attributes.put("stageName", getName().getBaseName());
        return attributes;
    }
}