package com.zutubi.pulse.acceptance;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.user.AllBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectSubscriptionConfiguration;
import com.zutubi.pulse.master.tove.config.user.SelectedBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.Condition;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.RandomUtils;

import java.util.*;

/**
 * Sanity acceptance tests for notifications.
 *
 * The tested notifications use emails since they are easily captured.
 *
 * The more details specifics of each type of notification and more complex conditions
 * are tested at the unit level.
 */
public class NotificationAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String DEFAULT_HOST = "localhost";
    private static final String EMAIL_DOMAIN = "@zutubi.com";
    private static final String DEFAULT_FROM = "testing" + EMAIL_DOMAIN;
    private static final int TIMEOUT = 30000;

    private static final int CONDITION_ALL_BUILDS = 1;
    private static final int CONDITION_SUCCESSFUL_BUILDS = 2;
    private static final int CONDITION_FAILED_BUILDS = 3;

    private SimpleSmtpServer server;
    private String random;

    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        random = getName() + "-" + RandomUtils.randomString(10);

        server = SimpleSmtpServer.start();
        ensureDefaultEmailSettings();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();

        server.stop();

        super.tearDown();
    }

    private void ensureDefaultEmailSettings() throws Exception
    {
        Hashtable<String, Object> emailSettings = xmlRpcHelper.createDefaultConfig(EmailConfiguration.class);
        emailSettings.put("host", DEFAULT_HOST);
        emailSettings.put("from", DEFAULT_FROM);
        xmlRpcHelper.saveConfig("settings/email", emailSettings, false);
    }

    /**
     * Simple sanity check to ensure that an email notification is generated
     * correctly on a successful build.
     *
     * @throws Exception on error
     */
    public void testEmailNotification() throws Exception
    {
        setupData();

        triggerAndCheckProjectA();
        clearSmtpServer();
        triggerAndCheckProjectB();
    }

    private void clearSmtpServer()
    {
        server.stop();
        server = SimpleSmtpServer.start();
    }

    private void triggerAndCheckProjectA() throws Exception
    {
        // trigger and wait for build of project a.
        String projectA = random + "projectA";
        xmlRpcHelper.triggerBuild(projectA);
        xmlRpcHelper.waitForBuildToComplete(projectA, 1, TIMEOUT);

        assertEmailsFrom("userA", "userB", "userD");
    }

    private void triggerAndCheckProjectB() throws Exception
    {
        // trigger and wait for build of project a.
        String projectB = random + "projectB";
        xmlRpcHelper.triggerBuild(projectB);
        xmlRpcHelper.waitForBuildToComplete(projectB, 1, TIMEOUT);

        assertEmailsFrom("userA", "userC", "userE");
    }

    private void assertEmailsFrom(final String... recipients)
    {
        // wait for emails to arrive.
        AcceptanceTestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return server.getReceivedEmailSize() == recipients.length;
            }
        }, TIMEOUT, "Expected "+recipients.length+" emails.");

        assertEquals(recipients.length, server.getReceivedEmailSize());
        Set<String> emailRecipients = new HashSet<String>();
        Iterator receivedEmails = server.getReceivedEmail();
        while (receivedEmails.hasNext())
        {
            SmtpMessage email = (SmtpMessage) receivedEmails.next();
            emailRecipients.add(email.getHeaderValue("To"));
        }

        for (String recipient : recipients)
        {
            assertTrue(emailRecipients.contains(random + recipient + EMAIL_DOMAIN));
        }
    }

    private void setupData() throws Exception
    {
        // Firstly, remove all existing subscriptions as we do not want them to interfer / slow things down.
        xmlRpcHelper.deleteAllConfigs("users/*/preferences/subscriptions/*");

        // user a, all builds, all projects, can view all.
        createUser(random + "userA", CONDITION_ALL_BUILDS);
        createGroup(random + "groupA", random + "userA");

        // user b, successful builds, all projects, can view all.
        createUser(random + "userB", CONDITION_SUCCESSFUL_BUILDS);
        createGroup(random + "groupB", random + "userB");

        // user c, failed builds, all projects, can view all.
        createUser(random + "userC", CONDITION_FAILED_BUILDS);
        createGroup(random + "groupC", random + "userC");

        // user d, all builds, all projects, can view project a.
        createUser(random + "userD", CONDITION_ALL_BUILDS);
        createGroup(random + "groupD", random + "userD");

        // user e, all builds, all projects, can view project b.
        createUser(random + "userE", CONDITION_ALL_BUILDS);
        createGroup(random + "groupE", random + "userE");

        // project a succeeds.
        createProject(true, random + "projectA", random + "groupA", random + "groupB", random + "groupC", random + "groupD");
        // project b fails.
        createProject(false, random + "projectB", random + "groupA", random + "groupB", random + "groupC", random + "groupE");
    }

    private void createUser(String name, int condition) throws Exception
    {
        String userPath = xmlRpcHelper.insertTrivialUser(name);
        
        // create email contact point.
        Hashtable<String, Object> contactPoint = xmlRpcHelper.createDefaultConfig(EmailContactConfiguration.class);
        contactPoint.put("name", "email");
        contactPoint.put("address", name + EMAIL_DOMAIN);
        xmlRpcHelper.insertConfig(userPath + "/preferences/contacts", contactPoint);

        // create all projects all builds subscription for email contact point.
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

    private void createProject(boolean successful, String name, String... viewableBy) throws Exception
    {
        if (successful)
        {
            xmlRpcHelper.insertSimpleProject(name, false);
        }
        else
        {
            xmlRpcHelper.insertProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
        }

        // remove all existing permissions so that we are sure they will not get in the way.
        String permissionsPath = "projects/" + name + "/permissions";
        Vector<String> listing = xmlRpcHelper.getConfigListing(permissionsPath);
        for (String path : listing)
        {
            assertTrue(xmlRpcHelper.deleteConfig(permissionsPath + "/" + path));
        }

        for (String groupName : viewableBy)
        {
            Hashtable<String, Object> acl = xmlRpcHelper.createDefaultConfig(ProjectAclConfiguration.class);
            acl.put("group", "groups/" + groupName);
            acl.put("allowedActions", new Vector(Arrays.asList(AccessManager.ACTION_VIEW)));
            xmlRpcHelper.insertConfig("projects/"+name+"/permissions", acl);
        }
    }

}
