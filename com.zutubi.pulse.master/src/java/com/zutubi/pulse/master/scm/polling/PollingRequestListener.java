package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.scm.util.PredicateRequestQueueListenerAdapter;
import com.zutubi.pulse.master.scm.util.PredicateRequest;
import com.zutubi.pulse.master.model.Project;

/**
 * A simple implementation of the PredicateRequestQueueListener interface that
 * triggers a check for changes when a request is activated.
 *
 * @see PollingService#checkForChanges() 
 */
public class PollingRequestListener extends PredicateRequestQueueListenerAdapter<Project>
{
    private PollingService pollingService;
    
    @Override
    public boolean onActivation(PredicateRequest<Project> request)
    {
        pollingService.checkForChanges(request.getData(), null);

        return true;
    }

    public void setPollingService(PollingService pollingService)
    {
        this.pollingService = pollingService;
    }
}
