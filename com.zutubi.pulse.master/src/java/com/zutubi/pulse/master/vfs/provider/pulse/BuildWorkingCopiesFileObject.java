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
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

import static com.google.common.collect.Collections2.transform;

/**
 * Represents the working copies for a build, keyed by stage.
 */
public class BuildWorkingCopiesFileObject extends AbstractPulseFileObject
{
    public BuildWorkingCopiesFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        long recipeId = Long.parseLong(fileName.getBaseName());

        return objectFactory.buildBean(WorkingCopyStageFileObject.class, fileName, recipeId, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        // can traverse this node.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            throw new FileSystemException("buildResult.not.available");
        }

        List<RecipeResultNode> stages = result.getStages();
        return transform(stages, new Function<RecipeResultNode, String>()
        {
            public String apply(RecipeResultNode node)
            {
                return Long.toString(node.getResult().getId());
            }
        }).toArray(new String[stages.size()]);
    }

    private BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }
}
