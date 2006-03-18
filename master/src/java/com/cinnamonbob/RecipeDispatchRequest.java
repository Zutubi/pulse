package com.cinnamonbob;

import com.cinnamonbob.core.BobException;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.TimeStamps;
import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.BuildHostRequirements;
import com.cinnamonbob.model.BuildResult;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeDispatchRequest
{
    private BuildHostRequirements hostRequirements;
    private LazyBobFile lazyBobFile;
    private RecipeRequest request;
    private BuildResult build;
    private long queueTime;

    public RecipeDispatchRequest(BuildHostRequirements hostRequirements, LazyBobFile lazyBobFile, RecipeRequest request, BuildResult build)
    {
        this.hostRequirements = hostRequirements;
        this.lazyBobFile = lazyBobFile;
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

    public void prepare() throws BobException
    {
        if(lazyBobFile.getBobFile() == null)
        {
            ScmBootstrapper scmBootstrapper = (ScmBootstrapper) request.getBootstrapper();
            scmBootstrapper.prepare();
            Revision revision = scmBootstrapper.getRevision();

            BobFileDetails bobFileDetails = build.getProject().getBobFileDetails();
            lazyBobFile.setBobFile(bobFileDetails.getBobFile(request.getId(), build.getProject(), revision));
        }

        request.setBobFileSource(lazyBobFile.getBobFile());
    }
}
