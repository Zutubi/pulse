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

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.xwork.actions.vfs.DirectoryComparator;
import com.zutubi.util.WebUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the artifacts in a single build result, keyed by stage.
 */
public class BuildArtifactsFileObject extends AbstractPulseFileObject implements AddressableFileObject, ComparatorProvider
{
    public BuildArtifactsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        // Should refer to a stage.  First, try to interpret the name as a
        // recipe result ID.  If that doesn't work, try looking at it as a
        // stage name.
        try
        {
            long recipeId = Long.parseLong(fileName.getBaseName());
            if(buildManager.getRecipeResult(recipeId) != null)
            {
                return objectFactory.buildBean(ArtifactStageFileObject.class, fileName, recipeId, pfs);
            }
        }
        catch (NumberFormatException e)
        {
            // Maybe a stage name, continue the research Smithers.
        }
        
        return objectFactory.buildBean(NamedStageFileObject.class, fileName, fileName.getBaseName(), pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected void doAttach() throws Exception
    {
        childrenChanged(null, null);
    }

    protected String[] doListChildren() throws Exception
    {
        // return a list of the recipes.
        List<String> results = new LinkedList<String>();

        BuildResult result = getBuildResult();
        if (result != null)
        {
            for (final RecipeResultNode child : getBuildResult().getStages())
            {
                results.add(Long.toString(child.getResult().getId()));
            }
            return results.toArray(new String[results.size()]);
        }

        return NO_CHILDREN;
    }

    public Comparator<FileObject> getComparator()
    {
        DirectoryComparator comp = new DirectoryComparator();
        comp.setUseDisplayName(false);
        return comp;
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider != null)
        {
            return provider.getBuildResult();
        }
        return null;
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath() throws FileSystemException
    {
        return "/browse/projects/" + WebUtils.uriComponentEncode(getBuildResult().getProject().getName()) + "/" + getBuildResult().getNumber() + "/artifacts";
    }
}
