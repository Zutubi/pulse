package com.zutubi.pulse.master.xwork.actions.project;

/**
 * Action for viewing project history.
 */
public class ProjectHistoryAction extends ProjectActionBase
{
    private int startPage = 0;
    private String stateFilter = ProjectHistoryDataAction.STATE_ANY;

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public String getStateFilter()
    {
        return stateFilter;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public String execute() throws Exception
    {
        getRequiredProject();
        return SUCCESS;
    }
}
