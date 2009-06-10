package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The artifacts tab for a build result.
 */
public class BuildArtifactsPage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildArtifactsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-artifacts", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildArtifacts(projectName, Long.toString(buildId));
    }

    /**
     * Generate the selenium locator for the named command node on the build artifacts page.
     *
     * @param command   name of the command in question.
     * @return the selenium locator for the command node
     */
    public String getCommandLocator(String command)
    {
        return "link=*command*::*" + command + "*";
    }

    /**
     * Generate the selenium locator for a named artifact node on the build artifacts page.
     *
     * @param artifact  name of the artifact in question
     *
     * @return  the selenium locator for the artifact node.
     */
    public String getArtifactLocator(String artifact)
    {
        return "link=" + artifact;
    }

    /**
     * Indicates whether or not an artifact of the given name exists in this
     * build.  Note, this does not imply that the artifact is available for
     * download.
     *
     * @param artifactName  name of the artifact being tested.
     * @return  true if the artifact exists, false otherwise.
     *
     * @see #isArtifactAvailable(String)
     */
    public boolean artifactExists(String artifactName)
    {
        return browser.isElementPresent(getArtifactLocator(artifactName));
    }

    /**
     * Indicates whether or not an artifact of the given name exists and was captured
     * / is available for download.
     *
     * @param artifactName  name of the artifact being tested.
     * @return  true if the artifact is available, false otherwise.
     */
    public boolean isArtifactAvailable(String artifactName)
    {
        // look up the icon, if it is the artifact broken icon, then the artifact is
        // not available.  Is there a better way to test this via the UI?.
        String id = browser.getAttribute(getArtifactLocator(artifactName) + "@id");
        int nodeNumber = Integer.valueOf(id.substring(11));
        String clazz = browser.getAttribute("id=ygtvfile" + nodeNumber + "@class");
        return !clazz.equals("treeview_broken");
    }
}
