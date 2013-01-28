package com.zutubi.pulse.acceptance.support;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceConstants;
import com.zutubi.pulse.core.scm.p4.PerforceCore;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Utilities for creating and working with Perforce projects.
 */
public class PerforceUtils
{
    public static final String P4PORT = ":6777";
    public static final String P4USER = "pulse";
    public static final String P4PASSWD = "pulse";
    public static final String P4CLIENT = "triviant";
    public static final String TRIVIAL_VIEW = "//depot/triviant/trunk/... //pulse/...";
    public static final String MAPPED_VIEW = "//depot/triviant/trunk/... //pulse/mapped/...";
    public static final String WORKSPACE_PREFIX = "pulse-";

    /**
     * Creates a perforce XML-RPC configuration using the triviant client as a
     * template.
     *
     * @param remoteApi used to call the remote API
     * @return s perforce configuration based on the triviant client
     * @throws Exception on a remote API error
     */
    public static Hashtable<String, Object> createSpecConfig(RemoteApiClient remoteApi) throws Exception
    {
        Hashtable<String, Object> p4Config = createPerforceConfig(remoteApi);
        p4Config.put("spec", P4CLIENT);
        return p4Config;
    }

    /**
     * Creates a perforce XML-RPC configuration using the the given client
     * view.
     *
     * @param remoteApi used to call the remote API
     * @param view         perforce client mapping as a single string
     * @return s perforce configuration based on the given view
     * @throws Exception on a remote API error
     */
    public static Hashtable<String, Object> createViewConfig(RemoteApiClient remoteApi, String view) throws Exception
    {
        Hashtable<String, Object> p4Config = createPerforceConfig(remoteApi);
        p4Config.put("useTemplateClient", false);
        p4Config.put("view", view);
        return p4Config;
    }

    /**
     * Creates a perforce XML-RPC configuration with default values.  A client
     * or view needs to be added to make the configuration valid.
     *
     * @param remoteApi used to call the remote API
     * @return a default perforce configuration
     * @throws Exception on a remote API error
     */
    public static Hashtable<String, Object> createPerforceConfig(RemoteApiClient remoteApi) throws Exception
    {
        Hashtable<String, Object> p4Config = remoteApi.createDefaultConfig("zutubi.perforceConfig");
        p4Config.put("port", P4PORT);
        p4Config.put("user", P4USER);
        p4Config.put("password", P4PASSWD);
        p4Config.put("monitor", false);
        return p4Config;
    }

    /**
     * Creates a core for low level interaction with the acceptance perforce
     * service.
     *
     * @return a low-level perforce client
     */
    public static PerforceCore createCore()
    {
        PerforceCore core = new PerforceCore();
        core.setEnv(PerforceConstants.ENV_PORT, P4PORT);
        core.setEnv(PerforceConstants.ENV_USER, P4USER);
        core.setEnv(PerforceConstants.ENV_PASSWORD, P4PASSWD);
        core.setEnv(PerforceConstants.ENV_CLIENT, P4CLIENT);
        return core;
    }

    /**
     * Returns the names of all Pulse perforce clients (i.e. those with the
     * Pulse workspace prefix pulse-).
     *
     * @param core client used to talk to perforce
     * @return the names of all perforce clients created by Pulse
     * @throws ScmException on a perforce error
     */
    public static Collection<String> getAllPulseWorkspaces(PerforceCore core) throws ScmException
    {
        return Collections2.filter(core.getAllWorkspaceNames(), new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return s.startsWith(WORKSPACE_PREFIX);
            }
        });
    }

    /**
     * Deletes all existing Pulse perforce clients (i.e. those with the Pulse
     * workspace prefix pulse-).
     *
     * @param core client used to talk to perforce
     * @throws ScmException on a perforce error
     */
    public static void deleteAllPulseWorkspaces(PerforceCore core) throws ScmException
    {
        for (String workspace: getAllPulseWorkspaces(core))
        {
            core.deleteWorkspace(workspace);
        }
    }
}
