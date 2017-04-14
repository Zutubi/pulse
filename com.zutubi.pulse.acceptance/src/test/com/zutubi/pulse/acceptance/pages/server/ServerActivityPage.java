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

package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.pulse.server.ActiveBuildsTable;
import com.zutubi.pulse.acceptance.components.pulse.server.QueuedBuildsTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

/**
 * The server activity page shows the server queues and active builds.
 */
public class ServerActivityPage extends SeleniumPage
{
    private static final String QUEUE_BUILD = "build";
    private static final String QUEUE_STAGE = "stage";
    
    private QueuedBuildsTable queued;
    private ActiveBuildsTable active;
    
    public ServerActivityPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server-activity", "server activity");
        queued = new QueuedBuildsTable(browser, getId() + "-queued");
        active = new ActiveBuildsTable(browser, getId() + "-active");
    }

    public String getUrl()
    {
        return urls.serverActivity();
    }

    /**
     * Returns the "build queue" table.
     * 
     * @return the build queue
     */
    public QueuedBuildsTable getQueued()
    {
        return queued;
    }

    /**
     * Returns the "active builds" table.
     * 
     * @return the active builds
     */
    public ActiveBuildsTable getActive()
    {
        return active;
    }

    /**
     * Returns the displayed state of the build queue.
     * 
     * @return the build queue state, e.g. "running", "ignoring all triggers"
     */
    public String getBuildQueueStatus()
    {
        return (String) browser.evaluateScript("return " + getQueueStatusExpression(QUEUE_BUILD));
    }

    /**
     * Indicates if the current user can pause the build queue.
     * 
     * @return true if an enabled pause link is present 
     */
    public boolean canPauseBuildQueue()
    {
        return canToggleQueue(QUEUE_BUILD, "pause");
    }

    /**
     * Indicates if the current user can resume the build queue.
     * 
     * @return true if an enabled resume link is present 
     */
    public boolean canResumeBuildQueue()
    {
        return canToggleQueue(QUEUE_BUILD, "resume");
    }

    /**
     * Clicks the build queue toggle link.
     */
    public void clickBuildQueueToggle()
    {
        browser.click(By.id("build-queue-toggle-text"));
    }
    
    /**
     * Returns the displayed state of the stage queue.
     * 
     * @return the stage queue state, e.g. "running", "paused"
     */
    public String getStageQueueStatus()
    {
        return (String) browser.evaluateScript("return " + getQueueStatusExpression(QUEUE_STAGE));
    }

    /**
     * Indicates if the current user can pause the stage queue.
     * 
     * @return true if an enabled pause link is present 
     */
    public boolean canPauseStageQueue()
    {
        return canToggleQueue(QUEUE_STAGE, "pause");
    }

    /**
     * Indicates if the current user can resume the stage queue.
     * 
     * @return true if an enabled resume link is present 
     */
    public boolean canResumeStageQueue()
    {
        return canToggleQueue(QUEUE_STAGE, "resume");
    }

    /**
     * Clicks the stage queue toggle link.
     */
    public void clickStageQueueToggle()
    {
        browser.click(By.id("stage-queue-toggle-text"));
    }
    
    private boolean canToggleQueue(String queuePrefix, String text)
    {
        String toggleText = (String) browser.evaluateScript("return " + getQueueToggleExpression(queuePrefix) + ".text");
        Boolean toggleEnabled = (Boolean) browser.evaluateScript("return " + getQueueToggleExpression(queuePrefix) + ".enabled");
        return toggleEnabled && toggleText.equals(text);
    }

    private String getQueueStatusExpression(String queuePrefix)
    {
        return "Ext.getCmp('" + queuePrefix + "-queue-state').text";
    }

    private String getQueueToggleExpression(String queuePrefix)
    {
        return "Ext.getCmp('" + queuePrefix + "-queue-toggle')";
    }
}
