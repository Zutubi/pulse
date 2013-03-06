package com.zutubi.pulse.acceptance;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.base.Function;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
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
import com.zutubi.util.Condition;
import com.zutubi.util.RandomUtils;

import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import static java.util.Arrays.asList;

/**
 * Sanity acceptance tests for notifications.
 * <p/>
 * The tested notifications use emails since they are easily captured.
 * <p/>
 * The specifics of each type of notification and more complex conditions
 * are tested at the unit level.
 */
public class NotificationAcceptanceTest extends AcceptanceTestBase
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

    private static final String BUILDS_ALL = "all";
    private static final String BUILDS_SUCCESSFUL = "success";
    private static final String BUILDS_FAILED = "failed";
    private static final String BUILDS_PROJECT_SUCCESS = "project-success";
    private static final String BUILDS_PROJECT_FAIL = "project-fail";
    private static final String BUILDS_LABEL_SUCCESS = "label-success";

    private static final String PROJECT_SUCCESS = "S";
    private static final String PROJECT_FAIL = "F";

    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        random = getName() + "-" + RandomUtils.randomString(10);

        ensureDefaultEmailSettings();
        server = SimpleSmtpServer.start(DEFAULT_PORT);
    }

    protected void tearDown() throws Exception
    {
        // Remove subscriptions so that subsequent tests are not affected.
        rpcClient.RemoteApi.deleteAllConfigs("users/*/preferences/subscriptions/*");
        rpcClient.logout();

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
        Hashtable<String, Object> emailSettings = rpcClient.RemoteApi.createDefaultConfig(EmailConfiguration.class);
        emailSettings.put("host", DEFAULT_HOST);
        emailSettings.put("from", DEFAULT_FROM);
        emailSettings.put("customPort", true);
        emailSettings.put("port", DEFAULT_PORT);
        rpcClient.RemoteApi.saveConfig("settings/email", emailSettings, false);
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
        int buildNumber = rpcClient.RemoteApi.runBuild(projectName);
        assertEquals(ResultState.SUCCESS, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));
        assertIndividualEmailsTo(BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_PROJECT_SUCCESS, BUILDS_LABEL_SUCCESS);
    }

    private void triggerAndCheckFailedBuild() throws Exception
    {
        String projectName = random + "project" + PROJECT_FAIL;
        int buildNumber = rpcClient.RemoteApi.runBuild(projectName);
        assertEquals(ResultState.FAILURE, rpcClient.RemoteApi.getBuildStatus(projectName, buildNumber));
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

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project);
        String contactsPath = PathUtils.getPath(projectPath, Constants.Project.CONTACTS);
        Hashtable<String, Object> contacts = rpcClient.RemoteApi.getConfig(contactsPath);
        contacts.put("groups", new Vector<String>(asList(groupPath)));
        contacts.put("users", new Vector<String>(asList(user1Path)));
        rpcClient.RemoteApi.saveConfig(contactsPath, contacts, false);

        Hashtable<String, Object> hook = rpcClient.RemoteApi.createDefaultConfig(PostBuildHookConfiguration.class);
        hook.put("name", random);
        Hashtable<String, Object> task = rpcClient.RemoteApi.createDefaultConfig("zutubi.sendEmailTaskConfig");
        task.put("template", "plain-text-email");
        task.put("emailContacts", true);
        hook.put("task", task);
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, Constants.Project.HOOKS), hook);

        rpcClient.RemoteApi.runBuild(project);

        assertEmailsTo(1, user1, user2);
    }

    public void testDeleteSubscribedProject() throws Exception
    {
        String discerningUserLogin = random + "-user-discerning";
        String promiscuousUserLogin = random + "-user-promiscuous";
        String subscribedProject = random + "-project-subscribed";
        String notSubscribedProject = random + "-project-not-subscribed";

        String subscribedProjectPath = rpcClient.RemoteApi.insertSimpleProject(subscribedProject);
        rpcClient.RemoteApi.insertSimpleProject(notSubscribedProject);
        createUserWithSubscription(discerningUserLogin, subscribedProject, null, CONDITION_ALL_BUILDS);
        createUserWithSubscription(promiscuousUserLogin, null, null, CONDITION_ALL_BUILDS);

        rpcClient.RemoteApi.runBuild(subscribedProject);
        assertEmailsTo(2, discerningUserLogin, promiscuousUserLogin);

        clearSmtpServer();

        rpcClient.RemoteApi.deleteConfig(subscribedProjectPath);
        rpcClient.RemoteApi.runBuild(notSubscribedProject);
        assertEmailsTo(1, promiscuousUserLogin);
    }

    private String insertUserWithPrimaryContact(String login) throws Exception
    {
        String userPath = rpcClient.RemoteApi.insertTrivialUser(login);
        Hashtable<String, Object> emailContact = rpcClient.RemoteApi.createDefaultConfig(EmailContactConfiguration.class);
        emailContact.put("address", login + EMAIL_DOMAIN);
        emailContact.put("name", "primary email");
        String contactPath = rpcClient.RemoteApi.insertConfig(PathUtils.getPath(userPath, "preferences", "contacts"), emailContact);
        rpcClient.RemoteApi.doConfigAction(contactPath, ContactConfigurationActions.ACTION_MARK_PRIMARY);
        return userPath;
    }

    private void assertIndividualEmailsTo(String... suffixes)
    {
        assertEmailsTo(suffixes.length, transform(asList(suffixes), new Function<String, String>()
        {
            public String apply(String s)
            {
                return random + "user" + s;
            }
        }));
    }

    private void assertEmailsTo(final int emailCount, final String... recipients)
    {
        assertEmailsTo(emailCount, asList(recipients));
    }

    private void assertEmailsTo(final int emailCount, final Collection<String> recipients)
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
        rpcClient.RemoteApi.deleteAllConfigs("users/*/preferences/subscriptions/*");

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

        // all builds, by successful label
        createUserAndGroup(BUILDS_LABEL_SUCCESS, getLabelForSuffix(PROJECT_SUCCESS), CONDITION_ALL_BUILDS);

        createProject(true, PROJECT_SUCCESS, BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_FAILED, BUILDS_PROJECT_SUCCESS, BUILDS_LABEL_SUCCESS);
        createProject(false, PROJECT_FAIL, BUILDS_ALL, BUILDS_SUCCESSFUL, BUILDS_FAILED, BUILDS_PROJECT_FAIL, BUILDS_LABEL_SUCCESS);
    }

    void createUserAndGroup(String nameSuffix, int condition) throws Exception
    {
        createUserAndGroup(nameSuffix, null, condition);
    }

    void createUserAndGroup(String nameSuffix, String label, int condition) throws Exception
    {
        createUserWithSubscription(random + "user" + nameSuffix, null, label, condition);
        createGroup(random + "group" + nameSuffix, random + "user" + nameSuffix);
    }

    private void createUserWithSubscription(String name, String project, String label, int condition) throws Exception
    {
        String userPath = rpcClient.RemoteApi.insertTrivialUser(name);

        // create email contact point.
        Hashtable<String, Object> contactPoint = rpcClient.RemoteApi.createDefaultConfig(EmailContactConfiguration.class);
        contactPoint.put("name", "email");
        contactPoint.put("address", name + EMAIL_DOMAIN);
        rpcClient.RemoteApi.insertConfig(userPath + "/preferences/contacts", contactPoint);

        insertProjectSubscription(userPath, project, label, condition);
    }

    private void insertProjectSubscription(String userPath, String project, String label, int condition) throws Exception
    {
        Hashtable<String, Object> projectSubscription = rpcClient.RemoteApi.createDefaultConfig(ProjectSubscriptionConfiguration.class);
        projectSubscription.put("name", "a subscription");
        projectSubscription.put("allProjects", project == null && label == null);
        if (project != null)
        {
            projectSubscription.put("projects", new Vector<String>(asList("projects/" + project)));
        }
        if (label != null)
        {
            projectSubscription.put("labels", new Vector<String>(asList(label)));
        }
        projectSubscription.put("contact", userPath + "/preferences/contacts/email");
        projectSubscription.put("template", "plain-text-email");

        switch (condition)
        {
            case CONDITION_ALL_BUILDS:
                projectSubscription.put("condition", rpcClient.RemoteApi.createDefaultConfig(AllBuildsConditionConfiguration.class));
                break;
            case CONDITION_SUCCESSFUL_BUILDS:
                Hashtable<String, Object> successfulCondition = rpcClient.RemoteApi.createDefaultConfig(CustomConditionConfiguration.class);
                successfulCondition.put("customCondition", NotifyConditionFactory.SUCCESS);
                projectSubscription.put("condition", successfulCondition);
                break;
            case CONDITION_FAILED_BUILDS:
                Hashtable<String, Object> failedCondition = rpcClient.RemoteApi.createDefaultConfig(SelectedBuildsConditionConfiguration.class);
                failedCondition.put("unsuccessful", Boolean.TRUE);
                projectSubscription.put("condition", failedCondition);
                break;
        }

        rpcClient.RemoteApi.insertConfig(userPath + "/preferences/subscriptions", projectSubscription);
    }

    private String createGroup(String group, String... users) throws Exception
    {
        return rpcClient.RemoteApi.insertGroup(group, transform(asList(users), new Function<String, String>()
        {
            public String apply(String name)
            {
                return "users/" + name;
            }
        }));
    }

    private void createProject(boolean successful, String nameSuffix, String... viewableBy) throws Exception
    {
        String name = random + "project" + nameSuffix;
        String projectPath;
        if (successful)
        {
            projectPath = rpcClient.RemoteApi.insertSimpleProject(name, false);
        }
        else
        {
            projectPath = rpcClient.RemoteApi.insertSingleCommandProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());
        }

        // remove all existing permissions so that we are sure they will not get in the way.
        String permissionsPath = "projects/" + name + "/permissions";
        Vector<String> listing = rpcClient.RemoteApi.getConfigListing(permissionsPath);
        for (String path : listing)
        {
            assertTrue(rpcClient.RemoteApi.deleteConfig(permissionsPath + "/" + path));
        }

        for (String groupNameSuffix : viewableBy)
        {
            Hashtable<String, Object> acl = rpcClient.RemoteApi.createDefaultConfig(ProjectAclConfiguration.class);
            acl.put("group", "groups/" + random + "group" + groupNameSuffix);
            acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_VIEW)));
            rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, "permissions"), acl);
        }

        Hashtable<String, Object> labelConfig = rpcClient.RemoteApi.createDefaultConfig(LabelConfiguration.class);
        labelConfig.put("label", getLabelForSuffix(nameSuffix));
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, "labels"), labelConfig);
    }

    private String getLabelForSuffix(String nameSuffix)
    {
        return random + "label" + nameSuffix;
    }
}
