package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.MasterAgentForm;
import com.zutubi.pulse.acceptance.forms.ResourceForm;
import com.zutubi.pulse.acceptance.forms.SlaveForm;
import com.zutubi.pulse.acceptance.forms.AddResourceWizard;
import com.zutubi.pulse.util.RandomUtils;

/**
 * <class-comment/>
 */
public class SlaveAcceptanceTest extends BaseAcceptanceTestCase
{
    private static final String SLAVE_HOST = "localhost";
    private static final String SLAVE_PORT = "7777";

    private String slaveName;
    private String resourceName;

    public SlaveAcceptanceTest()
    {
    }

    public SlaveAcceptanceTest(String name)
    {
        super(name);
        slaveName = "slave-" + RandomUtils.randomString(5);
        resourceName = "resource-" + RandomUtils.randomString(5);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        login("admin", "admin");
        clickLink(Navigation.TAB_AGENTS);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    // Master agent location

    public void testEditMasterLocation()
    {
        clickLink("edit_master");
        MasterAgentForm form = new MasterAgentForm(tester);
        form.assertFormPresent();
        form.saveFormElements("newhost");
        assertTextPresent("newhost:" + port);

        clickLink("edit_master");
        form.assertFormPresent();
        form.assertFormElements("newhost");
    }

    public void testEditMasterLocationValidation()
    {
        clickLink("edit_master");
        MasterAgentForm form = new MasterAgentForm(tester);
        form.assertFormPresent();
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("host is required");
    }

    public void testMasterEnableDisable()
    {
        // the initial status of the master should be enabled
        assertLinkPresent("agent.disable.master");
        assertLinkNotPresent("agent.enable.master");
        
        clickLink("agent.disable.master");

        // the status should now be disabled.
        assertLinkNotPresent("agent.disable.master");
        assertLinkPresent("agent.enable.master");

        clickLink("agent.enable.master");

        // and back again.
        assertLinkPresent("agent.disable.master");
        assertLinkNotPresent("agent.enable.master");
    }

    // Slave basics

    public void testAddSlave()
    {
        addAgent(slaveName);
        assertTextPresent(slaveName);
        assertTextPresent(SLAVE_HOST);
        assertTextPresent(SLAVE_PORT);

        clickLinkWithText(slaveName);
        assertAgentStatus(slaveName, SLAVE_HOST, SLAVE_PORT);
    }

    public void testAddSlaveValidation()
    {
        assertAndClick(Navigation.Agents.LINK_ADD_AGENTS);
        SlaveForm form = new SlaveForm(tester, true);
        assertSlaveValidation(form);
    }

    public void testAddSlaveDuplicate()
    {
        addAgent(slaveName);
        assertAndClick(Navigation.Agents.LINK_ADD_AGENTS);
        SlaveForm form = new SlaveForm(tester, true);
        assertSlaveDuplicate(form);
    }

    public void testEditSlave()
    {
        addAgent(slaveName);
        clickLink("edit_" + slaveName);

        SlaveForm form = new SlaveForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(slaveName, SLAVE_HOST, SLAVE_PORT);
        form.saveFormElements(slaveName + "_edited", SLAVE_HOST + "_edited", "1");

        assertTextPresent(slaveName + "_edited");
        assertTextPresent(SLAVE_HOST + "_edited");
        assertTextPresent("1");

        clickLinkWithText(slaveName + "_edited");
        assertAgentStatus(slaveName + "_edited", SLAVE_HOST + "_edited", "1");
    }

    public void testEditSlaveValidation()
    {
        addAgent(slaveName);
        clickLink("edit_" + slaveName);
        SlaveForm form = new SlaveForm(tester, false);
        assertSlaveValidation(form);
    }

    public void testEditSlaveDuplicate()
    {
        addAgent(slaveName);
        addAgent(slaveName + "2");
        clickLink("edit_" + slaveName + "2");
        SlaveForm form = new SlaveForm(tester, false);
        assertSlaveDuplicate(form);
    }

    public void testDeleteSlave()
    {
        addAgent(slaveName);
        assertTextPresent(slaveName);
        clickLink("delete_" + slaveName);
        assertTextNotPresent(slaveName);
    }

    //---( Agent/Slave Resources )---

    /**
     * CIB-519
     */
    // @Required(agent)
    public void testCanDeleteAgentWithAssociatedResource()
    {
        addAgent(slaveName);
        clickLinkWithText(slaveName);

        // add resource - need to be on the agent page.
        addResource(resourceName);

        clickLink(Navigation.TAB_AGENTS);
        assertTextPresent(slaveName);
        clickLink("delete_" + slaveName);
        assertTextNotPresent(slaveName);
    }

    // @Required(agent)
    public void testAddResource()
    {
        addAgent(slaveName);
        clickLinkWithText(slaveName);

        clickLinkWithText("resources");
        clickLink("resource.add");

        // select custom from the drop down.
        AddResourceWizard.Select select = new AddResourceWizard.Select(tester);
        select.assertFormPresent();
        select.nextFormElements("custom");
        select.assertFormNotPresent();

        // then select edit resource.
        AddResourceWizard.Custom custom = new AddResourceWizard.Custom(tester);
        custom.assertFormPresent();
        custom.nextFormElements(resourceName);
        custom.assertFormNotPresent();
    }

    // @Required(agent)
    public void testDeleteResource()
    {
        addAgent(slaveName);
        clickLinkWithText(slaveName);
        addResource(resourceName);
        assertTextPresent(resourceName);
        clickLink("delete_" + resourceName);
        assertTextNotPresent(resourceName);
    }

    private void assertResourceValidation(ResourceForm form)
    {
        form.assertFormPresent();
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("required");
    }

    private void assertSlaveDuplicate(SlaveForm form)
    {
        form.saveFormElements(slaveName, "localhost", "");
        form.assertFormPresent();
        assertTextPresent("An agent with name '" + slaveName + "' already exists");
    }

    private void assertSlaveValidation(SlaveForm form)
    {
        form.saveFormElements("", "", "hello");
        form.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("host is required");
        assertTextPresent("port must be a positive integer");
    }

    private void assertAgentStatus(String slaveName, String slaveHost, String slavePort)
    {
        assertTableRowsEqual("agent.status", 1, new String[][] {
                { "name", slaveName },
                { "location", slaveHost + ":" + slavePort },
        });
    }

    private void addAgent(String name)
    {
        assertAndClick(Navigation.Agents.LINK_ADD_AGENTS);
        SlaveForm form = new SlaveForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements(name, SLAVE_HOST, SLAVE_PORT);
    }

    /**
     * Add resource workflow.
     * Start: the agent page
     * Finish: the agents resources page.
     */
    private void addResource(String name)
    {
        clickLinkWithText("resources");
        clickLink("resource.add");

        // select custom from the drop down.
        AddResourceWizard.Select select = new AddResourceWizard.Select(tester);
        select.assertFormPresent();
        select.nextFormElements("custom");
        select.assertFormNotPresent();

        // then select edit resource.
        AddResourceWizard.Custom custom = new AddResourceWizard.Custom(tester);
        custom.assertFormPresent();
        custom.nextFormElements(name);
        custom.assertFormNotPresent();

        // return to the resources view.
        clickLinkWithText("resources");
    }
}
