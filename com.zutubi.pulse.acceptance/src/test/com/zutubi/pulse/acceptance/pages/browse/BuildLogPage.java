package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * A page that represents the build log page.
 */
public class BuildLogPage extends AbstractLogPage
{
    protected String project;
    protected String buildNumber;

    public BuildLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber)
    {
        super(browser, urls, "build-log-" + projectName + "-" + buildNumber);
        this.project = projectName;
        this.buildNumber = String.valueOf(buildNumber);
    }

    public String getUrl()
    {
        return urls.buildLog(WebUtils.uriComponentEncode(project), buildNumber);
    }

    public void selectStage(String stageName)
    {
        browser.evalExpression(
                "var combo = selenium.browserbot.getCurrentWindow().Ext.getCmp('select-log-combo');" +
                "combo.setValue('" + stageName + "');" +
                "var store = combo.getStore();" +
                "combo.fireEvent('select', combo, store.getAt(store.find('value', '" + stageName + "')));"
        );
    }
}
