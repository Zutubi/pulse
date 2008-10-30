package com.zutubi.pulse.master.hook.email;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.ResultNotifier;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookTaskConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.CompatibleHooks;
import com.zutubi.pulse.master.tove.config.project.hooks.ManualBuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PostBuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
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
    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.user.SubscriptionTemplateOptionProvider")
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

            EmailContactConfiguration.sendMail(emails, emailConfiguration, subject, mimeType, rendered.getContent());
        }
    }

    private List<String> getEmails(BuildResult result)
    {
        List<String> emails = new LinkedList<String>();
        for (PersistentChangelist change : buildManager.getChangesForBuild(result))
        {
            String user = change.getAuthor();
            if (TextUtils.stringSet(user) && (!ignorePulseUsers || userManager.getUser(user) == null))
            {
                emails.add(StringUtils.join("@", true, user, emailDomain));
            }
        }

        final List<String> filtered = new LinkedList<String>();
        CollectionUtils.filter(emails, new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return !filtered.contains(s);
            }
        }, filtered);

        return filtered;
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

