package com.zutubi.pulse.personal;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.xmlrpc.PulseXmlRpcClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * The client does the work of actually talking to the Pulse server, sending
 * personal build requests and getting back the results.
 */
public class PersonalBuildClient extends PersonalBuildSupport
{
    private PersonalBuildConfig config;

    public PersonalBuildClient(PersonalBuildConfig config)
    {
        this.config = config;
    }

    public WorkingCopy checkConfiguration() throws PersonalBuildException
    {
        checkRequiredConfig();

        try
        {
            PulseXmlRpcClient rpc = new PulseXmlRpcClient(config.getPulseUrl());
            String token = null;

            try
            {
                token = rpc.login(config.getPulseUser(), config.getPulsePassword());
                return prepare(rpc, token);
            }
            catch(PersonalBuildException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new PersonalBuildException("Unable to log in to pulse server: " + e.getMessage(), e);
            }
            finally
            {
                rpc.failSafeLogout(token);
            }
        }
        catch (MalformedURLException e)
        {
            throw new PersonalBuildException("Invalid pulse server URL '" + config.getPulseUrl() + "'", e);
        }
    }

    private void checkRequiredConfig() throws PersonalBuildException
    {
        if(config.getProject() == null)
        {
            throw new PersonalBuildException("Required property 'project' not specified.");
        }
    }

    private WorkingCopy prepare(PulseXmlRpcClient rpc, String token) throws PersonalBuildException
    {
        try
        {
            SCMConfiguration scmConfiguration = rpc.preparePersonalBuild(token, config.getProject(), config.getSpecification());
            String scmType = scmConfiguration.getType();

            WorkingCopy wc = WorkingCopyFactory.create(scmType, config.getBase());
            if(wc == null)
            {
                throw new PersonalBuildException("Personal builds are not supported for this SCM (" + scmType + ")");
            }

            wc.setUI(getUi());
            if(config.getCheckRepository())
            {
                if(!wc.matchesRepository(scmConfiguration.getRepositoryDetails()))
                {
                    PersonalBuildUI.Response response = ynaPrompt("This working copy may not match project '" + config.getProject() + "'.  Continue anyway?", PersonalBuildUI.Response.NO);
                    if(response.isPersistent())
                    {
                        config.setCheckRepository(!response.isAffirmative());
                    }

                    if(!response.isAffirmative())
                    {
                        throw new UserAbortException();
                    }
                }
            }

            return wc;
        }
        catch(PersonalBuildException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new PersonalBuildException("Unable to prepare personal build: " + e.getMessage(), e);
        }
    }

    public PatchArchive preparePatch(WorkingCopy wc, File patchFile) throws PersonalBuildException
    {
        WorkingCopyStatus status = getStatus(wc);

        if(status.isOutOfDate())
        {
            if(config.getConfirmUpdate())
            {
                // Ask user if we should update.
                PersonalBuildUI.Response response = ynaPrompt("Working copy must be updated to continue.  Update and continue?", PersonalBuildUI.Response.NO);
                if(response.isPersistent())
                {
                    config.setConfirmUpdate(!response.isAffirmative());
                }

                if(!response.isAffirmative())
                {
                    throw new UserAbortException();
                }
            }

            try
            {
                wc.update();
            }
            catch(SCMException e)
            {
                throw new PersonalBuildException("Unable to update working copy: " + e.getMessage(), e);
            }

            status = getStatus(wc);
        }

        return new PatchArchive(status, config.getBase(), patchFile);
    }

    private WorkingCopyStatus getStatus(WorkingCopy wc) throws PersonalBuildException
    {
        WorkingCopyStatus status = null;

        try
        {
            status = wc.getStatus();
        }
        catch (SCMException e)
        {
            throw new PersonalBuildException("Unable to get working copy status: " + e.getMessage(), e);
        }

        if(!status.inConsistentState())
        {
            // TODO: report what is inconsistent

            // Fatal, we can't deal with wc's in this state
            throw new PersonalBuildException("Working copy is not in a consistent state.");
        }

        return status;
    }

    public void sendRequest(PatchArchive patch) throws PersonalBuildException
    {
        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(config.getPulseUser(), config.getPulsePassword());
        client.getState().setCredentials(new AuthScope(null, -1), credentials);
        client.getParams().setAuthenticationPreemptive(true);

        PostMethod post = new PostMethod(config.getPulseUrl() + "/personal/personalBuild.action");
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
                // That's good ... now check the response
                System.out.println(post.getResponseBodyAsString());
            }
            else
            {
                // Not good
                error("Pulse server returned error code " + status + " (" + HttpStatus.getStatusText(status) + ")");
            }
        }
        catch (IOException e)
        {
            throw new PersonalBuildException("I/O error sending patch to pulse server: " + e.getMessage(), e);
        }
        finally
        {
            post.releaseConnection();
        }
    }
}
