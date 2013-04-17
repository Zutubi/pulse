package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.CleanupForm;
import com.zutubi.pulse.acceptance.forms.admin.RetainForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.CleanupRulesPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;

import static com.zutubi.pulse.acceptance.Constants.Project.Cleanup.*;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * The acceptance tests that run through the cleanup web ui.
 */
public class CleanupUIAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

         rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();

        super.tearDown();
    }

    public void testCreateNewCleanupRule() throws Exception
    {
        CleanupRulesPage cleanupRulesPage = addRuleOfType("zutubi.cleanupConfig");

        CleanupForm cleanup = getBrowser().createForm(CleanupForm.class);
        cleanup.waitFor();
        cleanup.finishNamedFormElements(asPair(NAME, "new rule"), asPair(RETAIN, "1"), asPair(CLEANUP_ALL, "true"));

        cleanupRulesPage.openAndWaitFor();
        cleanupRulesPage.waitFor();
        assertTrue(cleanupRulesPage.isItemPresent("new rule"));
        assertEquals("remove everything for all builds after 1 build", cleanupRulesPage.getSummary(0));
    }


    public void testCreateNewRetainRule() throws Exception
    {
        CleanupRulesPage cleanupRulesPage = addRuleOfType("zutubi.retainConfig");

        RetainForm retain = getBrowser().createForm(RetainForm.class);
        retain.waitFor();
        retain.finishNamedFormElements(asPair(NAME, "new rule"), asPair(RETAIN, "7"), asPair(UNIT, "DAYS"));

        cleanupRulesPage.openAndWaitFor();
        cleanupRulesPage.waitFor();
        assertTrue(cleanupRulesPage.isItemPresent("new rule"));
        assertEquals("retain all builds for up to 7 days", cleanupRulesPage.getSummary(0));
    }

    private CleanupRulesPage addRuleOfType(String symbolicName) throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);

        getBrowser().loginAsAdmin();

        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, random, false);

        CleanupRulesPage cleanupRulesPage = projectPage.clickCleanupAndWait();
        cleanupRulesPage.clickAdd();

        SelectTypeState typeSelect = new SelectTypeState(getBrowser());
        typeSelect.waitFor();
        typeSelect.nextFormElements(symbolicName);
        return cleanupRulesPage;
    }
}
