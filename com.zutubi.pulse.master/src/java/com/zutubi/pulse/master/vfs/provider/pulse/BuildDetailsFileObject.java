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
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;

/**
 * Represents the details of a single build - tailored to provide the
 * information for the details tab.
 */
public class BuildDetailsFileObject extends AbstractResultDetailsFileObject implements ComparatorProvider
{
    private static final Logger LOG = Logger.getLogger(AbstractResultDetailsFileObject.class);

    public BuildDetailsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        return objectFactory.buildBean(StageDetailsFileObject.class, fileName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        BuildResult result = getResult();
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

        return NO_CHILDREN;
    }

    @Override
    protected BuildResult getResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider != null)
        {
            return provider.getBuildResult();
        }
        
        return null;
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> attributes = super.getExtraAttributes();
        try
        {
            BuildResult result = getResult();
            attributes.put("projectName", result.getProject().getName());
            attributes.put("buildVID", result.getNumber());
            attributes.put("personal", Boolean.toString(result.isPersonal()));
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
        }
        return attributes;
    }

    public Comparator<FileObject> getComparator()
    {
        return null;
    }
}