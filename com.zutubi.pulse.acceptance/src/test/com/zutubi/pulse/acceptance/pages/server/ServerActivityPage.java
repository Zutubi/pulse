package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.pulse.server.ActiveBuildsTable;
import com.zutubi.pulse.acceptance.components.pulse.server.QueuedBuildsTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server activity page shows the server queues and active builds.
 */
public class ServerActivityPage extends SeleniumPage
{
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
     * Returns the displayed state of the stage queue.
     * 
     * @return the stage queue state, e.g. "running", "paused"
     */
    public String getStageQueueStatus()
    {
        return browser.evalExpression(getStageQueueStateExpression() + ".text");
    }

    /**
     * Indicates if the current user can pause the stage queue.
     * 
     * @return true if an enabled pause link is present 
     */
    public boolean canPauseStageQueue()
    {
        return canToggleStageQueue("pause");
    }

    /**
     * Indicates if the current user can resume the stage queue.
     * 
     * @return true if an enabled resume link is present 
     */
    public boolean canResumeStageQueue()
    {
        return canToggleStageQueue("resume");
    }

    private boolean canToggleStageQueue(String text)
    {
        String toggleText = browser.evalExpression(getStageQueueToggleExpression() + ".text");
        String toggleEnabled = browser.evalExpression(getStageQueueToggleExpression() + ".enabled");
        return Boolean.parseBoolean(toggleEnabled) && toggleText.equals(text);
    }

    /**
     * Clicks the stage queue toggle link.
     */
    public void clickStageQueueToggle()
    {
        browser.click("stage-queue-toggle-text");
    }
    
    private String getStageQueueStateExpression()
    {
        return SeleniumBrowser.CURRENT_WINDOW + ".Ext.getCmp('stage-queue-state')";
    }

    private String getStageQueueToggleExpression()
    {
        return SeleniumBrowser.CURRENT_WINDOW + ".Ext.getCmp('stage-queue-toggle')";
    }
}
