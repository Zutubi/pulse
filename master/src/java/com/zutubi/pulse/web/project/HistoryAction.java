package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.PersistentName;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.PagingSupport;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.*;

/**
 */
public class HistoryAction extends ProjectActionSupport implements Preparable
{
    private static final int SURROUNDING_PAGES = 10;

    private static final String STATE_ANY = "";
    private static final String STATE_FAILURE_OR_ERROR = "failure or error";
    private static final String STATE_FAILURE = "failure";
    private static final String STATE_ERROR = "error";
    private static final String STATE_SUCCESS = "success";

    private long id;
    private Project project;
    private List<BuildResult> history;
    private PagingSupport pagingSupport = new PagingSupport(SURROUNDING_PAGES);

    private Map<String, ResultState[]> nameToStates;
    private String stateFilter = STATE_ANY;
    private List<String> stateFilters;
    private Long spec = 0L;
    private BuildColumns columns;

    /**
     * The system configuration manager.
     */
    private MasterConfigurationManager configurationManager;

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public Project getProject()
    {
        return project;
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

    public List<String> getStateFilters()
    {
        return stateFilters;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public Long getSpec()
    {
        return spec;
    }

    public void setSpec(Long spec)
    {
        this.spec = spec;
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
        stateFilters = new LinkedList<String>();
        stateFilters.add(STATE_ANY);
        stateFilters.add(STATE_FAILURE_OR_ERROR);
        stateFilters.add(STATE_FAILURE);
        stateFilters.add(STATE_ERROR);
        stateFilters.add(STATE_SUCCESS);

        nameToStates = new TreeMap<String, ResultState[]>();
        nameToStates.put(STATE_ANY, new ResultState[]{ResultState.SUCCESS, ResultState.FAILURE, ResultState.ERROR});
        nameToStates.put(STATE_FAILURE_OR_ERROR, new ResultState[]{ResultState.FAILURE, ResultState.ERROR});
        nameToStates.put(STATE_FAILURE, new ResultState[]{ResultState.FAILURE});
        nameToStates.put(STATE_ERROR, new ResultState[]{ResultState.ERROR});
        nameToStates.put(STATE_SUCCESS, new ResultState[]{ResultState.SUCCESS});
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        if (pagingSupport.getStartPage() < 0)
        {
            addActionError("Invalid start page '" + pagingSupport.getStartPage() + "'");
            return ERROR;
        }

        HistoryPage page = new HistoryPage(project, pagingSupport.getStartOffset(), pagingSupport.getItemsPerPage());

        if (stateFilter.equals(STATE_ANY) && (spec == null || spec == 0L))
        {
            // Common case
            getBuildManager().fillHistoryPage(page);
        }
        else
        {
            ResultState[] states = nameToStates.get(stateFilter);
            if (states == null)
            {
                addActionError("Invalid state filter '" + stateFilter + "'");
                return ERROR;
            }

            getBuildManager().fillHistoryPage(page, states);
        }

        history = page.getResults();
        pagingSupport.setTotalItems(page.getTotalBuilds());

        if (!pagingSupport.isStartPageValid())
        {
            addActionError("Start page '" + pagingSupport.getStartPage() + "' is past the end of the results");
            return ERROR;
        }

        User user = getLoggedInUser();
        columns = new BuildColumns(user == null ? User.getDefaultProjectColumns() : user.getProjectHistoryColumns(), projectManager);

        return SUCCESS;
    }

}
