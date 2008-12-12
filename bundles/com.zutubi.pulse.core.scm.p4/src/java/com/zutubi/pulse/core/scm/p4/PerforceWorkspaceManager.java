package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A manager used by the Perforce implementation to manage workspaces.  Allows
 * sharing of workspaces without concurrency issues.  Since both Pulse and
 * Perforce use the term client for different things, we use the alternative
 * Perforce term "workspace".
 */
public class PerforceWorkspaceManager implements ScmClientFactory<PerforceConfiguration>
{
    private static final Logger LOG = Logger.getLogger(PerforceWorkspaceManager.class);

    private static final String WORKSPACE_NAME_SEPARATOR = "-";
    private static final String PROPERTY_WORKSPACE_PREFIX = "pulse.p4.client.prefix";
    private static final String DEFAULT_WORKSPACE_PREFIX = "pulse" + WORKSPACE_NAME_SEPARATOR;
    private static final String TEMP_WORKSPACE_TAG = "temp" + WORKSPACE_NAME_SEPARATOR;
    private static final String WORKSPACE_UNIQUE_SUFFIX = "$";

    private final Set<String> workspacesInUse = new HashSet<String>();

    private String allocateWorkspaceName(long handle)
    {
        String name = getWorkspacePrefix(handle);
        synchronized (workspacesInUse)
        {
            while (workspacesInUse.contains(name))
            {
                name += WORKSPACE_UNIQUE_SUFFIX;
            }

            workspacesInUse.add(name);
        }

        return name;
    }

    private PerforceWorkspace updateWorkspace(PerforceCore core, PerforceConfiguration configuration, String workspaceName, File root, String description, boolean temporary) throws ScmException
    {
        String rootPath = FileSystemUtils.getNormalisedAbsolutePath(root);
        PerforceWorkspace workspace = core.createOrUpdateWorkspace(configuration.getSpec(), workspaceName, description, rootPath);
        workspace.setTemporary(temporary);
        return workspace;
    }

    static String getWorkspacePrefix()
    {
        return System.getProperty(PROPERTY_WORKSPACE_PREFIX, DEFAULT_WORKSPACE_PREFIX);
    }

    static String getWorkspacePrefix(long projectHandle)
    {
        return getWorkspacePrefix() + projectHandle;
    }

    static String getSyncWorkspaceName(ExecutionContext context)
    {
        return getWorkspacePrefix(context.getLong(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT_HANDLE)) +
                WORKSPACE_NAME_SEPARATOR +
                Long.toString(context.getLong(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT_HANDLE));
    }

    static String getSyncWorkspaceDescription(ExecutionContext context)
    {
        String projectName = context.getString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT);
        String agentName = context.getString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT);
        return "Sync workspace for project '" + projectName + "' on agent '" + agentName + "'.";
    }

    /**
     * Ensures an appropriate workspace exists for bootstrapping builds with
     * the given configuration and context, and returns the details.  Such
     * workspaces persist so that they may be reused for future builds.  If
     * the workspace already exists at the time of this call, it may be
     * updated to reflect changes in the template workspace.
     *
     * @param core          core used to interact with Perforce
     * @param configuration configuration describing how t connect to Perforce
     *                      and what template workspace to use
     * @param context       context for the build
     * @return details of the created workspace
     * @throws ScmException on any error
     */
    public PerforceWorkspace getSyncWorkspace(PerforceCore core, PerforceConfiguration configuration, ExecutionContext context) throws ScmException
    {
        return updateWorkspace(core, configuration, getSyncWorkspaceName(context), context.getWorkingDir(), getSyncWorkspaceDescription(context), false);
    }

    static String getTemporaryWorkspaceDescription()
    {
        return "Temporary master workspace.";
    }

    static String getPersistentWorkspaceDescription(ScmContext scmContext)
    {
        return "Persistent master workspace for project '" + scmContext.getProjectName() + "'.";
    }

    /**
     * Allocates a workspace to be used for Perforce tasks on the master, e.g.
     * background tasks like polling, or user-driven tasks like browsing.
     * Workspaces allocated in this way must be freed using {@link #freeWorkspace(PerforceCore, PerforceWorkspace)}
     * when no longer in use.
     * <p/>
     * Workspaces will persist and be reused where possible when they can be
     * associated to a project (i.e. when the SCM context is not null).
     *
     * @param core          core to use to talk to Perforce
     * @param configuration configuration describing how to contact Perforce
     *                      and the template workspace
     * @param scmContext    context in which the SCM operation is executing
     * @return details of the allocated workspace
     * @throws ScmException on any error
     * @see #freeWorkspace(PerforceCore, PerforceWorkspace)
     */
    public PerforceWorkspace allocateWorkspace(PerforceCore core, PerforceConfiguration configuration, ScmContext scmContext) throws ScmException
    {
        String workspaceName;
        File root;
        String description;
        boolean temporary;
        if (scmContext == null)
        {
            workspaceName = getWorkspacePrefix() + TEMP_WORKSPACE_TAG + RandomUtils.randomString(10);
            root = new File(".");
            description = getTemporaryWorkspaceDescription();
            temporary = true;
        }
        else
        {
            workspaceName = allocateWorkspaceName(scmContext.getProjectHandle());
            root = scmContext.getPersistentWorkingDir();
            description = getPersistentWorkspaceDescription(scmContext);
            temporary = false;
        }

        return updateWorkspace(core, configuration, workspaceName, root, description, temporary);
    }

    /**
     * Frees a workspace previously allocated with {@link #allocateWorkspace(PerforceCore, com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration, com.zutubi.pulse.core.scm.api.ScmContext)}.
     * This will either delete the workspace (if it is temporary), or merely
     * allow it to be resused later for another operation.
     *
     * @param core      core to use to talk to Perforce
     * @param workspace the workspace to free
     * @see #allocateWorkspace(PerforceCore, com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration, com.zutubi.pulse.core.scm.api.ScmContext)
     */
    public void freeWorkspace(PerforceCore core, PerforceWorkspace workspace)
    {
        if (workspace != null)
        {
            if (workspace.isTemporary())
            {
                try
                {
                    core.deleteWorkspace(workspace.getName());
                }
                catch (ScmException e)
                {
                    LOG.warning("Unable to delete client: " + e.getMessage(), e);
                }
            }

            synchronized (workspacesInUse)
            {
                workspacesInUse.remove(workspace.getName());
            }
        }
    }

    /**
     * Deletes all persistent workspaces associated with a given project, as
     * defined by the SCM context.
     *
     * @param core    core used to communicate with Perforce
     * @param context context in which the SCM operation is executing
     * @param handler used to report feedback on progress
     * @throws ScmException on any error
     */
    public void cleanupPersistentWorkspaces(PerforceCore core, ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        handler.status("Cleaning up all persistent workspaces for project...");
        String prefix = getWorkspacePrefix(context.getProjectHandle());
        List<String> allWorkspaces = core.getAllWorkspaceNames();
        for (String workspace: allWorkspaces)
        {
            if (workspace.startsWith(prefix))
            {
                handler.status("  Deleting workspace '" + workspace + "'...");
                core.deleteWorkspace(workspace);
                handler.status("  Workspace deleted.");
            }
        }
        handler.status("Workspaces cleanup complete.");
    }

    public ScmClient createClient(PerforceConfiguration config) throws ScmException
    {
        return new PerforceClient(config, this);
    }
}
