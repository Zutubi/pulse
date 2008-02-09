package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.PersonalBuildSummaryPage;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.svn.SubversionWorkingCopy;
import com.zutubi.pulse.personal.PersonalBuildClient;
import com.zutubi.pulse.personal.PersonalBuildCommand;
import com.zutubi.pulse.personal.PersonalBuildConfig;
import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Simple sanity checks for personal builds.
 */
public class PersonalBuildAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "PersonalBuildAcceptanceTest-Project";

    private File workingCopyDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        WorkingCopyFactory.registerType("svn", SubversionWorkingCopy.class);
        workingCopyDir = FileSystemUtils.createTempDir(getName(), "");

        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        SVNUpdateClient client = new SVNUpdateClient(SVNWCUtil.createDefaultAuthenticationManager(), null);
        client.doCheckout(SVNURL.parseURIDecoded("svn://localhost:3088/accept/trunk/triviant"), workingCopyDir, SVNRevision.HEAD, SVNRevision.HEAD, true);

        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        FileSystemUtils.rmdir(workingCopyDir);

        super.tearDown();
    }

    public void testPersonalBuild() throws Exception
    {
        // Edit the build.xml file so we have an outstanding change
        File buildFile = new File(workingCopyDir, "build.xml");
        String fileContents = IOUtils.fileToString(buildFile);
        fileContents = fileContents.replaceAll("sleep", "nosuchcommand");
        FileSystemUtils.createFile(buildFile, fileContents);

        createConfigFile();

        loginAsAdmin();
        goTo(urls.adminProjects());
        addProject(random);

        TestPersonalBuildUI ui = requestPersonalBuild();

        assertEquals(0, ui.getWarnings().size());
        assertEquals(0, ui.getErrors().size());
        assertTrue(ui.getStatuses().size() > 0);

        String lastStatus = ui.getStatuses().get(ui.getStatuses().size() - 1);
        assertTrue(lastStatus.startsWith("Patch accepted"));

        String[] pieces = lastStatus.split(" ");
        String number = pieces[pieces.length - 1];
        number = number.substring(0, number.length() - 1);
        long buildNumber = Long.parseLong(number);

        MyBuildsPage myBuildsPage = new MyBuildsPage(selenium, urls);
        myBuildsPage.goTo();
        assertElementPresent(MyBuildsPage.getBuildNumberId(buildNumber));
        assertElementNotPresent(MyBuildsPage.getBuildNumberId(buildNumber + 1));
        SeleniumUtils.refreshUntilText(selenium, MyBuildsPage.getBuildStatusId(buildNumber), "failure");

        PersonalBuildSummaryPage summaryPage = new PersonalBuildSummaryPage(selenium, urls, buildNumber);
        summaryPage.goTo();
        assertTextPresent("nosuchcommand");
    }

    private void createConfigFile() throws IOException
    {
        File configFile = new File(workingCopyDir, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties config = new Properties();
        config.put(PersonalBuildConfig.PROPERTY_PULSE_URL, baseUrl);
        config.put(PersonalBuildConfig.PROPERTY_PULSE_USER, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PROJECT, random);

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(configFile);
            config.store(os, null);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private TestPersonalBuildUI requestPersonalBuild()
    {
        PersonalBuildConfig config = new PersonalBuildConfig(workingCopyDir, null);
        PersonalBuildClient client = new PersonalBuildClient(config);
        TestPersonalBuildUI ui = new TestPersonalBuildUI();
        client.setUI(ui);

        PersonalBuildCommand command = new PersonalBuildCommand();
        command.execute(client);

        return ui;
    }

    private static class TestPersonalBuildUI implements PersonalBuildUI
    {
        private List<String> debugs = new LinkedList<String>();
        private List<String> statuses = new LinkedList<String>();
        private List<String> warnings = new LinkedList<String>();
        private List<String> errors = new LinkedList<String>();

        public List<String> getDebugs()
        {
            return debugs;
        }

        public List<String> getStatuses()
        {
            return statuses;
        }

        public List<String> getWarnings()
        {
            return warnings;
        }

        public List<String> getErrors()
        {
            return errors;
        }

        public void setVerbosity(Verbosity verbosity)
        {
        }

        public void debug(String message)
        {
            debugs.add(message);
        }

        public void status(String message)
        {
            statuses.add(message);
        }

        public void warning(String message)
        {
            warnings.add(message);
        }

        public void error(String message)
        {
            errors.add(message);
        }

        public void error(String message, Throwable throwable)
        {
            errors.add(message);
        }

        public void enterContext()
        {
        }

        public void exitContext()
        {
        }

        public String inputPrompt(String question)
        {
            return "";
        }

        public String inputPrompt(String prompt, String defaultResponse)
        {
            return defaultResponse;
        }

        public String passwordPrompt(String question)
        {
            return "";
        }

        public Response ynPrompt(String question, Response defaultResponse)
        {
            return defaultResponse;
        }

        public Response ynaPrompt(String question, Response defaultResponse)
        {
            return defaultResponse;
        }
    }
}
