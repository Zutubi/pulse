package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildChangesPage extends SeleniumPage
{
    private long buildId;

    public PersonalBuildChangesPage(Selenium selenium, Urls urls, long buildId)
    {
        super(selenium, urls, "personal-build-" + Long.toString(buildId) + "-changes", "build " + buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.dashboardMyBuildChanges(Long.toString(buildId));
    }

    public String getCheckedOutRevision()
    {
        String text = selenium.getText("checked.out.revision");
        text = text.trim();
        String[] pieces = text.split(" ");
        return pieces[pieces.length - 1];
    }

    public String getChangedFile(int index)
    {
        return selenium.getTable(getId() + "." + (index + 2) + ".0");
    }
}
