package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryFunctionE;
import com.zutubi.util.io.IOUtils;

import java.util.Set;

/**
 * Utility methods for working with {@link com.zutubi.pulse.core.scm.api.ScmClient}
 * instances.
 */
public class ScmClientUtils
{
    /**
     * Executes an action with an ScmClient created from the given
     * configuration.  Takes care of creation and cleanup of the client.
     *
     * @param scmConfiguration configuration used to instantiate the client
     * @param clientFactory    factory for creating the client
     * @param action           callback to run with the client
     * @param <T>              type returned by the callback, and thus this method
     * @return the result of the callback
     * @throws ScmException if the callback encounters an error
     */
    public static <T> T withScmClient(ScmConfiguration scmConfiguration, ScmClientFactory<ScmConfiguration> clientFactory, ScmAction<T> action) throws ScmException
    {
        ScmClient client = null;
        try
        {
            client = clientFactory.createClient(scmConfiguration);
            return action.process(client);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    /**
     * Executes an action with an ScmClient created from the given
     * configuration and an ScmContext.  Takes care of creation and cleanup of
     * the client and context.
     *
     * @param project    configuration used to create the client and context
     * @param scmManager factory for creating the client and context
     * @param action     callback to run with the client and context
     * @param <T>        type returned by the callback, and thus this method
     * @return the result of the callback
     * @throws ScmException if the callback encounters an error
     */
    public static <T> T withScmClient(ProjectConfiguration project, ScmManager scmManager, ScmContextualAction<T> action) throws ScmException
    {
        ScmClient client = null;
        try
        {
            ScmConfiguration scm = project.getScm();
            client = scmManager.createClient(scm);
            ScmContext context = scmManager.createContext(project);
            return action.process(client, context);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    public static Set<ScmCapability> getCapabilities(final Project project, ProjectConfiguration config, ScmManager manager) throws ScmException
    {
        return withScmClient(config, manager, new ScmContextualAction<Set<ScmCapability>>()
        {
            public Set<ScmCapability> process(ScmClient client, ScmContext context) throws ScmException
            {
                ScmContext c = (project != null && project.isInitialised()) ? context : null;
                return client.getCapabilities(c);
            }
        });
    }
    
    public static Set<ScmCapability> getCapabilities(ScmConfiguration config, ScmClientFactory<ScmConfiguration> clientFactory, final ScmContext context) throws ScmException
    {
        return withScmClient(config, clientFactory, new ScmAction<Set<ScmCapability>>()
        {
            public Set<ScmCapability> process(ScmClient scmClient) throws ScmException
            {
                return scmClient.getCapabilities(context);
            }
        });
    }

    public static interface ScmAction<T> extends UnaryFunctionE<ScmClient, T, ScmException>
    {
    }

    public static interface ScmContextualAction<T>
    {
        T process(ScmClient client, ScmContext context) throws ScmException;
    }
}
