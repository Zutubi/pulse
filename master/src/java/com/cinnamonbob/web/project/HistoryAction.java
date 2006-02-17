package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.HistoryPage;
import com.cinnamonbob.model.Project;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Preparable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private int startPage;
    private int itemsPerPage = 10;
    private int historyCount;

    private Map<String, ResultState[]> nameToStates;
    private String stateFilter = STATE_ANY;
    private List<String> stateFilters;
    private List<String> specs;
    private String spec = "";

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public int getItemsPerPage()
    {
        return itemsPerPage;
    }

    public int getHistoryCount()
    {
        return historyCount;
    }

    public int getPageCount()
    {
        return (historyCount + itemsPerPage - 1) / itemsPerPage;
    }

    public int getPageRangeStart()
    {
        int offset = SURROUNDING_PAGES / 2;

        if (startPage + offset + 1 > getPageCount())
        {
            // show more to the left
            offset += startPage + offset + 1 - getPageCount();
        }

        int start = startPage - offset;
        if (start < 0)
        {
            start = 0;
        }

        return start;
    }

    public int getPageRangeEnd()
    {
        int offset = SURROUNDING_PAGES / 2;

        if (startPage - offset < 0)
        {
            // show more to the right
            offset += offset - startPage;
        }

        int end = startPage + offset;
        if (end >= getPageCount())
        {
            end = getPageCount() - 1;
        }

        return end;
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

    public List<String> getSpecs()
    {
        return specs;
    }

    public String getSpec()
    {
        return spec;
    }

    public void setSpec(String spec)
    {
        this.spec = spec;
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
        nameToStates.put(STATE_ANY, new ResultState[]{ResultState.IN_PROGRESS, ResultState.SUCCESS, ResultState.FAILURE, ResultState.ERROR});
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

        specs = new LinkedList<String>();
        specs.add("");
        for (BuildSpecification spec : project.getBuildSpecifications())
        {
            specs.add(spec.getName());
        }

        if (startPage < 0)
        {
            addActionError("Invalid start page '" + startPage + "'");
            return ERROR;
        }

        HistoryPage page = new HistoryPage(project, startPage * itemsPerPage, itemsPerPage);

        if (stateFilter.equals(STATE_ANY) && !TextUtils.stringSet(spec))
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

            BuildSpecification specification;
            if (!TextUtils.stringSet(spec))
            {
                specification = null;
            }
            else
            {
                specification = project.getBuildSpecification(spec);
                if (specification == null)
                {
                    addActionError("Invalid specification '" + spec + "'");
                    return ERROR;
                }
            }

            getBuildManager().fillHistoryPage(page, states, specification);
        }

        history = page.getResults();
        historyCount = page.getTotalBuilds();

        if (historyCount < startPage * itemsPerPage)
        {
            addActionError("Start page '" + startPage + "' is past the end of the results");
            return ERROR;
        }

        return SUCCESS;
    }

}
