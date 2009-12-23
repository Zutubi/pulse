package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.util.PredicateRequest;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueue;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueueSnapshot;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

/**
 * This predicate ensures that only one poll request can be activated for a
 * specific scm at a particular point in time.
 */
public class OneActivePollPerScmPredicate implements Predicate<PredicateRequest<Project>>
{
    private static final Logger LOG = Logger.getLogger(OneActivePollPerScmPredicate.class);

    private PredicateRequestQueue<Project> requestQueue;

    private ScmManager scmManager;

    public OneActivePollPerScmPredicate(PredicateRequestQueue<Project> requestQueue)
    {
        this.requestQueue = requestQueue;
    }

    public boolean satisfied(PredicateRequest<Project> request)
    {
        try
        {
            String uid = getProjectsScmServerUid(request.getData());

            PredicateRequestQueueSnapshot<Project> snapshot = requestQueue.getSnapshot();
            for (PredicateRequest<Project> activeRequest : snapshot.getActivatedRequests())
            {
                String activeRequestUid = getProjectsScmServerUid(activeRequest.getData());
                if (uid.equals(activeRequestUid))
                {
                    return false;
                }
            }
            return true;
        }
        catch (ScmException e)
        {
            // if we are unable to accurately determine that an scm server
            // is not already in use, then we disable this check.  To leave it
            // enabled runs the risk of blocking an scm and halting the queue.
            LOG.warning(e);
            return true;
        }
    }

    private String getProjectsScmServerUid(Project project) throws ScmException
    {
        ProjectConfiguration config = project.getConfig();
        ScmClient client = scmManager.createClient(config.getScm());
        return client.getUid();
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
