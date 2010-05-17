package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.agent.SynchronisationTask;
import com.zutubi.util.Condition;
import com.zutubi.util.EnumUtils;

/**
 * The status tab for a specific agent.
 */
public class AgentStatusPage extends SeleniumPage
{
    public static final String ID_STATUS_TABLE = "agent.status";
    public static final String ID_BUILD_TABLE = "executing.build.stage";
    public static final String ID_SYNCH_TABLE = "synchronisation.messages";
    public static final String ID_PATTERN_SYNCH_MESSAGE = "synchronisation.message.%d";
    public static final String ID_PATTERN_STATUS_MESSAGE = "status.message.%d";
    public static final String ID_PATTERN_STATUS_POPUP_BUTTON = "status.popup.%d_link";
    
    private static final long TIMEOUT = 30000;

    private String agent;

    public AgentStatusPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls, ID_STATUS_TABLE, "agent status");
        this.agent = agent;
    }

    public String getUrl()
    {
        return urls.agentStatus(agent);
    }

    public boolean isExecutingBuildPresent()
    {
        return browser.isElementPresent(ID_BUILD_TABLE);
    }

    public String getExecutingProject()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 1, 1);
    }

    public String getExecutingOwner()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 2, 1);
    }

    public String getExecutingId()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 3, 1);
    }

    public String getExecutingStage()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 5, 1);
    }

    public String getExecutingRecipe()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 6, 1);
    }

    /**
     * @return true if the synchronisation messages table is present
     */
    public boolean isSynchronisationTablePresent()
    {
        return browser.isElementIdPresent(ID_SYNCH_TABLE);
    }

    /**
     * Indicates how many synchronisation messages are showing.
     *
     * @return the number of synchronisation messages showing
     */
    public int getSynchronisationMessageCount()
    {
        int count = 0;
        while (browser.isElementIdPresent(String.format(ID_PATTERN_SYNCH_MESSAGE, count + 1)))
        {
            count++;
        }

        return count;
    }

    /**
     * Returns details of the given synchronisation message.
     *
     * @param index zero-based index of the message, oldest messages come
     *              first
     * @return details for the given message
     */
    public SynchronisationMessage getSynchronisationMessage(int index)
    {
        String typeString = browser.getCellContents(ID_SYNCH_TABLE, index + 2, 0);
        String description = browser.getCellContents(ID_SYNCH_TABLE, index + 2, 1);
        String statusString = browser.getCellContents(ID_SYNCH_TABLE, index + 2, 2);
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
    public String clickAndWaitForSynchronisationMessageStatus(int index)
    {
        String linkId = String.format(ID_PATTERN_STATUS_POPUP_BUTTON, index + 1);
        browser.click(linkId);
        final String popupId = String.format(ID_PATTERN_STATUS_MESSAGE, index + 1);
        AcceptanceTestUtils.waitForCondition(new Condition()
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
    }
}
