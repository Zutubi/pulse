package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.WebClient;
import com.zutubi.pulse.acceptance.forms.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.CreateUserForm;
import com.zutubi.pulse.acceptance.forms.CvsForm;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import junit.framework.Assert;
import org.apache.xmlrpc.XmlRpcClient;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

/**
 * <class-comment/>
 */
public abstract class BaseAcceptanceTest extends ExtendedWebTestCase
{
    protected static final String TEST_CVSROOT = ":pserver:cvstester:cvs@www.cinnamonbob.com:/cvsroot";

    //---( add project wizard forms )---
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

    protected void logout()
    {
        beginAt("/");
        clickLink("logout");
    }

    protected boolean hasLinkWithText(String text) throws Exception
    {
        return tester.getDialog().getResponse().getLinkWith(text) != null;
    }

    protected Object callRemoteApi(String function, Object... args) throws Exception
    {
        URL url = new URL("http", "127.0.0.1", Integer.valueOf(port), "/xmlrpc");
        XmlRpcClient client = new XmlRpcClient(url);
        Vector<Object> argVector = new Vector<Object>();

        // login as admin
        argVector.add("admin");
        argVector.add("admin");
        String token = (String) client.execute("RemoteApi.login", argVector);

        // Actual call
        argVector.clear();
        argVector.add(token);
        argVector.addAll(Arrays.asList(args));
        Object result = client.execute("RemoteApi." + function, argVector);

        // Logout
        argVector.clear();
        argVector.add(token);
        client.execute("RemoteApi.logout", argVector);

        return result;
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
        CreateUserForm form = new CreateUserForm(tester);
        form.assertFormPresent();
        form.saveFormElements(login, name, Boolean.toString(false), password, confirm, Boolean.toString(admin));
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
        form.nextFormElements(root, module, password, "", "", "", "");
    }

    protected void submitProjectBasicsForm(String projectName, String description, String url, String scm, String type)
    {
        AddProjectWizard.Select form = new AddProjectWizard.Select(tester);
        form.assertFormPresent();
        form.nextFormElements(projectName, description, url, scm, type);
    }

    protected void ensureProject(String name) throws Exception
    {
        Vector<String> projects = (Vector<String>) callRemoteApi("getAllProjectNames");
        if(!projects.contains(name))
        {
            clickLinkWithText("projects");
            clickLink("project.add");
            submitProjectBasicsForm(name, "desc", "url", "cvs", "ant");
            submitCvsSetupForm(TEST_CVSROOT, "module", "", "");
            submitAntSetupForm();
        }
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
    protected void assertTableRowEqual(String tableSummaryOrId, int row, String[] expectedValues)
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

    protected void goTo(String relativeUrl) throws HttpException, IOException, SAXException
    {
        String baseUrl = getTestContext().getBaseUrl();

        String targetUrl = "";
        if (baseUrl.endsWith("/") && relativeUrl.startsWith("/"))
        {
            targetUrl = baseUrl + relativeUrl.substring(1);
        }
        else
        {
            targetUrl = baseUrl + relativeUrl;
        }

        tester.getDialog().getWebClient().getResponse(new GetMethodWebRequest(targetUrl));
    }
}
