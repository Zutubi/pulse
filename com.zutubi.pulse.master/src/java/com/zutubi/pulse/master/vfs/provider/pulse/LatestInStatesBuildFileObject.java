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

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Abstract base for file objects that represent the latest build in some states for a scope, which
 * may be global or a single project.
 */
public abstract class LatestInStatesBuildFileObject extends AbstractBuildFileObject
{
    private static final Logger LOG = Logger.getLogger(LatestInStatesBuildFileObject.class);

    private ResultState[] inStates;

    public LatestInStatesBuildFileObject(final FileName name, final AbstractFileSystem fs, ResultState... inStates)
    {
        super(name, fs);
        this.inStates = inStates;
    }

    public BuildResult getBuildResult()
    {
        try
        {
            ProjectProvider provider = getAncestor(ProjectProvider.class, true);
            if (provider != null)
            {
                Project project = provider.getProject();
                return buildManager.getLatestBuildResult(project, true, inStates);
            }
            else
            {
                return buildManager.getLatestBuildResult(inStates);
            }
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return null;
        }
    }
}
