package com.zutubi.pulse.web;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.pulse.committransformers.CommitMessageSupport;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.xwork.TextProviderSupport;
import com.zutubi.pulse.xwork.interceptor.Cancelable;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.utility.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 
 *
 */
public class ActionSupport extends com.opensymphony.xwork.ActionSupport implements Cancelable
{
    private static final Logger LOG = Logger.getLogger(ActionSupport.class);

    protected static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    protected static final String ERROR_MESSAGE = "javax.servlet.error.message";
    protected static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    protected static final String ERROR_EXCEPTION = "javax.servlet.error.exception";


    /**
     * Use our own text provider implementation. It would be nice if there was a way to replace
     * the textProvider used by the default action support.
     */
    private transient final TextProvider textProvider = new TextProviderSupport(getClass(), this);

    protected String cancel;
    protected AccessManager accessManager;
    protected ProjectManager projectManager;
    protected UserManager userManager;
    protected ConfigurationSecurityManager configurationSecurityManager;
    protected ChangelistDao changelistDao;
    protected String changeUrl;
    private User loggedInUser = null;

    public boolean isCancelled()
    {
        return cancel != null && !cancel.equalsIgnoreCase("false");
    }

    public void setCancel(String name)
    {
        this.cancel = name;
    }

    public Object getPrinciple()
    {
        // note, need to be careful - the returned user is likely to be
        // hopelessly out of sync if any changes have been made to the user during the
        // latest session...
        return AcegiUtils.getLoggedInUsername();
    }

    public String trimmedString(String s, int length)
    {
        return StringUtils.trimmedString(s, length);
    }

    //---( TextProvider implementation )---

    public String getText(String aTextName)
    {
        return textProvider.getText(aTextName);
    }

    public String getText(String aTextName, List args)
    {
        return textProvider.getText(aTextName, args);
    }

    public String getText(String aTextName, String defaultValue)
    {
        return textProvider.getText(aTextName, defaultValue);
    }

    public String getText(String aTextName, String defaultValue, List args)
    {
        return textProvider.getText(aTextName, defaultValue, args);
    }

    public String getText(String key, String defaultValue, List args, OgnlValueStack stack)
    {
        return textProvider.getText(key, defaultValue, args, stack);
    }

    public ResourceBundle getTexts()
    {
        return textProvider.getTexts();
    }

    public ResourceBundle getTexts(String aBundleName)
    {
        return textProvider.getTexts(aBundleName);
    }

    public boolean notNull(Object object)
    {
        return object != null;
    }

    public Locale getLocale()
    {
        return ActionContext.getContext().getLocale();
    }

    public String getPrettyTime(long time)
    {
        return TimeStamps.getPrettyTime(time);
    }

    public String getPrettyDate(long time)
    {
        return TimeStamps.getPrettyDate(time, getLocale());
    }

    public String wrapString(String s, int lineLength)
    {
        return StringUtils.wrapString(s, lineLength, null);
    }

    public String plainToHtml(String s)
    {
        return TextUtils.plainTextToHtml(s);
    }

    public String escapeSpaces(String s)
    {
        return s.replace(" ", "%20");
    }

    public String urlEncode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return s;
        }
    }

    public String jsStringEncode(String s)
    {
        return StringUtil.javaScriptStringEnc(s);
    }

    public String uriComponentEncode(String s)
    {
        return s == null ? null : StringUtils.uriComponentEncode(s);
    }

    public String htmlEncode(String s)
    {
        return s == null ? null : TextUtils.htmlEncode(s);
    }

    public String urlDecode(String s)
    {
        try
        {
            return URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return s;
        }
    }

    public boolean hasPermission(String action, Object resource)
    {
        return accessManager.hasPermission(action, resource);
    }

    public boolean hasPermission(String action, String path)
    {
        return configurationSecurityManager.hasPermission(path, action);
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public void updateChangeUrl(Project project, Revision revision)
    {
        try
        {
            ProjectConfiguration projectConfig = null;
            if (project != null)
            {
                projectConfig = projectManager.getProjectConfig(project.getId(), false);
            }
            if(revision != null && projectConfig != null && projectConfig.getChangeViewer() != null)
            {
                changeUrl = projectConfig.getChangeViewer().getChangesetURL(revision);
                return;
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }

        changeUrl = null;
    }

    public String getChangeUrl()
    {
        return changeUrl;
    }

    public void updateChangeUrl(Changelist changelist)
    {
        // We cache the URL as velocity null handling is brain dead
        try
        {
            if (changelist != null)
            {
                Revision revision = changelist.getRevision();
                if (revision != null)
                {
                    for(long id: changelistDao.getAllAffectedProjectIds(changelist))
                    {
                        ProjectConfiguration p = getProjectManager().getProjectConfig(id, false);
                        if(p != null && p.getChangeViewer() != null)
                        {
                            String url = p.getChangeViewer().getChangesetURL(revision);
                            if(url != null)
                            {
                                changeUrl = url;
                                return;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
        changeUrl = null;
    }

    public String getMessage(Object instance, String key)
    {
        return Messages.getInstance(instance).format(key);
    }

    public String getMessage(Object instance, String key, String... args)
    {
        return Messages.getInstance(instance).format(key, (Object[])args);
    }

    public CommitMessageSupport getCommitMessageSupport(Changelist changelist)
    {
        List<CommitMessageTransformerConfiguration> transformers = new LinkedList<CommitMessageTransformerConfiguration>();
        for(long projectId: changelistDao.getAllAffectedProjectIds(changelist))
        {
            Project project = projectManager.getProject(projectId, false);
            if(project != null)
            {
                transformers.addAll(project.getConfig().getCommitMessageTransformers().values());
            }
        }

        return new CommitMessageSupport(changelist.getComment(), transformers);
    }

    public User getLoggedInUser()
    {
        if (loggedInUser == null)
        {
            Object principle = getPrinciple();
            if(principle != null && principle instanceof String)
            {
                loggedInUser = userManager.getUser((String)principle);
            }
        }

        return loggedInUser;
    }

    public String execute() throws Exception
    {
        return super.execute();
    }

    public final void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public final void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public final void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public final void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }
}
