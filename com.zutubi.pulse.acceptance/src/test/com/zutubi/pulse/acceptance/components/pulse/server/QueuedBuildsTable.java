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

package com.zutubi.pulse.acceptance.components.pulse.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.pulse.acceptance.pages.MessageDialog;
import com.zutubi.pulse.acceptance.pages.YesNoDialog;
import org.openqa.selenium.By;

import java.util.Map;

/**
 * Corresponds to the Zutubi.pulse.server.QueuedBuildsTable JS component.
 */
public class QueuedBuildsTable extends SummaryTable
{
    /**
     * Creates a new queued builds table.
     * 
     * @param browser web browser
     * @param id      the table's component id
     */
    public QueuedBuildsTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Clicks the cancel link for the build request at the given index.
     *
     * @param index zero-based index of the build request
     * @return the confirm dialog that pops up
     */
    public MessageDialog clickCancel(int index)
    {
        long id = (Long) browser.evaluateScript("return " + getComponentJS() + ".data[" + index + "].id;");
        browser.click(By.id("cancel-" + id + "-button"));
        return new YesNoDialog(browser);
    }

    /**
     * Returns the build request at the given index.
     * 
     * @param index zero-based index of the request to retrieve
     * @return the build request at the given index
     */
    public QueuedBuild getBuild(int index)
    {
        Map<String, String> row = getRow(index);
        return new QueuedBuild(row.get("owner"), row.get("revision"), row.get("reason"), "cancel".equals(row.get("cancelPermitted")));
    }

    /**
     * Holds information about a build request.
     */
    public static class QueuedBuild
    {
        public String owner;
        public String revision;
        public String reason;
        public boolean cancelPermitted;

        /**
         * Creates a new queued build request struct.
         * 
         * @param owner           owner of the build (project or user)
         * @param revision        build revision, may be "[floating]"
         * @param reason          the build reason
         * @param cancelPermitted if true, a cancel link is present for the request
         */
        public QueuedBuild(String owner, String revision, String reason, boolean cancelPermitted)
        {
            this.owner = owner;
            this.revision = revision;
            this.reason = reason;
            this.cancelPermitted = cancelPermitted;
        }
    }
}
