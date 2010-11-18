package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.AgentConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import static com.zutubi.pulse.acceptance.rpc.RemoteApiClient.SYMBOLIC_NAME_KEY;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

/**
 * Acceptance tests for configuration state display tables.
 */
public class ConfigStateAcceptanceTest extends AcceptanceTestBase
{
    private static final String FIELD_COMPATIBLE_STAGES = "compatibleStages";
    private static final String FIELD_STATUS = "status";

    private String agentPath;

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
        agentPath = rpcClient.RemoteApi.insertSimpleAgent(random);
        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected void tearDown() throws Exception
    {
        try
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
            rpcClient.logout();
        }
        finally
        {
            super.tearDown();
        }
    }

    public void testCompositeState() throws Exception
    {
        getBrowser().loginAsAdmin();
        AgentConfigPage agentConfigPage = getBrowser().openAndWaitFor(AgentConfigPage.class, random, false);
        assertTrue(agentConfigPage.isStatePresent());
        assertTrue(agentConfigPage.isStateFieldPresent(FIELD_STATUS));
    }

    public void testCollectionState() throws Exception
    {
        getBrowser().loginAsAdmin();
        ListPage agentResourcesPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getPath(agentPath, "resources"));
        assertTrue(agentResourcesPage.isStatePresent());
        assertTrue(agentResourcesPage.isStateFieldPresent(FIELD_COMPATIBLE_STAGES));
        assertEquals("all projects (all stages)", agentResourcesPage.getStateField(FIELD_COMPATIBLE_STAGES));
    }

    public void testExpandableState() throws Exception
    {
        // Insert multiple projects, including an incompatible one so the
        // state is not "all projects".
        for (int i = 0; i < 5; i++)
        {
            rpcClient.RemoteApi.insertSimpleProject(random + "-project-" + i, false);
        }

        String unsatisfiedProjectPath = rpcClient.RemoteApi.insertSimpleProject(random + "-project-unsatisfied", false);
        Hashtable<String, Object> resourceRequirement = new Hashtable<String, Object>();
        resourceRequirement.put(SYMBOLIC_NAME_KEY, "zutubi.resourceRequirementConfig");
        resourceRequirement.put("resource", "doesnotexist");
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(unsatisfiedProjectPath, "requirements"), resourceRequirement);

        getBrowser().loginAsAdmin();
        ListPage agentResourcesPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getPath(agentPath, "resources"));
        assertTrue(agentResourcesPage.isStatePresent());
        assertTrue(agentResourcesPage.isStateFieldPresent(FIELD_COMPATIBLE_STAGES));
        assertTrue(agentResourcesPage.isStateFieldExpandable(FIELD_COMPATIBLE_STAGES));
        assertTrue(agentResourcesPage.isStateFieldExpandVisible(FIELD_COMPATIBLE_STAGES));
        assertFalse(agentResourcesPage.isStateFieldCollapseVisible(FIELD_COMPATIBLE_STAGES));

        agentResourcesPage.expandStateField(FIELD_COMPATIBLE_STAGES);
        assertFalse(agentResourcesPage.isStateFieldExpandVisible(FIELD_COMPATIBLE_STAGES));
        assertTrue(agentResourcesPage.isStateFieldCollapseVisible(FIELD_COMPATIBLE_STAGES));

        agentResourcesPage.collapseStateField(FIELD_COMPATIBLE_STAGES);
        assertTrue(agentResourcesPage.isStateFieldExpandVisible(FIELD_COMPATIBLE_STAGES));
        assertFalse(agentResourcesPage.isStateFieldCollapseVisible(FIELD_COMPATIBLE_STAGES));
    }
}