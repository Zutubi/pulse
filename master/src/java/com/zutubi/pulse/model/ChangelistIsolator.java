package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.PersistentName;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;

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

    public ChangelistIsolator(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public synchronized List<Revision> getRevisionsToRequest(Project project, BuildSpecification specification, boolean force) throws SCMException
    {
        List<Revision> result;
        Revision latestBuiltRevision = null;

        if (latestRequestedRevisions.containsKey(specification.getId()))
        {
            latestBuiltRevision = latestRequestedRevisions.get(specification.getId());
        }
        else
        {
            // We have not yet seen a request for this build specification.
            // Find out what the last revision built of this specification
            // was (if any).
            latestBuiltRevision = getLatestBuiltRevision(project, specification);
            if (latestBuiltRevision != null)
            {
                latestRequestedRevisions.put(specification.getId(), latestBuiltRevision);
            }
        }

        if (latestBuiltRevision == null)
        {
            // The spec has never been built or even requested.  Just build
            // the latest (we need to start somewhere!).
            result = Arrays.asList(project.getScm().createServer().getLatestRevision());
        }
        else
        {
            // We now have the last requested revision, return every revision
            // since then.
            result = project.getScm().createServer().getRevisionsSince(latestBuiltRevision);
        }

        if (result.size() > 0)
        {
            // Remember the new latest
            latestRequestedRevisions.put(specification.getId(), result.get(result.size() - 1));
        }
        else if (force)
        {
            // Force a build of the latest revision anyway
            // We need to copy the revision as they are unique in the BUILD_SCM table!
            result = Arrays.asList(latestBuiltRevision.copy());
        }

        return result;
    }

    private Revision getLatestBuiltRevision(Project project, BuildSpecification specification)
    {
        for (int first = 0; /* forever */; first++)
        {
            List<BuildResult> latest = buildManager.queryBuilds(new Project[]{project}, null, new PersistentName[]{specification.getPname()}, -1, -1, null, first, 1, true);
            if (latest.size() > 0)
            {
                BuildScmDetails scmDetails = latest.get(0).getScmDetails();
                if(scmDetails != null)
                {
                    return scmDetails.getRevision();
                }
            }
            else
            {
                return null;
            }
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
