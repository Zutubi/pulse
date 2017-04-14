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
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.adt.Pair;
import org.openqa.selenium.By;

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
        return browser.isElementIdPresent(ID_BUILD_BASICS);
    }

    public Pair<String, String> getBuildBasicsRow(int index)
    {
        return getRow(ID_BUILD_BASICS, index);
    }
    
    public boolean isBasicsRowPresent(String key)
    {
        return browser.isElementIdPresent(getBasicsId(key));
    }

    public String getBasicsValue(String key)
    {
        return browser.getText(By.id(getBasicsId(key)));
    }

    private String getBasicsId(String key)
    {
        return "basics-" + key;
    }
    
    public boolean isFeaturesTablePresent(Feature.Level level)
    {
        return browser.isElementIdPresent("features-" + level.getPrettyString());
    }

    protected Pair<String, String> getRow(String tableId, int index)
    {
        return new Pair<String, String>(browser.getCellContents(tableId, index + 1, 0), browser.getCellContents(tableId, index + 1, 1));
    }
}
