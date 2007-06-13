package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.ResultNotifier;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build action that sends an email to users that committed changes
 * that affected the build.  This is especially useful when the Pulse admin
 * wants everyone to get mails despite them potentially not having a Pulse
 * account.
 */
public class EmailCommittersPostBuildAction extends PostBuildAction
{
    private static final Logger LOG = Logger.getLogger(EmailCommittersPostBuildAction.class);
    
    private String emailDomain;
    private String template;
    private boolean ignorePulseUsers;

    private BuildResultRenderer buildResultRenderer;
    private MasterConfigurationManager configurationManager;
    private BuildManager buildManager;
    private UserManager userManager;

    public EmailCommittersPostBuildAction()
    {
    }

    public EmailCommittersPostBuildAction(String name, List<BuildSpecification> specifications, List<ResultState> states, boolean failOnError, String emailDomain, String template, boolean ignorePulseUsers)
    {
        super(name, specifications, states, failOnError);
        this.emailDomain = emailDomain;
        this.template = template;
        this.ignorePulseUsers = ignorePulseUsers;
    }

    protected void internalExecute(BuildResult result, RecipeResultNode recipe, List<ResourceProperty> properties)
    {
        try
        {
            MasterConfiguration appConfig = configurationManager.getAppConfig();
            if (TextUtils.stringSet(appConfig.getSmtpHost()))
            {
                List<String> emails = getEmails(result);
                if (emails.size() > 0)
                {
                    ResultNotifier.RenderedResult rendered = ResultNotifier.renderResult(result, appConfig.getBaseUrl(), buildManager, buildResultRenderer, template);
                    String mimeType = buildResultRenderer.getTemplateInfo(template, result.isPersonal()).getMimeType();
                    String subject = rendered.getSubject();
                    if(TextUtils.stringSet(appConfig.getSmtpPrefix()))
                    {
                        subject = appConfig.getSmtpPrefix() + " " + subject;
                    }
                    
                    EmailContactPoint.sendMail(emails, subject, mimeType, rendered.getContent(), appConfig.getSmtpHost(), appConfig.getSmtpPort(), appConfig.getSmtpSSL(), appConfig.getSmtpUsername(), appConfig.getSmtpPassword(), appConfig.getSmtpLocalhost(), appConfig.getSmtpFrom());
                }
            }
            else
            {
                LOG.warning("Ignoring email post build action as not SMTP host is configured.");
            }
        }
        catch (Exception e)
        {
            addError(e.getMessage());
        }
    }

    private List<String> getEmails(BuildResult result)
    {
        List<String> emails = new LinkedList<String>();
        for(Changelist change: buildManager.getChangesForBuild(result))
        {
            String user = change.getUser();
            if(TextUtils.stringSet(user) && (!ignorePulseUsers || userManager.getUser(user) == null))
            {
                emails.add(StringUtils.join("@", true, user, emailDomain));
            }
        }
        
        return emails;
    }

    public String getType()
    {
        return "email committers";
    }

    public PostBuildAction copy()
    {
        EmailCommittersPostBuildAction copy = new EmailCommittersPostBuildAction();
        copyCommon(copy);
        copy.emailDomain = emailDomain;
        copy.template = template;
        copy.ignorePulseUsers = ignorePulseUsers;
        return copy;
    }

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

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
