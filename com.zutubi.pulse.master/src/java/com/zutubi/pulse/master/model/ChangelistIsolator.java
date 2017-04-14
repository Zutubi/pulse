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

package com.zutubi.pulse.master.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.*;

import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

/**
 */
public class ChangelistIsolator
{
    private Map<Long, Revision> latestRequestedRevisions = new TreeMap<Long, Revision>();
    private BuildManager buildManager;
    private ScmManager scmManager;

    public ChangelistIsolator(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public synchronized List<Revision> getRevisionsToRequest(ProjectConfiguration projectConfig, Project project, boolean force) throws ScmException
    {
        final Revision latestBuiltRevision;

        if (latestRequestedRevisions.containsKey(projectConfig.getHandle()))
        {
            latestBuiltRevision = latestRequestedRevisions.get(projectConfig.getHandle());
        }
        else
        {
            // We have not yet seen a request for this project. Find out what
            // the last revision built of this project was (if any).
            latestBuiltRevision = buildManager.getPreviousRevision(project);
            if (latestBuiltRevision != null)
            {
                latestRequestedRevisions.put(projectConfig.getHandle(), latestBuiltRevision);
            }
        }

        List<Revision> allRevisions = withScmClient(projectConfig, project.getState(), scmManager, new ScmContextualAction<List<Revision>>()
        {
            public List<Revision> process(ScmClient client, ScmContext context) throws ScmException
            {
                if (latestBuiltRevision == null)
                {
                    // The spec has never been built or even requested.  Just build
                    // the latest (we need to start somewhere!).
                    return Arrays.asList(client.getLatestRevision(context));
                }
                else
                {
                    // We now have the last requested revision, return every revision
                    // since then.
                    return client.getRevisions(context, latestBuiltRevision, null);
                }
            }
        });

        List<Revision> result = new ArrayList<Revision>();
        if (allRevisions.size() > 0)
        {
            List<List<Revision>> partitions = Lists.partition(allRevisions, projectConfig.getOptions().getMaxChangesPerBuild());
            for (List<Revision> partition : partitions)
            {
                result.add(Iterables.getLast(partition));
            }

            // Remember the new latest
            latestRequestedRevisions.put(projectConfig.getHandle(), Iterables.getLast(result));
        }
        else if (force)
        {
            // Force a build of the latest revision anyway
            assert latestBuiltRevision != null;
            result.add(latestBuiltRevision);
        }

        return result;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
