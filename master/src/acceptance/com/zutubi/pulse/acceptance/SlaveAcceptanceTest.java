package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.SlaveForm;
import com.zutubi.pulse.acceptance.forms.ResourceForm;
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
        beginAt("/");
        clickLinkWithText("agents");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

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
        clickLink("agent.add");
        SlaveForm form = new SlaveForm(tester, true);
        assertSlaveValidation(form);
    }

    public void testAddSlaveDuplicate()
    {
        addAgent(slaveName);
        clickLink("agent.add");
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

        clickLinkWithText("agents");
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

        ResourceForm form = new ResourceForm(tester);
        assertResourceValidation(form);

        form.saveFormElements(resourceName);
        form.assertFormNotPresent();
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
        clickLink("agent.add");
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

        ResourceForm form = new ResourceForm(tester);
        assertResourceValidation(form);
        form.saveFormElements(name);
        form.assertFormNotPresent();

        // return to the resources view.
        clickLinkWithText("resources");
    }
}
