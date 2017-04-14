/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.vfs.provider.pulse.ArtifactFileObject;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The artifacts tab for a build result.
 */
public class BuildArtifactsPage extends SeleniumPage
{
    private static final String ID_TREE = "artifacts-tree";
    private static final String ID_COMBO = "filter-combo";
    private static final String EXPRESSION_COMBO = "var combo = Ext.getCmp('" + ID_COMBO + "');";

    private String projectName;
    private long buildId;

    public BuildArtifactsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, ID_TREE, "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildArtifacts(WebUtils.uriComponentEncode(projectName), Long.toString(buildId));
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        waitForLoad();
    }

    private void waitForLoad()
    {
        browser.waitForCondition("return Ext && Ext.getCmp('artifacts-tree') && Ext.getCmp('artifacts-tree').loading === false");
    }

    /**
     * Returns the current value of the filter combo.
     *
     * @return the current filter
     */
    public String getCurrentFilter()
    {
        return (String) browser.evaluateScript(EXPRESSION_COMBO + " return combo.getValue();");
    }

    /**
     * Reset the current filter to show all artifacts.
     */
    public void resetFilter()
    {
        setFilterAndWait("");
    }

    /**
     * Sets the current filter to the given value, then waits for the tree to
     * reload.
     *
     * @param filter new filter value
     */
    public void setFilterAndWait(String filter)
    {
        browser.setComboByValue(ID_COMBO, filter);
        waitForLoad();
    }

    /**
     * Clicks the save filter link, and waits for it to be saved.
     */
    public void clickSaveFilterAndWait()
    {
        browser.click(By.id("save-filter-link"));
        browser.waitForStatus("Filter saved.");
    }

    /**
     * Generate the link text for the named command node on the build artifacts page.
     *
     * @param command name of the command in question.
     * @return the link text for the command node
     */
    public String getCommandLinkText(String command)
    {
        return "command :: " + command;
    }

    /**
     * Generate the XPath expression for a named artifact node on the build artifacts page.
     *
     * @param artifact name of the artifact in question
     * @return the xpath expression for the artifact node.
     */
    public String getArtifactXPath(String artifact)
    {
        return "//a[contains(@class, 'x-tree-node-anchor')]/span[text()='" + artifact + "']";
    }

    /**
     * Indicates whether or not an artifact of the given name exists in this
     * build.  Note, this does not imply that the artifact is available for
     * download.
     *
     * @param artifactName name of the artifact being tested.
     * @return true if the artifact exists, false otherwise.
     * @see #isArtifactAvailable(String)
     */
    public boolean isArtifactListed(String artifactName)
    {
        return browser.isElementPresent(By.xpath(getArtifactXPath(artifactName)));
    }

    /**
     * Indicates whether or not an artifact of the given name exists and was captured
     * / is available for download.
     *
     * @param artifactName name of the artifact being tested.
     * @return true if the artifact is available, false otherwise.
     */
    public boolean isArtifactAvailable(String artifactName)
    {
        String iconCls = browser.getAttribute(By.xpath(getArtifactXPath(artifactName) + "/preceding::img[1]"), "class");
        return !iconCls.contains(ArtifactFileObject.CLASS_PREFIX + ArtifactFileObject.CLASS_SUFFIX_BROKEN);
    }

    /**
     * Indicates if a file with the given name is listed under the artifact
     * with the given name.
     *
     * @param artifactName name of the artifact to look under
     * @param fileName     name of the file to look for
     * @return true if a file of the given name is listed under the given
     *         artifact
     */
    public boolean isArtifactFileListed(String artifactName, String fileName)
    {
        return browser.isElementPresent(By.xpath(getArtifactFileXPath(artifactName, fileName)));
    }

    private String getArtifactFileXPath(String artifactName, String fileName)
    {
        return getArtifactXPath(artifactName) + "/ancestor::tbody[1]" + getArtifactXPath(fileName);
    }

    /**
     * Returns the contents of the hash column for the given artifact file.
     *
     * @param artifactName name of the artifact the file is captured within
     * @param fileName     name of the file to get the hash for
     * @return the contents of the has column for the given file
     */
    public String getArtifactFileHash(String artifactName, String fileName)
    {
        String xpath = getArtifactFileXPath(artifactName, fileName) + "/ancestor::tr[1]/td[contains(@class, 'artifact-hash')]/div";
        return browser.getText(By.xpath(xpath)).trim();
    }

    /**
     * Clicks on the download action link for the given artifact file.
     * 
     * @param artifactName name of the artifact the file is captured within
     * @param fileName     name of the file to click the download link of
     */
    public void clickArtifactFileDownload(String artifactName, String fileName)
    {
        clickArtifactFileAction(artifactName, fileName, "download");
    }

    private void clickArtifactFileAction(String artifactName, String fileName, String type)
    {
        String xpath = getArtifactFileXPath(artifactName, fileName) + "/ancestor::tr[1]//img[@alt='" + type + "']";
        browser.click(By.xpath(xpath));
    }
}
