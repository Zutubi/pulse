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

package com.zutubi.pulse.master.scm;

import com.google.common.base.Supplier;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.time.TimeStamps;

import java.util.Locale;

import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

/**
 * A supplier of revisions that gets the latest revision from a project's SCM.
 */
public class LatestScmRevisionSupplier implements Supplier<Revision>
{
    private Project project;
    private ScmManager scmManager;

    public LatestScmRevisionSupplier(Project project, ScmManager scmManager)
    {
        this.project = project;
        this.scmManager = scmManager;
    }

    public Revision get()
    {
        try
        {
            return withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<Revision>()
            {
                public Revision process(ScmClient client, ScmContext context) throws ScmException
                {
                    if (context.getPersistentContext() == null)
                    {
                        throw new ScmException("No persistent context, project state is '" + project.getState() + "'");
                    }

                    boolean supportsRevisions = client.getCapabilities(context).contains(ScmCapability.REVISIONS);
                    return supportsRevisions ? client.getLatestRevision(context) : new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), Locale.getDefault()));
                }
            });
        }
        catch (ScmException e)
        {
            throw new BuildException("Unable to retrieve latest revision for project '" + project.getName() + "': " + e.getMessage(), e);
        }
    }
}
