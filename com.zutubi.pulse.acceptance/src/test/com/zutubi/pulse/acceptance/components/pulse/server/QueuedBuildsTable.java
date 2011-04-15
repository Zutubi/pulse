package com.zutubi.pulse.acceptance.components.pulse.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;

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
     */
    public void clickCancel(int index)
    {
        String id = browser.evalExpression(getComponentJS() + ".data[" + index + "].id");
        browser.click("cancel-" + id + "-button");        
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
