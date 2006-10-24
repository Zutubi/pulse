package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;
import com.zutubi.pulse.util.RandomUtils;

/**
 */
public class ResourceAcceptanceTest extends BaseAcceptanceTestCase
{
    private String resourceName;
    private String versionName;
    private String propertyName;

    protected void setUp() throws Exception
    {
        super.setUp();

        resourceName = "resource-" + RandomUtils.randomString(5);
        versionName = "version-" + RandomUtils.randomString(5);
        propertyName = "property-" + RandomUtils.randomString(5);

        login("admin", "admin");
        clickLink(Navigation.TAB_AGENTS);
        clickLinkWithText("master");
        clickLinkWithText("resources");
    }

    public void testAddResource()
    {
        addResource(resourceName);
        assertTextPresent("resource " + resourceName);
        assertTextPresent("default properties");

        clickLinkWithText("resources");
        assertTextPresent(resourceName);
        assertLinkPresent("edit_" + resourceName);
        assertLinkPresent("delete_" + resourceName);
    }

    public void testAddResourceValidation()
    {
        clickLink("resource.add");

        // select custom from the drop down.
        AddResourceWizard.Select select = new AddResourceWizard.Select(tester);
        select.assertFormPresent();
        select.nextFormElements("custom");
        select.assertFormNotPresent();

        // then select edit resource.
        AddResourceWizard.Custom custom = new AddResourceWizard.Custom(tester);
        custom.assertFormPresent();
        custom.nextFormElements("");

        custom.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddResourceDuplicate()
    {
        addResource(resourceName);
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
        custom.assertFormPresent();

        assertTextPresent("A resource with name '" + resourceName + "' already exists");
    }

    public void testEditResource()
    {
        addResource(resourceName);
        clickLinkWithText("edit resource");
        EditResourceForm form = new EditResourceForm(tester);
        form.assertFormPresent();
        form.saveFormElements(resourceName + "_edited");
        assertTextPresent("resource " + resourceName + "_edited");
        clickLinkWithText("edit resource");
        form.assertFormPresent();
        form.assertFormElements(resourceName + "_edited");
    }

    public void testEditResourceValidation()
    {
        addResource(resourceName);
        clickLinkWithText("edit resource");
        EditResourceForm form = new EditResourceForm(tester);
        form.assertFormPresent();
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testEditResourceDuplicate()
    {
        addResource(resourceName);
        clickLinkWithText("resources");
        addResource(resourceName + "2");
        clickLinkWithText("edit resource");
        EditResourceForm form = new EditResourceForm(tester);
        form.assertFormPresent();
        form.saveFormElements(resourceName);
        form.assertFormPresent();
        assertTextPresent("this agent already has a resource with name '" + resourceName + "'");
    }

    public void testDeleteResource()
    {
        addResource(resourceName);
        clickLinkWithText("resources");
        assertTextPresent(resourceName);
        clickLink("delete_" + resourceName);
        assertTextNotPresent(resourceName);
    }

    public void testAddResourceProperty()
    {
        addPropertyHelper("");
    }

    public void testAddVersionProperty()
    {
        addPropertyHelper(versionName);
    }

    private void addPropertyHelper(String version)
    {
        helperPreamble(version);
        addProperty(version, propertyName);
        assertProperties(version, new String[] { propertyName, getPropertyValue(propertyName), "true", "true" });
    }

    public void testAddResourcePropertyValidation()
    {
        addResource(resourceName);
        clickLink("property.add_");
        CreateResourcePropertyForm form = new CreateResourcePropertyForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", "", "false", "false");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddResourcePropertyDuplicate()
    {
        addPropertyDuplicateHelper("");
    }

    public void testAddVersionPropertyDuplicate()
    {
        addPropertyDuplicateHelper(versionName);
    }

    private void addPropertyDuplicateHelper(String version)
    {
        helperPreamble(version);
        addProperty(version, propertyName);
        clickLink("property.add_" + version);
        CreateResourcePropertyForm form = new CreateResourcePropertyForm(tester);
        form.assertFormPresent();
        form.saveFormElements(propertyName, "", "true", "true");
        form.assertFormPresent();

        String msg = version.length() == 0 ? "resource" : "version";
        assertTextPresent("This " + msg + " already contains a property with name '" + propertyName + "'");
    }

    public void testEditResourceProperty()
    {
        editPropertyHelper("");
    }

    public void testEditVersionProperty()
    {
        editPropertyHelper(versionName);
    }

    private void editPropertyHelper(String version)
    {
        helperPreamble(version);
        addProperty(version, propertyName);
        clickLink("edit_" + propertyName);

        EditResourcePropertyForm form = new EditResourcePropertyForm(tester);
        form.assertFormPresent();
        form.assertFormElements(propertyName, getPropertyValue(propertyName), "true", "true");
        form.saveFormElements(propertyName + "_edited", getPropertyValue(propertyName + "_edited"), "false", "false");
        assertProperties(version, new String[] {propertyName + "_edited", getPropertyValue(propertyName + "_edited"), "false", "false"} );
    }

    public void testEditResourcePropertyValidation()
    {
        addResource(resourceName);
        addProperty("", propertyName);
        clickLink("edit_" + propertyName);

        EditResourcePropertyForm form = new EditResourcePropertyForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", "", "false", "false");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testEditResourcePropertyDuplicate()
    {
        editPropertyDuplicateHelper("");
    }

    public void testEditVersionPropertyDuplicate()
    {
        editPropertyDuplicateHelper(versionName);
    }

    private void editPropertyDuplicateHelper(String version)
    {
        helperPreamble(version);
        addProperty(version, propertyName);
        addProperty(version, propertyName + "2");
        clickLink("edit_" + propertyName + "2");

        EditResourcePropertyForm form = new EditResourcePropertyForm(tester);
        form.assertFormPresent();
        form.saveFormElements(propertyName, "", "false", "false");
        form.assertFormPresent();

        String msg = version.length() == 0 ? "resource" : "version";
        assertTextPresent("this " + msg + " already contains a property with name '" + propertyName + "'");
    }

    public void testDeleteResourceProperty()
    {
        deletePropertyHelper("");
    }

    public void testDeleteVersionProperty()
    {
        deletePropertyHelper(versionName);
    }

    private void deletePropertyHelper(String version)
    {
        helperPreamble(version);
        addProperty(version, propertyName);
        assertTextPresent(propertyName);
        clickLink("delete_" + propertyName);
        assertTextNotPresent(propertyName);
    }

    public void testAddResourceVersion()
    {
        addResource(resourceName);
        addVersion(versionName);
        assertTextPresent(versionName);
        assertTablePresent("properties_" + versionName);
    }

    public void testAddResourceVersionValidation()
    {
        addResource(resourceName);
        clickLink("version.add");
        ResourceVersionForm form = new ResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("version is required");
    }

    public void testAddResourceVersionDuplicate()
    {
        addResource(resourceName);
        addVersion(versionName);
        clickLink("version.add");
        ResourceVersionForm form = new ResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements(versionName);
        form.assertFormPresent();
        assertTextPresent("this resource already has a version '" + versionName + "'");
    }

    public void testEditResourceVersion()
    {
        addResource(resourceName);
        addVersion(versionName);
        clickLink("edit_" + versionName);
        EditResourceVersionForm form = new EditResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements(versionName + "_edited");
        assertTextPresent(versionName + "_edited");
        assertTablePresent("properties_" + versionName + "_edited");
    }

    public void testEditResourceVersionValidation()
    {
        addResource(resourceName);
        addVersion(versionName);
        clickLink("edit_" + versionName);
        EditResourceVersionForm form = new EditResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("version is required");
    }

    public void testEditResourceVersionDuplicate()
    {
        addResource(resourceName);
        addVersion(versionName);
        addVersion(versionName + "2");
        clickLink("edit_" + versionName + "2");
        EditResourceVersionForm form = new EditResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements(versionName);
        form.assertFormPresent();
        assertTextPresent("this resource already has a version '" + versionName + "'");
    }

    private void helperPreamble(String version)
    {
        addResource(resourceName);
        if(version.length() > 0)
        {
            addVersion(version);
        }
    }

    private void assertProperties(String version, String[]... properties)
    {
        String [][] propertyRows = new String[properties.length][4];
        for(int i = 0; i < properties.length; i++)
        {
            propertyRows[i] = new String[] { properties[i][0], properties[i][1], properties[i][2], properties[i][3], "edit", "delete" };
        }

        assertTableRowsEqual("properties_" + version, 2, propertyRows);
    }

    private void addProperty(String version, String name)
    {
        clickLink("property.add_" + version);
        CreateResourcePropertyForm form = new CreateResourcePropertyForm(tester);
        form.assertFormPresent();
        form.saveFormElements(name, getPropertyValue(name), "true", "true");
    }

    private void addVersion(String name)
    {
        clickLink("version.add");
        ResourceVersionForm form = new ResourceVersionForm(tester);
        form.assertFormPresent();
        form.saveFormElements(name);
    }

    private String getPropertyValue(String name)
    {
        return name + "-value";
    }

    private void addResource(String name)
    {
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

        assertLinkPresent("edit_" + name);
        clickLink("edit_" + name);
    }
}
