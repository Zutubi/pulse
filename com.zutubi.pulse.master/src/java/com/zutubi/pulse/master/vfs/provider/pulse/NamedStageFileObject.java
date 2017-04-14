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

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a single stage in a single build.
 */
public class NamedStageFileObject extends AbstractPulseFileObject implements RecipeResultProvider
{
    private final String stageName;

    public NamedStageFileObject(final FileName name, final String stageName, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.stageName = stageName;
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        return objectFactory.buildBean(NamedCommandFileObject.class, fileName, name, pfs);
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

    public RecipeResult getRecipeResult() throws FileSystemException
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            throw new FileSystemException("No build result available.");
        }
        
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
}
