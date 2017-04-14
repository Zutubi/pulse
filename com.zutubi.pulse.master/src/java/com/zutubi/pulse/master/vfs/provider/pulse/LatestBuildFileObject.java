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
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents the latest build result for a scope, which may be global, or a
 * single project.
 */
public class LatestBuildFileObject extends AbstractBuildFileObject
{
    private static final Logger LOG = Logger.getLogger(LatestBuildFileObject.class);

    public LatestBuildFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public BuildResult getBuildResult()
    {
        try
        {
            ProjectProvider provider = getAncestor(ProjectProvider.class, true);
            if (provider != null)
            {
                Project project = provider.getProject();
                return buildManager.getLatestBuildResult(project, true);
            }
            else
            {
                return buildManager.getLatestBuildResult();
            }
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return null;
        }
    }
}
