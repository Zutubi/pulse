package com.zutubi.pulse.acceptance;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.master.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.user.AllBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectSubscriptionConfiguration;
import com.zutubi.pulse.master.tove.config.user.SelectedBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.Mapping;
import com.zutubi.util.RandomUtils;

import java.util.*;

/**
 * Sanity acceptance tests for notifications.
 * <p/>
 * The tested notifications use emails since they are easily captured.
 * <p/>
 * The specifics of each type of notification and more complex conditions
 * are tested at the unit level.
 */
public class NotificationAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String DEFAULT_HOST = "localhost";
    private static final String EMAIL_DOMAIN = "@zutubi.com";
    private static final int DEFAULT_PORT = 2525;
    private static final String DEFAULT_FROM = "testing" + EMAIL_DOMAIN;
    private static final int EMAIL_TIMEOUT = 30000;

    private static final int CONDITION_ALL_BUILDS = 1;
    private static final int CONDITION_SUCCESSFUL_BUILDS = 2;
    private static final int CONDITION_FAILED_BUILDS = 3;

    private SimpleSmtpServer server;
    private String random;

    private static final String BUILDS_ALL = "A";
    private static final String BUILDS_SUCCESSFUL = "B";
    private static final String BUILDS_FAILED = "C";
    private static final String BUILDS_PROJECT_SUCCESS = "D";
    private static final String BUILDS_PROJECT_FAIL = "E";

    private static final String PROJECT_SUCCESS = "S";
    private static final String PROJECT_FAIL = "F";

    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        random = getName() + "-" + RandomUtils.randomString(10);

        server = SimpleSmtpServer.start(DEFAULT_PORT);
        ensureDefaultEmailSettings();
    }

    protected void tearDown() throws Exception
    {
        // Remove subscriptions so that subsequent tests are not affected.
        xmlRpcHelper.deleteAllConfigs("users/*/preferences/subscriptions/*");
        xmlRpcHelper.logout();

        server.stop();

        super.tearDown();
    }

    private void ensureDefaultEmailSettings() throws Exception
    {
        Hashtable<String, Object> emailSettings = xmlRpcHelper.createDefaultConfig(EmailConfiguration.class);
        emailSettings.put("host", DEFAULT_HOST);
        emailSettings.put("from", DEFAULT_FROM);
        emailSettings.put("customPort", true);
        emailSettings.put("port", DEFAULT_PORT);
        xmlRpcHelper.saveConfig("settings/email", emailSettings, false);
    }

    public void testEmailNotification() throws Exception
    {
        setupData();

        triggerAndCheckSuccessfulBuild();
        clearSmtpServer();
        triggerAndCheckFailedBuild();
    }

    private void clearSmtpServer()
    {
        server.stop();
        server = SimpleSmtpServer.start(DEFAULT_PORT);
    }

    private void triggerAndCheckSuccessfulBuild() throws Exception
    {
        String projectName = random + "project" + PROJECT_SUCCESS;
        int buildNumber = xmlRpcHelper.runBuild(projectName);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(projectName, buildNumber);
        assertEquals("success", build.get("status"));
        assertEmailsFrom(BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_PROJECT_SUCCESS);
    }

    private void triggerAndCheckFailedBuild() throws Exception
    {
        String projectName = random + "project" + PROJECT_FAIL;
        int buildNumber = xmlRpcHelper.runBuild(projectName);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(projectName, buildNumber);
        assertEquals("failure", build.get("status"));
        assertEmailsFrom(BUILDS_ALL, BUILDS_FAILED, BUILDS_PROJECT_FAIL);
    }

    private void assertEmailsFrom(final String... recipients)
    {
        // wait for emails to arrive.
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return server.getReceivedEmailSize() == recipients.length;
            }
        }, EMAIL_TIMEOUT, "Expected " + recipients.length + " emails.");

        assertEquals(recipients.length, server.getReceivedEmailSize());
        Set<String> emailRecipients = new HashSet<String>();
        Iterator receivedEmails = server.getReceivedEmail();
        while (receivedEmails.hasNext())
        {
            SmtpMessage email = (SmtpMessage) receivedEmails.next();
            emailRecipients.add(email.getHeaderValue("To"));
        }

        for (String nameSuffix : recipients)
        {
            assertTrue(emailRecipients.contains(random + "user" + nameSuffix + EMAIL_DOMAIN));
        }
    }

    private void setupData() throws Exception
    {
        // Firstly, remove all existing subscriptions as we do not want them to interfer / slow things down.
        xmlRpcHelper.deleteAllConfigs("users/*/preferences/subscriptions/*");

        // all builds, all projects, can view all.
        createUserAndGroup(BUILDS_ALL, CONDITION_ALL_BUILDS);

        // successful builds, all projects, can view all.
        createUserAndGroup(BUILDS_SUCCESSFUL, CONDITION_SUCCESSFUL_BUILDS);

        // failed builds, all projects, can view all.
        createUserAndGroup(BUILDS_FAILED, CONDITION_FAILED_BUILDS);

        // all builds, all projects, can view project a.
        createUserAndGroup(BUILDS_PROJECT_SUCCESS, CONDITION_ALL_BUILDS);

        // all builds, all projects, can view project b.
        createUserAndGroup(BUILDS_PROJECT_FAIL, CONDITION_ALL_BUILDS);

        createProject(true, PROJECT_SUCCESS, BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_FAILED, BUILDS_PROJECT_SUCCESS);
        createProject(false, PROJECT_FAIL, BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_FAILED, BUILDS_PROJECT_FAIL);
    }

    void createUserAndGroup(String nameSuffix, int condition) throws Exception
    {
        createUser(random + "user" + nameSuffix, condition);
        createGroup(random + "group" + nameSuffix, random + "user" + nameSuffix);
    }

    private void createUser(String name, int condition) throws Exception
    {
        String userPath = xmlRpcHelper.insertTrivialUser(name);

        // create email contact point.
        Hashtable<String, Object> contactPoint = xmlRpcHelper.createDefaultConfig(EmailContactConfiguration.class);
        contactPoint.put("name", "email");
        contactPoint.put("address", name + EMAIL_DOMAIN);
        xmlRpcHelper.insertConfig(userPath + "/preferences/contacts", contactPoint);

        Hashtable<String, Object> projectSubscription = xmlRpcHelper.createDefaultConfig(ProjectSubscriptionConfiguration.class);
        projectSubscription.put("name", "all projects");
        projectSubscription.put("projects", new Vector());
        projectSubscription.put("contact", userPath + "/preferences/contacts/email");
        projectSubscription.put("template", "plain-text-email");

        switch (condition)
        {
            case CONDITION_ALL_BUILDS:
                projectSubscription.put("condition", xmlRpcHelper.createDefaultConfig(AllBuildsConditionConfiguration.class));
                break;
            case CONDITION_SUCCESSFUL_BUILDS:
                Hashtable<String, Object> successfulCondition = xmlRpcHelper.createDefaultConfig(CustomConditionConfiguration.class);
                successfulCondition.put("customCondition", NotifyConditionFactory.SUCCESS);
                projectSubscription.put("condition", successfulCondition);
                break;
            case CONDITION_FAILED_BUILDS:
                Hashtable<String, Object> failedCondition = xmlRpcHelper.createDefaultConfig(SelectedBuildsConditionConfiguration.class);
                failedCondition.put("unsuccessful", Boolean.TRUE);
                projectSubscription.put("condition", failedCondition);
                break;
        }

        xmlRpcHelper.insertConfig(userPath + "/preferences/subscriptions", projectSubscription);
    }

    private void createGroup(String group, String... users) throws Exception
    {
        xmlRpcHelper.insertGroup(group, CollectionUtils.map(users, new Mapping<String, String>()
        {
            public String map(String name)
            {
                return "users/" + name;
            }
        }));
    }

    private void createProject(boolean successful, String nameSuffix, String... viewableBy) throws Exception
    {
        String name = random + "project" + nameSuffix;
        if (successful)
        {
            xmlRpcHelper.insertSimpleProject(name, false);
        }
        else
        {
            xmlRpcHelper.insertSingleCommandProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
        }

        // remove all existing permissions so that we are sure they will not get in the way.
        String permissionsPath = "projects/" + name + "/permissions";
        Vector<String> listing = xmlRpcHelper.getConfigListing(permissionsPath);
        for (String path : listing)
        {
            assertTrue(xmlRpcHelper.deleteConfig(permissionsPath + "/" + path));
        }

        for (String groupNameSuffix : viewableBy)
        {
            Hashtable<String, Object> acl = xmlRpcHelper.createDefaultConfig(ProjectAclConfiguration.class);
            acl.put("group", "groups/"+random + "group" + groupNameSuffix);
            acl.put("allowedActions", new Vector<String>(Arrays.asList(AccessManager.ACTION_VIEW)));
            xmlRpcHelper.insertConfig("projects/" + name + "/permissions", acl);
        }
    }

}
