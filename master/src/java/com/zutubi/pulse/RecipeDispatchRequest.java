/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.InitialBootstrapper;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.util.XMLUtils;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeDispatchRequest
{
    private BuildHostRequirements hostRequirements;
    private LazyPulseFile lazyPulseFile;
    private RecipeRequest request;
    private BuildResult build;
    private long queueTime;

    public RecipeDispatchRequest(BuildHostRequirements hostRequirements, LazyPulseFile lazyPulseFile, RecipeRequest request, BuildResult build)
    {
        this.hostRequirements = hostRequirements;
        this.lazyPulseFile = lazyPulseFile;
        this.request = request;
        this.build = build;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }

    public BuildResult getBuild()
    {
        return build;
    }

    public void queued()
    {
        queueTime = System.currentTimeMillis();
    }

    public long getQueueTime()
    {
        return queueTime;
    }

    public String getPrettyQueueTime()
    {
        return TimeStamps.getPrettyTime(queueTime);
    }

    public void prepare() throws PulseException
    {
        if(lazyPulseFile.getPulseFile() == null)
        {
            InitialBootstrapper scmBootstrapper = (InitialBootstrapper) request.getBootstrapper();
            scmBootstrapper.prepare();
            Revision revision = scmBootstrapper.getRevision();

            PulseFileDetails pulseFileDetails = build.getProject().getPulseFileDetails();
            lazyPulseFile.setPulseFile(pulseFileDetails.getPulseFile(request.getId(), build.getProject(), revision));
        }

        request.setPulseFileSource(XMLUtils.prettyPrint(lazyPulseFile.getPulseFile()));
    }
}
