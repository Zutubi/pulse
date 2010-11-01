package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.BuildSummaryTable;
import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

import java.util.List;

/**
 * The project history page shows completed builds for a project.
 */
public class ProjectHistoryPage extends SeleniumPage
{
    private static final String ID_STATE_FILTER = "state-filter";
    private static final String ID_STATE_FILTER_CLEAR = "state-filter-clear";
    
    private String projectName;
    private BuildSummaryTable builds;
    private Pager pager;

    public ProjectHistoryPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-history-" + projectName, projectName);
        this.projectName = projectName;
        builds = new BuildSummaryTable(browser, "history-builds");
        pager = new Pager(browser, "history-pager");
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("view.initialised");
    }

    @Override
    public String getUrl()
    {
        return urls.projectHistory(WebUtils.uriComponentEncode(projectName));
    }

    /**
     * Returns the current value in the state filter combo box.
     * 
     * @return the current state filter
     */
    public String getStateFilter()
    {
        return browser.getComboValue(ID_STATE_FILTER);
    }
    
    /**
     * Selects a state filter from the filter combo box.
     * 
     * @param stateFilter the new state filter value
     */
    public void setStateFilter(String stateFilter)
    {
        // Unfortunately selecting using the combo itself fails under selenium
        // due to security restrictions (can't navigate using JS).
        String location = getUrl() + pager.getCurrentPage() + "/";
        if (StringUtils.stringSet(stateFilter))
        {
            location += "stateFilter/" + stateFilter + "/";
        }
        
        browser.open(location);
    }

    /**
     * Clicks the clear state filter link.
     */
    public void clearStateFilter()
    {
        browser.click(ID_STATE_FILTER_CLEAR);
    }
    
    /**
     * Indicates if there is are any builds displayed on the page.
     *
     * @return true if there are some builds, false otherwise
     */
    public boolean hasBuilds()
    {
        return getBuildCount() > 0;
    }

    /**
     * Returns the number of entries in the visible builds table (note that
     * this may be fewer than the total number of builds due to paging).
     *
     * @return the number of entries in the builds table
     */
    public int getBuildCount()
    {
        return builds.getRowCount();
    }
    
    /**
     * Returns information about the builds shown, from the most recent (first
     * row of the table) to least recent.
     *
     * @return information about the builds shown
     */
    public List<BuildInfo> getBuilds()
    {
        return builds.getBuilds();
    }

    /**
     * Returns the paging component that presents history pages.
     * 
     * @return the paging component for page navigation
     */
    public Pager getPager()
    {
        return pager;
    }
}
