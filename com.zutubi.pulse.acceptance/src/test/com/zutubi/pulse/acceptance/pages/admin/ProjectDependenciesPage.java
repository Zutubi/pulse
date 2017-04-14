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

package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.IDs;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

/**
 * The project dependencies page.
 */
public class ProjectDependenciesPage extends CompositePage
{
    public ProjectDependenciesPage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, path);
    }

    public DependencyForm clickAdd()
    {
        browser.click(By.id(ListPage.ADD_LINK));
        return browser.createForm(DependencyForm.class);
    }

    /**
     * Get the contents of the requested row from the dependencies table.  The row
     * index starts at 1 for the first row.
     *
     * @param row   the row index, starting at 1.
     *
     * @return the contents of the row
     */
    public DependencyRow getDependencyRow(int row)
    {
        row = row + 1; // skip the table header row.
        return new DependencyRow(
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 0),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 1),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 2),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 3)
        );
    }

    public DependencyForm clickView(String baseName)
    {
        String actionId = ListPage.ACTION_VIEW + ":" + baseName;
        if (!browser.isElementIdPresent(actionId))
        {
            throw new RuntimeException();
        }
        browser.click(By.id(actionId));
        return browser.createForm(DependencyForm.class);
    }

    public class DependencyRow
    {
        String projectName;
        String revision;
        String stageList;
        String transitive;

        public DependencyRow(String projectName, String revision, String stageList, String transitive)
        {
            this.projectName = projectName;
            this.revision = revision;
            this.stageList = stageList;
            this.transitive = transitive;
        }

        public String getProjectName()
        {
            return projectName;
        }

        public String getRevision()
        {
            return revision;
        }

        public String getStageList()
        {
            return stageList;
        }

        public String getTransitive()
        {
            return transitive;
        }
    }
}
