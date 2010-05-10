package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Pair;

/**
 * Abstract base for pages that show build status information.
 */
public abstract class AbstractBuildStatusPage extends ResponsibilityPage
{
    public static final String ID_BUILD_BASICS = "build-basics";
    
    protected String projectName;
    protected long buildId;

    public AbstractBuildStatusPage(SeleniumBrowser browser, Urls urls, String id, String title, String projectName, long buildId)
    {
        super(browser, urls, id, title);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public boolean isBuildBasicsPresent()
    {
        return browser.isElementPresent(ID_BUILD_BASICS);
    }

    public Pair<String, String> getBuildBasicsRow(int index)
    {
        return getRow(ID_BUILD_BASICS, index);
    }

    public boolean isFeaturesTablePresent(Feature.Level level)
    {
        return browser.isElementPresent("features-" + level.getPrettyString());
    }

    protected Pair<String, String> getRow(String tableId, int index)
    {
        return new Pair<String, String>(browser.getCellContents(tableId, index + 1, 0), browser.getCellContents(tableId, index + 1, 1));
    }
}
