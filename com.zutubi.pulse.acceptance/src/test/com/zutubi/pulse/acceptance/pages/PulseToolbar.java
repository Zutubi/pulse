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

package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * The Pulse toolbar is the toolbar at the very top of the Pulse page,
 * containing the breadcrumbs and user related links.
 */
public class PulseToolbar
{
    private static final String ID_TOOLBAR = "pulse-toolbar";
    private static final String ID_PROJECT_LINK = "pulse-toolbar-project-link";
    private static final String ID_BUILD_LINK = "pulse-toolbar-build-link";
    private static final String PREFIX_ID_BUILD_ITEM = "pulse-toolbar-build-item-";
    private static final String ID_AGENT_LINK = "pulse-toolbar-agent-link";
    private static final String ID_BUILD_MENU = "pulse-toolbar-actions-button";
    private static final String ID_NEXT_SUCCESSFUL_LINK = "next-successful-pulse-toolbar-actions";
    private static final String ID_PREVIOUS_SUCCESSFUL_LINK = "previous-successful-pulse-toolbar-actions";
    private static final String ID_NEXT_BROKEN_LINK = "next-broken-pulse-toolbar-actions";
    private static final String ID_PREVIOUS_BROKEN_LINK = "previous-broken-pulse-toolbar-actions";
    private static final String ID_LATEST_LINK = "latest-pulse-toolbar-actions";

    private SeleniumBrowser browser;

    public PulseToolbar(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isPresent()
    {
        return this.browser.isElementIdPresent(ID_TOOLBAR);
    }

    public boolean isBuildNavMenuPresent()
    {
        return this.browser.isElementIdPresent(ID_BUILD_MENU);
    }

    public boolean isNextSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_NEXT_SUCCESSFUL_LINK);
    }

    public void clickNextSuccessfulBuildLink()
    {
        browser.click(By.id(ID_NEXT_SUCCESSFUL_LINK));
    }

    public boolean isPreviousSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_PREVIOUS_SUCCESSFUL_LINK);
    }

    public void clickPreviousSuccessfulBuildLink()
    {
        browser.click(By.id(ID_PREVIOUS_SUCCESSFUL_LINK));
    }

    public boolean isNextBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_NEXT_BROKEN_LINK);
    }

    public void clickNextBrokenBuildLink()
    {
        browser.click(By.id(ID_NEXT_BROKEN_LINK));
    }

    public boolean isPreviousBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_PREVIOUS_BROKEN_LINK);
    }

    public void clickPreviousBrokenBuildLink()
    {
        browser.click(By.id(ID_PREVIOUS_BROKEN_LINK));
    }

    public boolean isLatestBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_LATEST_LINK);
    }

    public void clickLatestBuildLink()
    {
        browser.click(By.id(ID_LATEST_LINK));
    }

    public void clickOnNavMenu()
    {
        this.browser.click(By.id(ID_BUILD_MENU));
    }

    public boolean isBuildNavItemPresent(int buildNumber)
    {
        return browser.isElementIdPresent(PREFIX_ID_BUILD_ITEM + buildNumber);
    }

    public void clickBuildNavItem(int buildNumber)
    {
        browser.click(By.id(PREFIX_ID_BUILD_ITEM + buildNumber));
    }

    public boolean isBuildNavLinkPresent(int buildNumber)
    {
        return browser.isElementPresent(By.xpath("//span[@id='"+ID_BUILD_LINK+"']/a[text()='build " + buildNumber + "']"));
    }

    public boolean isProjectLinkPresent()
    {
        return browser.isElementIdPresent(ID_PROJECT_LINK);
    }

    public boolean isMyBuildsLinkPresent()
    {
        return browser.isElementIdPresent(ID_PROJECT_LINK);
    }

    public void waitFor()
    {
        browser.waitForElement(ID_TOOLBAR);
    }

    public void waitForBuildNav()
    {
        browser.waitForElement(ID_BUILD_LINK);
    }

    public boolean isBuildNavPresent()
    {
        return browser.isElementIdPresent(ID_BUILD_LINK);
    }
}
