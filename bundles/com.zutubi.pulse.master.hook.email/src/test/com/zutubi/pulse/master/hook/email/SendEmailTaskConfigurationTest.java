package com.zutubi.pulse.master.hook.email;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.notifications.email.RecordingEmailService;
import com.zutubi.pulse.master.notifications.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.notifications.renderer.RenderService;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.pulse.master.notifications.renderer.TemplateInfo;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.JabberContactConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.CollectionUtils;
import org.mockito.Matchers;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SendEmailTaskConfigurationTest extends PulseTestCase
{
    private static final String EMAIL_DOMAIN = "@zutubi.com";

    private SendEmailTaskConfiguration taskConfig;
    private ProjectConfiguration projectConfig;
    private List<UserConfiguration> groupMembers = new LinkedList<UserConfiguration>();
    private RecordingEmailService emailService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectConfig = new ProjectConfiguration("test");
        taskConfig = new SendEmailTaskConfiguration();
        taskConfig.setHandle(1);

        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        doReturn(projectConfig).when(configurationProvider).getAncestorOfType(taskConfig, ProjectConfiguration.class);
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setHost("dummy");
        doReturn(emailConfiguration).when(configurationProvider).get(EmailConfiguration.class);
        doReturn(new GlobalConfiguration()).when(configurationProvider).get(GlobalConfiguration.class);

        BuildResultRenderer resultRenderer = mock(BuildResultRenderer.class);
        doReturn(new TemplateInfo("template", "display", "mimeType")).when(resultRenderer).getTemplateInfo(anyString(), anyBoolean());
        
        UserManager userManager = mock(UserManager.class);
        doReturn(groupMembers).when(userManager).getGroupMembers(Matchers.<GroupConfiguration>anyObject());

        emailService = new RecordingEmailService();

        RenderService renderService = mock(RenderService.class);
        doReturn(new RenderedResult("subject", "content", "mimeType")).when(renderService).renderResult(Matchers.<BuildResult>anyObject(), anyString(), anyString());

        MasterConfigurationManager configurationManager = mock(MasterConfigurationManager.class);
        doReturn(null).when(configurationManager).getDataDirectory();
        
        taskConfig.setConfigurationProvider(configurationProvider);
        taskConfig.setBuildResultRenderer(resultRenderer);
        taskConfig.setUserManager(userManager);
        taskConfig.setEmailService(emailService);
        taskConfig.setRenderService(renderService);
        taskConfig.setConfigurationManager(configurationManager);
    }

    public void testEmailUserContacts() throws Exception
    {
        UserConfiguration user1 = createUserWithPrimaryEmail("1");
        UserConfiguration user2 = createUserWithPrimaryEmail("2");
        projectConfig.getContacts().getUsers().addAll(asList(user1, user2));

        taskConfig.setEmailContacts(true);
        taskConfig.execute(null, createCompleteResult(), null);
        assertEmailsSentTo("1", "2");
    }

    public void testEmailGroupContacts() throws Exception
    {
        projectConfig.getContacts().getGroups().add(new UserGroupConfiguration("dummy"));
        groupMembers.add(createUserWithPrimaryEmail("1"));
        
        taskConfig.setEmailContacts(true);
        taskConfig.execute(null, createCompleteResult(), null);
        assertEmailsSentTo("1");
    }

    public void testEliminatesDuplicateContacts() throws Exception
    {
        UserConfiguration user = createUserWithPrimaryEmail("1");
        projectConfig.getContacts().getUsers().add(user);
        projectConfig.getContacts().getGroups().add(new UserGroupConfiguration("dummy"));
        groupMembers.add(user);
        
        taskConfig.setEmailContacts(true);
        taskConfig.execute(null, createCompleteResult(), null);
        assertEmailsSentTo("1");
    }

    public void testNoPrimaryContactPoint() throws Exception
    {
        projectConfig.getContacts().getUsers().add(new UserConfiguration());

        taskConfig.setEmailContacts(true);
        taskConfig.execute(null, createCompleteResult(), null);
        assertEquals(0, emailService.getEmailCount());
    }

    public void testPrimaryContactPointNotEmail() throws Exception
    {
        UserConfiguration user = new UserConfiguration();
        JabberContactConfiguration contact = new JabberContactConfiguration();
        contact.setPrimary(true);
        user.getPreferences().addContact(contact);

        projectConfig.getContacts().getUsers().add(user);

        taskConfig.setEmailContacts(true);
        taskConfig.execute(null, createCompleteResult(), null);
        assertEquals(0, emailService.getEmailCount());
    }
    
    private BuildResult createCompleteResult()
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), null, 1, false);
        result.commence();
        result.complete();
        return result;
    }

    private UserConfiguration createUserWithPrimaryEmail(String login)
    {
        UserConfiguration user = new UserConfiguration(login, login);
        EmailContactConfiguration contact = new EmailContactConfiguration("primary", login + EMAIL_DOMAIN);
        contact.setPrimary(true);
        user.getPreferences().addContact(contact);
        return user;
    }

    private void assertEmailsSentTo(String... logins)
    {
        assertEquals(1, emailService.getEmailCount());
        List<String> recipients = new LinkedList<String>(emailService.getRecipientsForEmail(0));
        Collections.sort(recipients);
        assertEquals(transform(asList(logins), new Function<String, String>()
        {
            public String apply(String s)
            {
                return s + EMAIL_DOMAIN;
            }
        }), recipients);
    }
}
