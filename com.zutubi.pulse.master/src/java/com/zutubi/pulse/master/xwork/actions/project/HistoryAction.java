package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.PagingSupport;
import com.zutubi.pulse.master.xwork.interceptor.Preparable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class HistoryAction extends ProjectActionBase implements Preparable
{
    private static final int SURROUNDING_PAGES = 10;

    private static final String STATE_ANY = "[any]";
    private static final String STATE_FAILURE_OR_ERROR = "failure or error";
    private static final String STATE_FAILURE = "failure";
    private static final String STATE_ERROR = "error";
    private static final String STATE_SUCCESS = "success";

    private List<BuildResult> history;
    private PagingSupport pagingSupport = new PagingSupport(SURROUNDING_PAGES);

    private Map<String, ResultState[]> nameToStates;
    private String stateFilter = STATE_ANY;
    private BuildColumns columns;

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public List<BuildResult> getHistory()
    {
        return history;
    }

    public String getStateFilter()
    {
        return stateFilter;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public BuildColumns getColumns()
    {
        return columns;
    }

    public List<String> getPrepareParameterNames()
    {
        return null;
    }

    public void prepare() throws Exception
    {
        nameToStates = new TreeMap<String, ResultState[]>();
        nameToStates.put(STATE_ANY, ResultState.getCompletedStates());
        nameToStates.put(STATE_FAILURE_OR_ERROR, ResultState.getBrokenStates());
        nameToStates.put(STATE_FAILURE, new ResultState[]{ResultState.FAILURE});
        nameToStates.put(STATE_ERROR, new ResultState[]{ResultState.ERROR});
        nameToStates.put(STATE_SUCCESS, new ResultState[]{ResultState.SUCCESS});
    }

    public String execute()
    {
        Project project = getRequiredProject();

        if (pagingSupport.getStartPage() < 0)
        {
            addActionError("Invalid start page '" + pagingSupport.getStartPage() + "'");
            return ERROR;
        }

        HistoryPage page = new HistoryPage(project, pagingSupport.getStartOffset(), pagingSupport.getItemsPerPage());

        if (stateFilter.equals(STATE_ANY))
        {
            // Common case
            buildManager.fillHistoryPage(page);
        }
        else
        {
            ResultState[] states = nameToStates.get(stateFilter);
            if (states == null)
            {
                addActionError("Invalid state filter '" + stateFilter + "'");
                return ERROR;
            }

            buildManager.fillHistoryPage(page, states);
        }

        history = page.getResults();
        pagingSupport.setTotalItems(page.getTotalBuilds());

        if (!pagingSupport.isStartPageValid())
        {
            addActionError("Start page '" + pagingSupport.getStartPage() + "' is past the end of the results");
            return ERROR;
        }

        User user = getLoggedInUser();
        columns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectHistoryColumns());

        return SUCCESS;
    }

}
