package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.util.Stack;

/**
 *
 *
 */
public class ViewBuildAction extends ProjectActionSupport
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 100;

    private static final Logger LOG = Logger.getLogger(ViewBuildAction.class);

    private long id;
    private BuildResult result;
    private List<Changelist> changelists;
    private long selectedNode;

    private MasterConfigurationManager configurationManager;
    /**
     * Insanity to work around lack of locals in velocity.
     */
    private Stack<String> pathStack = new Stack<String>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(long selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public boolean haveSelectedNode()
    {
        return selectedNode != 0L && selectedNode != result.getId();
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
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

    public void validate()
    {

    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        checkPermissions(result);

        if(result.completed())
        {
            result.loadFailedTestResults(configurationManager.getDataDirectory(), getFailureLimit());
        }

        return SUCCESS;
    }

    public String appendSuitePath(String path, TestSuiteResult suite)
    {
        return path + "/" + urlEncode(suite.getName());
    }

    public String pushSuite(TestSuiteResult suite)
    {
        if(pathStack.empty())
        {
            return pathStack.push(urlEncode(suite.getName()));
        }
        else
        {
            return pathStack.push(appendSuitePath(pathStack.peek(), suite));
        }
    }

    public void popSuite()
    {
        pathStack.pop();
    }

    public List<Changelist> getChangelists()
    {
        if(changelists == null)
        {
            changelists = getBuildManager().getChangesForBuild(getResult());
        }
        return changelists;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
