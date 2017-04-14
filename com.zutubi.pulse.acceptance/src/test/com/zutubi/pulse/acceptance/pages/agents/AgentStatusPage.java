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
import com.zutubi.pulse.acceptance.components.pulse.project.CommentList;
import com.zutubi.pulse.acceptance.components.table.KeyValueTable;
import com.zutubi.pulse.acceptance.components.table.LinkTable;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;
import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The status tab for a specific agent.
 */
public class AgentStatusPage extends SeleniumPage implements CommentPage
{
    private String agent;
    private PropertyTable infoTable;
    private KeyValueTable statusTable;
    private ExecutingStageTable executingStageTable;
    private CommentList commentList;
    private SynchronisationMessageTable synchronisationMessagesTable;
    private LinkTable actionsTable;

    public AgentStatusPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls, "agent-status", "agent status");
        this.agent = agent;
        infoTable = new PropertyTable(browser, getId() + "-info");
        statusTable = new KeyValueTable(browser, getId() + "-status");
        executingStageTable = new ExecutingStageTable(browser, getId() + "-executingStage");
        synchronisationMessagesTable = new SynchronisationMessageTable(browser, getId() + "-synchronisationMessages");
        commentList = new CommentList(browser, getId() + "-comments");
        actionsTable = new LinkTable(browser, getId() + "-actions");
    }

    public String getUrl()
    {
        return urls.agentStatus(agent);
    }

    public PropertyTable getInfoTable()
    {
        return infoTable;
    }

    public KeyValueTable getStatusTable()
    {
        return statusTable;
    }

    public ExecutingStageTable getExecutingStageTable()
    {
        return executingStageTable;
    }

    public void waitForComments(long timeout)
    {
        commentList.waitFor(timeout);
    }

    public boolean isCommentsPresent()
    {
        return commentList.isPresent();
    }

    /**
     * Indicates if a comment with the given id is present.
     *
     * @param commentId unique id of the comment
     * @return true if the comment is present, false otherwise
     */
    public boolean isCommentPresent(long commentId)
    {
        return commentList.isCommentPresent(commentId);
    }

    /**
     * Indicates if a delete link is shown for the comment of the given id.
     *
     * @param commentId unique id of the comment
     * @return true if the given comment has a delete link
     */
    public boolean isCommentDeleteLinkPresent(long commentId)
    {
        return commentList.isDeleteLinkPresent(commentId);
    }

    /**
     * Clicks the delete link for the comment of the given id.
     *
     * @param commentId unique id of the comment
     * @return a confirmation dialog that will be poppe up on clicking the link
     */
    public ConfirmDialog clickDeleteComment(long commentId)
    {
        commentList.clickDeleteLink(commentId);
        return new ConfirmDialog(browser);
    }

    public SynchronisationMessageTable getSynchronisationMessagesTable()
    {
        return synchronisationMessagesTable;
    }

    public boolean isActionPresent(String actionName)
    {
        browser.waitForElement(actionsTable.getId());
        return actionsTable.isLinkPresent(actionName);
    }

    public void clickAction(String actionName)
    {
        actionsTable.clickLink(actionName);
    }
}
