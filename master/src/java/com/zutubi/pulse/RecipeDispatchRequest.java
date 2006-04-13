/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.util.TimeStamps;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.BuildResult;
import nu.xom.Document;
import nu.xom.Builder;
import nu.xom.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

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
            ScmBootstrapper scmBootstrapper = (ScmBootstrapper) request.getBootstrapper();
            scmBootstrapper.prepare();
            Revision revision = scmBootstrapper.getRevision();

            PulseFileDetails pulseFileDetails = build.getProject().getPulseFileDetails();
            lazyPulseFile.setPulseFile(pulseFileDetails.getPulseFile(request.getId(), build.getProject(), revision));
        }

        request.setPulseFileSource(prettyPrint(lazyPulseFile.getPulseFile()));
    }

    private String prettyPrint(String pulseFile)
    {
        Builder builder = new Builder();
        ByteArrayOutputStream os = null;

        try
        {
            Document doc = builder.build(new StringReader(pulseFile));
            os = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(os);
            serializer.setIndent(4);
            serializer.write(doc);
            serializer.flush();
            return os.toString();
        }
        catch (Exception e)
        {
            // Try our best to pretty print, but not fatal if we can't
            return pulseFile;
        }
        finally
        {
            IOUtils.close(os);
        }
    }
}
