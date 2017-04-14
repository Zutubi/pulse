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

package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

/**
 * Bootstrapper which runs an incremental update on a working copy using the
 * project's SCM.
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project)
    {
        super(project);
    }

    ScmClient doBootstrap(ExecutionContext executionContext)
    {
        writeFeedback("Updating " + FileSystemUtils.getNormalisedAbsolutePath(executionContext.getWorkingDir()));
        ScmClient scm = null;
        try
        {
            scm = createScmClient(executionContext);
            scm.update(executionContext, getRevision(executionContext), this);
            return scm;
        }
        catch (ScmException e)
        {
            IOUtils.close(scm);
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
