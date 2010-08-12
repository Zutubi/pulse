package com.zutubi.pulse.master.api;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.servercore.agent.*;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.util.logging.Logger;

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
            SynchronisationTask task = synchronous ? new TestSynchronisationTask(succeed): new TestAsyncSynchronisationTask(succeed);
            SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
            agentManager.enqueueSynchronisationMessages(internalGetAgent(agent), asList(asPair(message, description)));
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
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

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }
}
