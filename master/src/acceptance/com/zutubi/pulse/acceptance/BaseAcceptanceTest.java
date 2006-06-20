package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.WebClient;
import com.zutubi.pulse.acceptance.forms.CvsForm;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import junit.framework.Assert;

import java.io.IOException;

/**
 * <class-comment/>
 */
public abstract class BaseAcceptanceTest extends ExtendedWebTestCase
{
    //---( administrations create user form )---
    private static final String FO_USER_CREATE = "newUser.create";
    protected static final String USER_CREATE_LOGIN = "newUser.login";
    protected static final String USER_CREATE_NAME = "newUser.name";
    protected static final String USER_CREATE_PASSWORD = "newUser.password";
    protected static final String USER_CREATE_CONFIRM = "confirm";
    protected static final String USER_CREATE_ADMIN = "admin";

    //---( add project wizard forms )---
    protected static final String FO_PROJECT_BASICS = "project.basics";
    protected static final String PROJECT_BASICS_NAME = "name";
    protected static final String PROJECT_BASICS_DESCRIPTION = "description";
    protected static final String PROJECT_BASICS_URL = "url";
    protected static final String PROJECT_BASICS_SCM = "scm";
    protected static final String PROJECT_BASICS_TYPE = "type";
    protected static final String FO_ANT_SETUP = "ant.setup";
    protected static final String FO_VERSIONED_SETUP = "versioned.setup";
    protected static final String VERSIONED_SETUP_FILE = "details.pulseFileName";

    protected String port;

    public BaseAcceptanceTest()
    {
    }

    public BaseAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        port = System.getProperty("pulse.port");
        if(port == null)
        {
            port = "8080";
        }

        getTestContext().setBaseUrl("http://localhost:" + port + "/");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected void login(String user, String password)
    {
        beginAt("/login.action");
        LoginForm loginForm = new LoginForm(tester);
        loginForm.loginFormElements(user, password, "false");
    }

    protected void loginAsAdmin()
    {
        beginAt("/login.action");
        LoginForm loginForm = new LoginForm(tester);
        loginForm.loginFormElements("admin", "admin", "false");
    }

    /**
     * Assert that the cookie has been set as part of the conversation with the
     * server.
     *
     * @param cookieName
     */
    protected void assertCookieSet(String cookieName)
    {
        WebClient client = tester.getDialog().getWebClient();
        assertNotNull(client.getCookieValue(cookieName));
    }

    protected void assertCookieValue(String cookieName, String expectedValue)
    {
        WebClient client = tester.getDialog().getWebClient();
        assertEquals(expectedValue, client.getCookieValue(cookieName));
    }

    /**
     * Assert that the cookie has not been set as part of the conversation with
     * the server.
     *
     * @param cookieName
     */
    protected void assertCookieNotSet(String cookieName)
    {
        WebClient client = tester.getDialog().getWebClient();
        assertNull(client.getCookieValue(cookieName));
    }

    protected void submitCreateUserForm(String login, String name, String password, String confirm, boolean admin)
    {
        setWorkingForm(FO_USER_CREATE);
        setFormElement(USER_CREATE_LOGIN, login);
        setFormElement(USER_CREATE_NAME, name);
        setFormElement(USER_CREATE_PASSWORD, password);
        setFormElement(USER_CREATE_CONFIRM, confirm);
        if (admin)
        {
            checkCheckbox(USER_CREATE_ADMIN, "true");
        }
        else
        {
            uncheckCheckbox(USER_CREATE_ADMIN);
        }
        submit("save");
    }

    protected void navigateToUserAdministration()
    {
        gotoPage("/");
        clickLinkWithText("administration");
        clickLinkWithText("users");
    }

    protected void submitAntSetupForm()
    {
        assertFormPresent(FO_ANT_SETUP);
        setWorkingForm(FO_ANT_SETUP);
        submit("next");
    }

    protected void submitVersionedSetupForm(String file)
    {
        assertFormPresent(FO_VERSIONED_SETUP);
        setWorkingForm(FO_VERSIONED_SETUP);
        setFormElement(VERSIONED_SETUP_FILE, file);
        submit("next");
    }

    protected void submitCvsSetupForm(String root, String module, String password, String path)
    {
        CvsForm.Create form = new CvsForm.Create(tester);
        form.assertFormPresent();
        form.nextFormElements(root, module, password, "", "", "");
    }

    protected void submitProjectBasicsForm(String projectName, String description, String url, String scm, String type)
    {
        assertFormPresent(FO_PROJECT_BASICS);
        setWorkingForm(FO_PROJECT_BASICS);
        setFormElement(PROJECT_BASICS_NAME, projectName);
        setFormElement(PROJECT_BASICS_DESCRIPTION, description);
        setFormElement(PROJECT_BASICS_URL, url);
        setFormElement(PROJECT_BASICS_SCM, scm);
        setFormElement(PROJECT_BASICS_TYPE, type);
        submit("next");
    }

    public void assertAndClick(String name)
    {
        assertLinkPresent(name);
        clickLink(name);
    }

    public String getEditId(String name)
    {
        return "edit_" + name;
    }

    public boolean textInResponse(String text)
    {
        return tester.getDialog().isTextInResponse(text);
    }

    protected String getResponse() throws IOException
    {
        return tester.getDialog().getResponse().getText();
    }

    /**
     * An adaptation of the assertTableRowsEquals that allows us to assert a single row in a table.
     *
     * @param tableSummaryOrId
     * @param row
     * @param expectedValues
     */
    public void assertTableRowEqual(String tableSummaryOrId, int row, String[] expectedValues)
    {
        assertTablePresent(tableSummaryOrId);
        String[][] sparseTableCellValues = tester.getDialog().getSparseTableBySummaryOrId(tableSummaryOrId);
        if (sparseTableCellValues.length <= row)
        {
            Assert.fail("Expected row["+row+"] does not exist. Actual number of rows is " + sparseTableCellValues.length);
        }
        for (int j = 0; j < expectedValues.length; j++) {
            if (expectedValues.length != sparseTableCellValues[row].length)
                Assert.fail("Unequal number of columns for row " + row + " of table " + tableSummaryOrId +
                        ". Expected [" + expectedValues.length + "] found [" + sparseTableCellValues[row].length + "].");
            String expectedString = expectedValues[j];
            Assert.assertEquals("Expected " + tableSummaryOrId + " value at [" + row + "," + j + "] not found.",
                    expectedString, tester.getTestContext().toEncodedString(sparseTableCellValues[row][j].trim()));
        }
    }
}
