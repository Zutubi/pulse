package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.HistoryPage;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.pulse.master.xwork.actions.project.BuildModel;
import com.zutubi.pulse.master.xwork.actions.project.BuildResultToModelMapping;
import com.zutubi.pulse.master.xwork.actions.project.PagerModel;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Action to supply JSON data to the project history tab.
 */
public class HistoryDataAction extends ActionSupport
{
    public static final int BUILDS_PER_PAGE = 10;
    
    public static final String STATE_ANY = "";
    public static final String STATE_BROKEN = "broken";
    public static final String STATE_FAILURE = "failure";
    public static final String STATE_ERROR = "error";
    public static final String STATE_SUCCESS = "success";
    public static final String STATE_TERMINATED = "terminated";

    private static final Map<String, ResultState[]> NAME_TO_STATES;
    static
    {
        NAME_TO_STATES = new TreeMap<String, ResultState[]>();
        NAME_TO_STATES.put(STATE_ANY, ResultState.getCompletedStates());
        NAME_TO_STATES.put(STATE_BROKEN, ResultState.getBrokenStates());
        NAME_TO_STATES.put(STATE_FAILURE, new ResultState[]{ResultState.FAILURE});
        NAME_TO_STATES.put(STATE_ERROR, new ResultState[]{ResultState.ERROR});
        NAME_TO_STATES.put(STATE_TERMINATED, new ResultState[]{ResultState.TERMINATED});
        NAME_TO_STATES.put(STATE_SUCCESS, new ResultState[]{ResultState.SUCCESS});
    }

    private long projectId = 0;
    private long agentId = 0;
    private int startPage;
    private String stateFilter = STATE_ANY;
    private HistoryModel model;

    private ConfigurationManager configurationManager;
    private BuildManager buildManager;
    private AgentManager agentManager;

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    public void setStartPage(int page)
    {
        startPage = page;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public HistoryModel getModel()
    {
        return model;
    }

    public String execute()
    {
        Project project = null;
        if (projectId != 0)
        {
            project = projectManager.getProject(projectId, false);
            if (project == null)
            {
                throw new LookupErrorException("Unknown project [" + projectId + "]");
            }
        }

        Agent agent = null;
        if (agentId != 0)
        {
            agent = agentManager.getAgentById(agentId);
            if (agent == null)
            {
                throw new LookupErrorException("Unknown agent [" + agentId + "]");
            }
        }

        ResultState[] states = NAME_TO_STATES.get(stateFilter);
        if (states == null)
        {
            stateFilter = STATE_ANY;
            states = ResultState.getCompletedStates();
        }
        
        int totalBuilds = agent == null ? buildManager.getBuildCount(project, states) : buildManager.getBuildCount(agent, states);
        int pageCount = (totalBuilds + BUILDS_PER_PAGE - 1) / BUILDS_PER_PAGE;

        if (startPage >= pageCount)
        {
            startPage = pageCount - 1;
        }
        
        if (startPage < 0)
        {
            startPage = 0;
        }

        HistoryPage page;
        if (agent == null)
        {
            page = new HistoryPage(project, startPage * BUILDS_PER_PAGE, BUILDS_PER_PAGE);
        }
        else
        {
            page = new HistoryPage(agent, startPage * BUILDS_PER_PAGE, BUILDS_PER_PAGE);
        }

        buildManager.fillHistoryPage(page, states);

        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        BuildResultToModelMapping toModelMapping;
        if (project == null)
        {
            toModelMapping = new BuildResultToModelMapping(urls);
        }
        else
        {
            toModelMapping = new BuildResultToModelMapping(urls, project.getConfig().getChangeViewer());
        }

        List<BuildModel> builds = CollectionUtils.map(page.getResults(), toModelMapping);
        model = new HistoryModel(builds, new PagerModel(page.getTotalBuilds(), BUILDS_PER_PAGE, startPage));

        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
