package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 *
 */
public class ViewBuildAction extends CommandActionBase
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 100;

    private static final Logger LOG = Logger.getLogger(ViewBuildAction.class);

    private List<PersistentChangelist> changelists;
    private BuildColumns summaryColumns;

    private MasterConfigurationManager configurationManager;
    /**
     * Insanity to work around lack of locals in velocity.
     */
    private Stack<String> pathStack = new Stack<String>();
    private boolean cancelAvailable;
    private boolean deleteAvailable;
    private List<BuildHookConfiguration> hooks;

    public boolean haveRecipeResultNode()
    {
        return getRecipeResultNode() != null;
    }

    public boolean haveCommandResult()
    {
        return getCommandResult() != null;
    }

    public BuildResult getResult()
    {
        return getBuildResult();
    }

    public BuildColumns getSummaryColumns()
    {
        // Lazy init: not always used.
        if(summaryColumns == null)
        {
            User u = getLoggedInUser();
            summaryColumns = new BuildColumns(u == null ? UserPreferencesConfiguration.defaultProjectColumns() : u.getPreferences().getProjectSummaryColumns(), projectManager);
        }
        return summaryColumns;
    }

    public static int getFailureLimit()
    {
        int limit = DEFAULT_FAILURE_LIMIT;
        String property = System.getProperty(FAILURE_LIMIT_PROPERTY);
        if(property != null)
        {
            try
            {
                limit = Integer.parseInt(property);
            }
            catch(NumberFormatException e)
            {
                LOG.warning(e);
            }
        }

        return limit;
    }

    public List<BuildHookConfiguration> getHooks()
    {
        return hooks;
    }

    public boolean isCancelAvailable()
    {
        return cancelAvailable;
    }

    public boolean isDeleteAvailable()
    {
        return deleteAvailable;
    }

    public boolean hasActions()
    {
        return cancelAvailable || deleteAvailable;
    }

    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        boolean canWrite = accessManager.hasPermission(AccessManager.ACTION_WRITE, result.getProject());
        if (!isPersonal() && canWrite)
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            hooks = new LinkedList<BuildHookConfiguration>(projectConfig.getBuildHooks().values());
            Collections.sort(hooks, new NamedConfigurationComparator());
        }
        else
        {
            hooks = Collections.emptyList();
        }

        if (result.completed())
        {
            cancelAvailable = false;
            deleteAvailable = canWrite;
        }
        else
        {
            cancelAvailable = accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, result);
            deleteAvailable = false;
        }

        // Initialise detail down to the command level (optional)
        getCommandResult();

        result.loadFeatures(configurationManager.getDataDirectory());

        if(result.completed())
        {
            result.loadFailedTestResults(configurationManager.getDataDirectory(), getFailureLimit());
        }

        return SUCCESS;
    }

    public String pushSuite(PersistentTestSuiteResult suite)
    {
        if(pathStack.empty())
        {
            return pathStack.push(uriComponentEncode(suite.getName()));
        }
        else
        {
            return pathStack.push(pathStack.peek() + "/" + uriComponentEncode(suite.getName()));
        }
    }

    public void popSuite()
    {
        pathStack.pop();
    }

    public List<PersistentChangelist> getChangelists()
    {
        if(changelists == null)
        {
            changelists = buildManager.getChangesForBuild(getResult());
        }
        return changelists;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
