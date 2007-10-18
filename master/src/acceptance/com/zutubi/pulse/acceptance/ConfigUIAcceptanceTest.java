package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.DeleteConfirmPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.model.ProjectManager;

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

}
