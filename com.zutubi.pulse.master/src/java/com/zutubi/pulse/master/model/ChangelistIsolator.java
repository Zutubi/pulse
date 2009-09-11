package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        List<Revision> result;
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

        result = withScmClient(projectConfig, scmManager, new ScmContextualAction<List<Revision>>()
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

        if (result.size() > 0)
        {
            // Remember the new latest
            latestRequestedRevisions.put(projectConfig.getHandle(), result.get(result.size() - 1));
        }
        else if (force)
        {
            // Force a build of the latest revision anyway
            assert latestBuiltRevision != null;
            result = Arrays.asList(latestBuiltRevision);
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
