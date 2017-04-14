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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.agents.AgentStatusPage;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests for displaying/adding/removing comments on agents.
 */
public class AgentCommentAcceptanceTest extends CommentAcceptanceTestBase
{
    private String agentName;
    private AgentStatusPage agentSummaryPage;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();

        agentName = randomName() + "-agent";
        rpcClient.RemoteApi.insertSimpleAgent(agentName);
        agentSummaryPage = getBrowser().createPage(AgentStatusPage.class, agentName);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.RemoteApi.deleteConfig(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, agentName));
        super.tearDown();
    }

    public void testAddComment() throws Exception
    {
        addCommentOnPage(agentSummaryPage);
    }

    public void testCancelComment() throws Exception
    {
        cancelCommentOnPage(agentSummaryPage);
    }

    public void testDeleteComment() throws Exception
    {
        addAndDeleteCommentOnPage(agentSummaryPage);
    }

    public void testRemoteApi() throws Exception
    {
        remoteApiHelper();
    }

    protected long getLatestCommentId() throws Exception
    {
        Vector<Hashtable<String, Object>> comments = rpcClient.RemoteApi.getAgentComments(agentName);
        Hashtable<String, Object> latestComment = comments.get(comments.size() - 1);
        return Long.parseLong((String) latestComment.get("id"));
    }

    protected Vector<Hashtable<String,Object>> getComments(RpcClient client) throws Exception
    {
        return client.RemoteApi.getAgentComments(agentName);
    }

    protected String addComment(RpcClient client, String comment) throws Exception
    {
        return client.RemoteApi.addAgentComment(agentName, comment);
    }

    protected boolean deleteComment(RpcClient client, String id) throws Exception
    {
        return client.RemoteApi.deleteAgentComment(agentName, id);
    }
}
