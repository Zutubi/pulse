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

package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationTask;
import com.zutubi.util.Condition;
import com.zutubi.util.EnumUtils;
import org.openqa.selenium.By;

/**
 * A summary table of synchronisation messages.
 */
public class SynchronisationMessageTable extends SummaryTable
{
    private static final long TIMEOUT = 30000;

    public SynchronisationMessageTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns details of the given synchronisation message.
     *
     * @param index zero-based index of the message, oldest messages come
     *              first
     * @return details for the given message
     */
    public SynchronisationMessage getMessage(int index)
    {
        String typeString = browser.getCellContents(getId(), index + 2, 0);
        String description = browser.getCellContents(getId(), index + 2, 1);
        String statusString = browser.getCellContents(getId(), index + 2, 2);
        // Strip off text related to the status message popup (if any)
        int closeIndex = statusString.indexOf("close");
        if (closeIndex >= 0)
        {
            statusString = statusString.substring(0, closeIndex).trim();
        }
        return new SynchronisationMessage(EnumUtils.fromPrettyString(SynchronisationTask.Type.class, typeString), description, EnumUtils.fromPrettyString(AgentSynchronisationMessage.Status.class, statusString));
    }

    /**
     * Clicks the popdown link for a synchronisation message's status, waits
     * for the popup and returns the text within it.
     *
     * @param index index of the message to pop the status for
     * @return text found in the popup
     */
    public String clickAndWaitForMessageStatus(int index)
    {
        String linkId = String.format("synch-%d-link", index);
        browser.click(By.id(linkId));
        final By popupId = By.id(String.format("sync-%d", index));
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return browser.isElementPresent(popupId) && browser.getText(popupId).length() > 0;
            }
        }, TIMEOUT, "sync message status popup to appear");
        return browser.getText(popupId);
    }

    /**
     * Holds details about a synchronisation message displayed on this page.
     */
    public static class SynchronisationMessage
    {
        public SynchronisationTask.Type type;
        public String description;
        public AgentSynchronisationMessage.Status status;

        public SynchronisationMessage(SynchronisationTask.Type type, String description, AgentSynchronisationMessage.Status status)
        {
            this.type = type;
            this.description = description;
            this.status = status;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            SynchronisationMessage that = (SynchronisationMessage) o;

            if (!description.equals(that.description))
            {
                return false;
            }
            if (status != that.status)
            {
                return false;
            }
            if (type != that.type)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = type.hashCode();
            result = 31 * result + description.hashCode();
            result = 31 * result + status.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return type.getPrettyString() + ": " + description + ": " + status.getPrettyString();
        }
    }
}
