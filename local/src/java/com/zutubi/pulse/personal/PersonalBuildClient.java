package com.zutubi.pulse.personal;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.PulseException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.IOException;

/**
 * The client does the work of actually talking to the Pulse server, sending
 * personal build requests and getting back the results.
 */
public class PersonalBuildClient
{
    private PersonalBuildConfig config;

    public PersonalBuildClient(PersonalBuildConfig config)
    {
        this.config = config;
    }

    public void sendRequest(PatchArchive patch) throws PulseException
    {
        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(config.getPulseUser(), config.getPulsePassword());
        client.getState().setCredentials(new AuthScope(null, -1), credentials);
        client.getParams().setAuthenticationPreemptive(true);

        PostMethod post = new PostMethod(config.getPulseUrl() + "/personalBuild.action");
        post.setDoAuthentication(true);

        try
        {
            Part[] parts = {
                    new StringPart("version", Version.getVersion().getBuildNumber()),
                    new StringPart("project", config.getProject()),
                    new StringPart("specification", config.getSpecification()),
                    new FilePart("patch.zip", patch.getPatchFile()),
            };
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            int status = client.executeMethod(post);
            if (status == HttpStatus.SC_OK)
            {
                // That's good ... don't do anything else ATM!
            }
            else
            {
                // Not good
                throw new PulseException("Pulse server returned error: " + post.getResponseBodyAsString());
            }
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error sending patch to pulse server: " + e.getMessage(), e);
        }
        finally
        {
            post.releaseConnection();
        }
    }
}
