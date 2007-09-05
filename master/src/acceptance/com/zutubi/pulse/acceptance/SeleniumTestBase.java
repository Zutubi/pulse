package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import junit.framework.TestCase;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * Helper base class for web UI acceptance tests that use Selenium.
 */
public class SeleniumTestBase extends TestCase
{
    protected Selenium selenium;
    protected Urls urls;
    protected String port;
    protected String baseUrl;
    protected String random;
    protected XmlRpcHelper xmlRpcHelper;

    public SeleniumTestBase()
    {
        super();
    }
    
    public SeleniumTestBase(String baseUrl)
    {
        super();
        this.baseUrl = baseUrl;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        port = System.getProperty("pulse.port", "8080");
        xmlRpcHelper = new XmlRpcHelper(new URL("http", "localhost", Integer.parseInt(port), "/xmlrpc"));
        baseUrl = "http://localhost:" + port + "/";
        urls = new Urls("");
        random = RandomUtils.randomString(10);
        selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://localhost:" + port + "/");
        selenium.start();
    }

    protected void tearDown() throws Exception
    {
        selenium.stop();
        selenium = null;
        xmlRpcHelper = null;
        super.tearDown();
    }

    private void login(String username, String password)
    {
        goTo(urls.login());
        selenium.type("j_username", username);
        selenium.type("j_password", password);
        selenium.click("login");
        selenium.waitForPageToLoad("30000");
    }

    protected void loginAsAdmin()
    {
        login("admin", "admin");
    }

    protected void goTo(String location)
    {
        selenium.open(StringUtils.join("/", true, baseUrl, location));
    }

    protected void assertElementPresent(String id)
    {
        SeleniumUtils.assertElementPresent(selenium, id);
    }

    protected void assertTextPresent(String text)
    {
        SeleniumUtils.assertTextPresent(selenium, text);
    }

    protected void assertLinkPresent(String id)
    {
        SeleniumUtils.assertLinkPresent(selenium, id);
    }

    protected void assertFormFieldNotEmpty(String id)
    {
        SeleniumUtils.assertFormFieldNotEmpty(selenium, id);
    }

    protected void waitForElement(String id)
    {
        SeleniumUtils.waitForElement(selenium, id);
    }

    protected void waitForElement(String id, long timeout)
    {
        SeleniumUtils.waitForElement(selenium, id, timeout);
    }

    protected void addProject(String name)
    {
        addProject(name, false, "global project template", true);
    }

    protected void addProject(String name, boolean template, String parentName, boolean parentIsTemplate)
    {
        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, parentName, parentIsTemplate);
        globalPage.waitFor();
        if (template)
        {
            globalPage.clickAddTemplate();
        }
        else
        {
            globalPage.clickAdd();
        }

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        projectState.nextFormElements(name, "test description", "http://test.com/");

        SelectTypeState scmTypeState = new SelectTypeState(selenium);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.svnConfig");

        AddProjectWizard.SvnState svnState = new AddProjectWizard.SvnState(selenium);
        svnState.waitFor();
        svnState.nextFormElements("svn://localhost:3088/accept/trunk/triviant", null, null, null, null, "CLEAN_CHECKOUT");

        SelectTypeState projectTypeState = new SelectTypeState(selenium);
        projectTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.antTypeConfig");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(selenium);
        antState.waitFor();
        antState.finishFormElements(null, "build.xml", null, null);

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, name, template);
        hierarchyPage.waitFor();
        hierarchyPage.assertPresent();
    }

    protected void ensureProject(String name) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            xmlRpcHelper.ensureProject(name);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
    }

    protected String getNewestListItem(String labelsPath) throws Exception
    {
        Vector<String> labels = xmlRpcHelper.call("getConfigListing", labelsPath);
        Collections.sort(labels, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                long h1 = Long.parseLong(o1);
                long h2 = Long.parseLong(o2);
                return (int) (h1 - h2);
            }
        });
        return labels.get(labels.size() - 1);
    }
}
