package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.HistoryPage;
import com.cinnamonbob.model.Project;

import java.util.List;

/**
 */
public class HistoryAction extends ProjectActionSupport
{
    private static final int SURROUNDING_PAGES = 10;

    private long id;
    private Project project;
    private List<BuildResult> history;
    private int startPage;
    private int itemsPerPage = 10;
    private int historyCount;

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

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        if (startPage < 0)
        {
            addActionError("Invalid start page '" + startPage + "'");
            return ERROR;
        }

        HistoryPage page = new HistoryPage(project, startPage * itemsPerPage, itemsPerPage);
        getBuildManager().fillHistoryPage(page);

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
