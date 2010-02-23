package com.zutubi.pulse.acceptance;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PostBuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.user.AllBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectSubscriptionConfiguration;
import com.zutubi.pulse.master.tove.config.user.SelectedBuildsConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.Mapping;
import com.zutubi.util.RandomUtils;

import java.util.*;

import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.core.engine.api.ResultState;

import static java.util.Arrays.asList;

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

        ensureDefaultEmailSettings();
        server = SimpleSmtpServer.start(DEFAULT_PORT);
    }

    protected void tearDown() throws Exception
    {
        // Remove subscriptions so that subsequent tests are not affected.
        xmlRpcHelper.deleteAllConfigs("users/*/preferences/subscriptions/*");
        xmlRpcHelper.logout();

        stopSmtpServer();

        super.tearDown();
    }

    private void stopSmtpServer() throws InterruptedException
    {
        server.stop();
        Thread.sleep(1000);
    }

    private void clearSmtpServer() throws InterruptedException
    {
        stopSmtpServer();
        server = SimpleSmtpServer.start(DEFAULT_PORT);
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

    private void triggerAndCheckSuccessfulBuild() throws Exception
    {
        String projectName = random + "project" + PROJECT_SUCCESS;
        int buildNumber = xmlRpcHelper.runBuild(projectName);
        assertEquals(ResultState.SUCCESS, xmlRpcHelper.getBuildStatus(projectName, buildNumber));
        assertIndividualEmailsTo(BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_PROJECT_SUCCESS);
    }

    private void triggerAndCheckFailedBuild() throws Exception
    {
        String projectName = random + "project" + PROJECT_FAIL;
        int buildNumber = xmlRpcHelper.runBuild(projectName);
        assertEquals(ResultState.FAILURE, xmlRpcHelper.getBuildStatus(projectName, buildNumber));
        assertIndividualEmailsTo(BUILDS_ALL, BUILDS_FAILED, BUILDS_PROJECT_FAIL);
    }

    public void testSendEmailHook() throws Exception
    {
        String user1 = random + "-user1";
        String user2 = random + "-user2";
        String group = random + "-group";
        String project = random + "-project";

        String user1Path = insertUserWithPrimaryContact(user1);
        insertUserWithPrimaryContact(user2);
        String groupPath = createGroup(group, user2);

        String projectPath = xmlRpcHelper.insertSimpleProject(project);
        String contactsPath = PathUtils.getPath(projectPath, Constants.Project.CONTACTS);
        Hashtable<String, Object> contacts = xmlRpcHelper.getConfig(contactsPath);
        contacts.put("groups", new Vector<String>(asList(groupPath)));
        contacts.put("users", new Vector<String>(asList(user1Path)));
        xmlRpcHelper.saveConfig(contactsPath, contacts, false);

        Hashtable<String, Object> hook = xmlRpcHelper.createDefaultConfig(PostBuildHookConfiguration.class);
        hook.put("name", random);
        Hashtable<String, Object> task = xmlRpcHelper.createDefaultConfig("zutubi.sendEmailTaskConfig");
        task.put("template", "plain-text-email");
        task.put("emailContacts", true);
        hook.put("task", task);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, Constants.Project.HOOKS), hook);

        xmlRpcHelper.runBuild(project);

        assertEmailsTo(1, user1, user2);
    }

    private String insertUserWithPrimaryContact(String login) throws Exception
    {
        String userPath = xmlRpcHelper.insertTrivialUser(login);
        Hashtable<String, Object> emailContact = xmlRpcHelper.createDefaultConfig(EmailContactConfiguration.class);
        emailContact.put("address", login + EMAIL_DOMAIN);
        emailContact.put("name", "primary email");
        String contactPath = xmlRpcHelper.insertConfig(PathUtils.getPath(userPath, "preferences", "contacts"), emailContact);
        xmlRpcHelper.doConfigAction(contactPath, ContactConfigurationActions.ACTION_MARK_PRIMARY);
        return userPath;
    }

    private void assertIndividualEmailsTo(String... suffixes)
    {
        assertEmailsTo(suffixes.length, CollectionUtils.mapToArray(suffixes, new Mapping<String, String>()
        {
            public String map(String s)
            {
                return random + "user" + s;
            }
        }, new String[suffixes.length]));
    }

    private void assertEmailsTo(final int emailCount, final String... recipients)
    {
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return server.getReceivedEmailSize() == emailCount;
            }
        }, EMAIL_TIMEOUT, " " + emailCount + " emails.");

        assertEquals(emailCount, server.getReceivedEmailSize());
        Set<String> emailRecipients = new HashSet<String>();
        Iterator receivedEmails = server.getReceivedEmail();
        while (receivedEmails.hasNext())
        {
            SmtpMessage email = (SmtpMessage) receivedEmails.next();
            emailRecipients.addAll(asList(email.getHeaderValues("To")));
        }

        for (String login : recipients)
        {
            assertTrue(emailRecipients.contains(login + EMAIL_DOMAIN));
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

    private String createGroup(String group, String... users) throws Exception
    {
        return xmlRpcHelper.insertGroup(group, CollectionUtils.map(users, new Mapping<String, String>()
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
            acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_VIEW)));
            xmlRpcHelper.insertConfig("projects/" + name + "/permissions", acl);
        }
    }
}
