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
 * Represents the stages within a build.
 */
public class BuildStagesFileObject extends AbstractPulseFileObject
{
    public BuildStagesFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        return objectFactory.buildBean(NamedStageFileObject.class, fileName, fileName.getBaseName(), pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider != null)
        {
            BuildResult result = provider.getBuildResult();
            if (result != null)
            {
                List<RecipeResultNode> nodes = result.getStages();
                return transform(nodes, new Function<RecipeResultNode, String>()
                {
                    public String apply(RecipeResultNode recipeResultNode)
                    {
                        return recipeResultNode.getStageName();
                    }
                }).toArray(new String[nodes.size()]);
            }
        }

        return NO_CHILDREN;
    }
}