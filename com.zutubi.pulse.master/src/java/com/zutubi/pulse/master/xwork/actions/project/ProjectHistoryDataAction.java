package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.HistoryPage;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Action to supply JSON data to the project history tab.
 */
public class ProjectHistoryDataAction extends ProjectActionBase
{
    private static final int BUILDS_PER_PAGE = 10;
    
    public static final String STATE_ANY = "[any]";
    private static final String STATE_BROKEN = "[any broken]";
    private static final String STATE_FAILURE = "failure";
    private static final String STATE_ERROR = "error";
    private static final String STATE_SUCCESS = "success";
    private static final String STATE_TERMINATED = "terminated";

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
    
    private int startPage;
    private String stateFilter = STATE_ANY;
    private ProjectHistoryModel model;
    
    private ConfigurationManager configurationManager;

    public void setStartPage(int page)
    {
        startPage = page;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public ProjectHistoryModel getModel()
    {
        return model;
    }

    public String execute()
    {
        Project project = getRequiredProject();

        ResultState[] states = NAME_TO_STATES.get(stateFilter);
        if (states == null)
        {
            stateFilter = STATE_ANY;
            states = ResultState.getCompletedStates();
        }
        
        int totalBuilds = buildManager.getBuildCount(project, states);
        int pageCount = (totalBuilds + BUILDS_PER_PAGE - 1) / BUILDS_PER_PAGE;

        if (startPage >= pageCount)
        {
            startPage = pageCount - 1;
        }
        
        if (startPage < 0)
        {
            startPage = 0;
        }

        HistoryPage page = new HistoryPage(project, startPage, BUILDS_PER_PAGE);
        buildManager.fillHistoryPage(page, states);

        List<BuildModel> builds = CollectionUtils.map(page.getResults(), new BuildResultToModelMapping(project.getConfig().getChangeViewer()));
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        
        model = new ProjectHistoryModel(urls.projectHistory(project), stateFilter, builds, new PagerModel(page.getTotalBuilds(), BUILDS_PER_PAGE, startPage));

        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
