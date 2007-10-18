package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.DeleteConfirmPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ProjectManager;

import java.util.Hashtable;

/**
 * Acceptance tests that verify operation of the configuration UI by trying
 * some real cases against a running server.
 */
public class ConfigUIAcceptanceTest extends SeleniumTestBase
{
    private static final String CHECK_PROJECT = "config-check-project";

    public void testEmptyOptionsAddedForSelects() throws Exception
    {
        // When configuring a template and a single select is shown, that
        // single select should have an empty option added.
        loginAsAdmin();
        goTo(urls.adminProjects());
        addProject(random, true, ProjectManager.GLOBAL_PROJECT_NAME, true);
        goTo(urls.adminProject(random) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        String[] options = form.getComboBoxOptions("checkoutScheme");
        assertEquals("", options[0]);
    }

    public void testDeleteListItemFromTemplateChild() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String parentName = random + "-parent";
            String childName = random + "-child";
            xmlRpcHelper.insertTrivialProject(parentName, true);
            String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
            String labelsPath = PathUtils.getPath(childPath, "labels");

            loginAsAdmin();
            ListPage labelsPage = new ListPage(selenium, urls, labelsPath);
            labelsPage.goTo();
            labelsPage.clickAdd();

            LabelForm labelForm = new LabelForm(selenium);
            labelForm.waitFor();
            labelForm.finishFormElements("my-label");

            labelsPage.waitFor();
            String baseName = getNewestListItem(labelsPath);
            labelsPage.assertItemPresent(baseName, null, "view", "delete");
            DeleteConfirmPage deleteConfirmPage = labelsPage.clickDelete(baseName);
            deleteConfirmPage.waitFor();
            labelsPage = deleteConfirmPage.confirm();

            labelsPage.assertItemNotPresent(baseName);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    public void testCheckForm() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(true, "ok");
    }

    public void testCheckFormFailure() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "svn://localhost:9999/foo");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(false, "Connection refused");
    }

    public void testCheckFormValidationFailure() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(false, "unable to check configuration due to validation errors");
        assertTextPresent("url requires a value");
    }

    public void testCheckFormCheckFieldValidationFailure() throws Exception
    {
        loginAsAdmin();
        goTo(urls.admin() + "settings/emailConfig/");
        EmailSettingsForm form = new EmailSettingsForm(selenium);
        form.waitFor();
        EmailSettingsCheckForm checkForm = new EmailSettingsCheckForm(form);
        checkForm.checkAndAssertResult(false, "unable to check configuration due to validation errors", "");
        assertTextPresent("emailAddress requires a value");
    }

    public void testClearItemPicker() throws Exception
    {
        ensureProject(random);

        loginAsAdmin();
        ProjectConfigPage configPage = new ProjectConfigPage(selenium, urls, random, false);
        configPage.goTo();
        ListPage listPage = configPage.clickCollection("permissions", "permissions");
        listPage.waitFor();
        listPage.assertCellContent(0, 1, "[view]");

        selenium.click("link=view");
        ProjectAclForm form = new ProjectAclForm(selenium);
        form.waitFor();
        form.assertFormElements("all users", "view");
        form.saveFormElements(null, "");
        listPage.waitFor();

        listPage.assertCellContent(0, 1, "[]");
        selenium.click("link=view");
        form = new ProjectAclForm(selenium);
        form.waitFor();
        form.assertFormElements("all users", "");
    }

    public void testClearMultiSelect() throws Exception
    {
        ensureProject(random);

        loginAsAdmin();
        ProjectConfigPage configPage = new ProjectConfigPage(selenium, urls, random, false);
        configPage.goTo();
        AntTypeForm form = configPage.clickComposite("project type", new AntTypeForm(selenium));
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "");
        form.applyFormElements(null, null, null, null, "ant");
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "ant");

        form.applyFormElements(null, null, null, null, "");
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "");
    }

    public void testNameValidationDuplicate() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
            String propertiesPath = PathUtils.getPath(projectPath, "properties");
            Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
            property.put("name", "p1");
            xmlRpcHelper.insertConfig(propertiesPath, property);

            loginAsAdmin();
            ListPage propertiesPage = new ListPage(selenium, urls, propertiesPath);
            propertiesPage.goTo();
            propertiesPage.clickAdd();

            ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
            form.waitFor();
            form.finishFormElements("p1", "value", null, null, null);
            form.assertFormPresent();
            assertTextPresent("name is already in use, please select another name");
        }
        finally
        {
            xmlRpcHelper.logout();
        }        
    }

    public void testNameValidationDuplicateInherited() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String parentName = random + "-parent";
            String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
            String parentPropertiesPath = PathUtils.getPath(parentPath, "properties");
            Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
            property.put("name", "p1");
            xmlRpcHelper.insertConfig(parentPropertiesPath, property);
            String childPath = xmlRpcHelper.insertTrivialProject(random + "-child", parentName, false);

            loginAsAdmin();
            ListPage propertiesPage = new ListPage(selenium, urls, PathUtils.getPath(childPath, "properties"));
            propertiesPage.goTo();
            propertiesPage.clickAdd();

            ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
            form.waitFor();
            form.finishFormElements("p1", "value", null, null, null);
            form.assertFormPresent();
            assertTextPresent("name is already in use, please select another name");
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    public void testNameValidationDuplicateInDescendent() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String parentName = random + "-parent";
            String childName = random + "-child";
            String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
            String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
            String childPropertiesPath = PathUtils.getPath(childPath, "properties");
            Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
            property.put("name", "p1");
            xmlRpcHelper.insertConfig(childPropertiesPath, property);

            loginAsAdmin();
            ListPage propertiesPage = new ListPage(selenium, urls, PathUtils.getPath(parentPath, "properties"));
            propertiesPage.goTo();
            propertiesPage.assertItemNotPresent("p1");
            propertiesPage.clickAdd();

            ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
            form.waitFor();
            form.finishFormElements("p1", "value", null, null, null);
            form.assertFormPresent();
            assertTextPresent("name is already in use in descendent \"" + childName + "\", please select another name");
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    public void testNameValidationDuplicateInDescendents() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String parentName = random + "-parent";
            String child1Name = random + "-child1";
            String child2Name = random + "-child2";
            String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
            String child1Path = xmlRpcHelper.insertTrivialProject(child1Name, parentName, false);
            String child2Path = xmlRpcHelper.insertTrivialProject(child2Name, parentName, false);
            Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
            property.put("name", "p1");
            xmlRpcHelper.insertConfig(PathUtils.getPath(child1Path, "properties"), property);
            xmlRpcHelper.insertConfig(PathUtils.getPath(child2Path, "properties"), property);

            loginAsAdmin();
            ListPage propertiesPage = new ListPage(selenium, urls, PathUtils.getPath(parentPath, "properties"));
            propertiesPage.goTo();
            propertiesPage.assertItemNotPresent("p1");
            propertiesPage.clickAdd();

            ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
            form.waitFor();
            form.finishFormElements("p1", "value", null, null, null);
            form.assertFormPresent();
            assertTextPresent("name is already in use in descendents [" + child1Name + ", " + child2Name + "], please select another name");
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    public void tetNameValidationDuplicateInAncestor() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            String parentName = random + "-parent";
            String childName = random + "-child";
            String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
            String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
            String parentPropertiesPath = PathUtils.getPath(parentPath, "properties");
            Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
            property.put("name", "p1");
            xmlRpcHelper.insertConfig(parentPropertiesPath, property);

            String childPropertiesPath = PathUtils.getPath(childPath, "properties");
            xmlRpcHelper.deleteConfig(PathUtils.getPath(childPropertiesPath, "p1"));

            loginAsAdmin();
            ListPage propertiesPage = new ListPage(selenium, urls, childPropertiesPath);
            propertiesPage.goTo();
            propertiesPage.assertItemPresent("p1", ListPage.ANNOTATION_HIDDEN);
            propertiesPage.clickAdd();

            ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
            form.waitFor();
            form.finishFormElements("p1", "value", null, null, null);
            form.assertFormPresent();
            assertTextPresent("name is already in use in ancestor \"" + parentName + "\", please select another name");
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }
}