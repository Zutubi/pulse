package com.zutubi.pulse.master.api;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginList;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.servercore.agent.*;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * Implements an XML-RPC API with testing-specific functionality.  Accepts
 * tokens from the main remote API as credentials.
 */
public class TestApi
{
    private static final Logger LOG = Logger.getLogger(TestApi.class);

    private AgentManager agentManager;
    private SynchronisationTaskFactory synchronisationTaskFactory;
    private TokenManager tokenManager;
    private PluginManager pluginManager;
    private BuildManager buildManager;

    public TestApi()
    {
    }

    /**
     * @internal Writes an error message to the log for testing.
     * @param token   authentication token
     * @param message message to write
     * @return true
     */
    public boolean logError(String token, String message)
    {
        tokenManager.verifyAdmin(token);
        LOG.severe(message);
        return true;
    }

    /**
     * @internal Writes a warning message to the log for testing.
     * @param token   authentication token
     * @param message message to write
     * @return true
     */
    public boolean logWarning(String token, String message)
    {
        tokenManager.verifyAdmin(token);
        LOG.warning(message);
        return true;
    }

    /**
     * @internal Enqueues a test synchronisation message for the given agent.
     * @param token       authentication token
     * @param agent       name of the agent to queue the message for
     * @param synchronous if true, enqueue a synchronous message, otherwise
     *                    enqueue an asynchronous one
     * @param description description of the message
     * @param succeed     true if the task should succeed, false otherwise
     * @return true
     */
    public boolean enqueueSynchronisationMessage(String token, String agent, boolean synchronous, String description, boolean succeed)
    {
        tokenManager.verifyAdmin(token);
        tokenManager.loginUser(token);
        try
        {
            SynchronisationTask task = synchronous ? new TestSynchronisationTask(succeed) : new TestAsyncSynchronisationTask(succeed);
            SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
            agentManager.enqueueSynchronisationMessages(internalGetAgent(agent), asList(asPair(message, description)));
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * @internal Installs a plugin from a local jar file.
     * @param token authentication token
     * @param pluginJar path of the jar file to install as a plugin
     * @return true
     * @throws PluginException on any error
     */
    public boolean installPlugin(String token, String pluginJar) throws PluginException
    {
        tokenManager.verifyAdmin(token);
        pluginManager.install(new File(pluginJar).toURI());
        return true;
    }

    /**
     * @internal Lists information about all plugins running on this server.
     * @param token authentication token
     * @return an array of plugin structs, one for each running plugin
     */
    public Vector<Hashtable<String, Object>> getRunningPlugins(String token)
    {
        tokenManager.verifyAdmin(token);
        List<Plugin> plugins = CollectionUtils.filter(pluginManager.getPlugins(), new PluginRunningPredicate());
        return new Vector<Hashtable<String, Object>>(PluginList.pluginsToHashes(plugins));
    }

    /**
     * Utility method that cancels any builds that are not in a completed
     * state.
     *
     * @param token authentication token.
     * @return true
     */
    public boolean cancelIncompleteBuilds(String token)
    {
        tokenManager.verifyAdmin(token);
        List<BuildResult> results = buildManager.queryBuilds(null, ResultState.getIncompleteStates(), -1, -1, -1, -1, false);
        for (BuildResult result : results)
        {
            buildManager.terminateBuild(result, "Terminated by batch cancel.");
        }
        return true;
    }

    private Agent internalGetAgent(String name) throws IllegalArgumentException
    {
        Agent agent = agentManager.getAgent(name);
        if (agent == null)
        {
            throw new IllegalArgumentException("Unknown agent: " + name);
        }
        return agent;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                // Rewire on startup to get the full token manager.  Maybe there is a way to delay
                // the creation of this instance until after the context is fully initialised and
                // hence the objectFactory.buildBean(RemoteApi.class) will return a fully wired instance?.
                SpringComponentContext.autowire(TestApi.this);
            }
        });
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
