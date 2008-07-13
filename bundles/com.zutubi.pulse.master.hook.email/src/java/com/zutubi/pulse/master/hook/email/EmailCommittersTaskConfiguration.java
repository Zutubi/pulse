package com.zutubi.pulse.master.hook.email;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.ResultNotifier;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.tove.config.project.hooks.BuildHookTaskConfiguration;
import com.zutubi.pulse.tove.config.project.hooks.CompatibleHooks;
import com.zutubi.pulse.tove.config.project.hooks.ManualBuildHookConfiguration;
import com.zutubi.pulse.tove.config.project.hooks.PostBuildHookConfiguration;
import com.zutubi.pulse.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * A build hook task that emails users that committed changes that affected
 * the build.
 */
@SymbolicName("zutubi.emailCommittersTaskConfig")
@Form(fieldOrder = {"emailDomain", "template", "ignorePulseUsers"})
@CompatibleHooks({ManualBuildHookConfiguration.class, PostBuildHookConfiguration.class})
@Wire
public class EmailCommittersTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    @Required
    private String emailDomain;
    @Select(optionProvider = "com.zutubi.pulse.tove.config.user.SubscriptionTemplateOptionProvider")
    private String template;
    private boolean ignorePulseUsers;

    private BuildResultRenderer buildResultRenderer;
    private ConfigurationProvider configurationProvider;
    private BuildManager buildManager;
    private UserManager userManager;

    public String getEmailDomain()
    {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain)
    {
        this.emailDomain = emailDomain;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public boolean isIgnorePulseUsers()
    {
        return ignorePulseUsers;
    }

    public void setIgnorePulseUsers(boolean ignorePulseUsers)
    {
        this.ignorePulseUsers = ignorePulseUsers;
    }

    public void execute(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode) throws Exception
    {
        if (!buildResult.completed())
        {
            throw new PulseException("Email build hook task can only be applied post-build");
        }

        EmailConfiguration emailConfiguration = configurationProvider.get(EmailConfiguration.class);
        if (!TextUtils.stringSet(emailConfiguration.getHost()))
        {
            throw new PulseException("Cannot execute email build hook task as no SMTP host is configured.");
        }

        GlobalConfiguration globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
        List<String> emails = getEmails(buildResult);
        if (emails.size() > 0)
        {
            ResultNotifier.RenderedResult rendered = ResultNotifier.renderResult(buildResult, globalConfiguration.getBaseUrl(), buildManager, buildResultRenderer, template);
            String mimeType = buildResultRenderer.getTemplateInfo(template, buildResult.isPersonal()).getMimeType();
            String subject = rendered.getSubject();
            if (TextUtils.stringSet(emailConfiguration.getSubjectPrefix()))
            {
                subject = emailConfiguration.getSubjectPrefix() + " " + subject;
            }

            EmailContactConfiguration.sendMail(emails, emailConfiguration, subject, mimeType, rendered.getContent());
        }
    }

    private List<String> getEmails(BuildResult result)
    {
        List<String> emails = new LinkedList<String>();
        for (Changelist change : buildManager.getChangesForBuild(result))
        {
            String user = change.getUser();
            if (TextUtils.stringSet(user) && (!ignorePulseUsers || userManager.getUser(user) == null))
            {
                emails.add(StringUtils.join("@", true, user, emailDomain));
            }
        }

        return emails;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}

