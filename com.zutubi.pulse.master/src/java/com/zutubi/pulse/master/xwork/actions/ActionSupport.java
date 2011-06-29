package com.zutubi.pulse.master.xwork.actions;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.xwork.TextProviderSupport;
import com.zutubi.pulse.master.xwork.actions.project.Viewport;
import com.zutubi.pulse.master.xwork.interceptor.Cancelable;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import freemarker.template.utility.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static com.zutubi.util.Constants.UTF8;

/**
 * Base for all actions.  Includes standard i18n, encoding, security and other
 * utility methods.
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
    protected ObjectFactory objectFactory;
    private User loggedInUser = null;

    public boolean isCancelled()
    {
        return cancel != null && !cancel.equalsIgnoreCase("false");
    }

    public void setCancel(String name)
    {
        this.cancel = name;
    }

    public void doCancel()
    {
        // Do nothing by default
    }

    public String getPrinciple()
    {
        // note, need to be careful - the returned user is likely to be
        // hopelessly out of sync if any changes have been made to the user during the
        // latest session...
        return SecurityUtils.getLoggedInUsername();
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
        return WebUtils.formUrlEncode(s);
    }

    public String jsStringEncode(String s)
    {
        return StringUtil.javaScriptStringEnc(s);
    }

    public String uriComponentEncode(String s)
    {
        return s == null ? null : WebUtils.uriComponentEncode(s);
    }

    public String htmlEncode(String s)
    {
        return s == null ? null : TextUtils.htmlEncode(s);
    }

    public String urlDecode(String s)
    {
        try
        {
            return URLDecoder.decode(s, UTF8);
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

    public String getMessage(Object instance, String key)
    {
        return Messages.getInstance(instance).format(key);
    }

    public String getMessage(Object instance, String key, Object... args)
    {
        return Messages.getInstance(instance).format(key, args);
    }

    public CommitMessageSupport getCommitMessageSupport(PersistentChangelist changelist)
    {
        List<CommitMessageTransformerConfiguration> transformers = new LinkedList<CommitMessageTransformerConfiguration>();

        Set<Long> ids = changelistDao.getAllAffectedProjectIds(changelist);
        for(Project project: projectManager.getProjects(ids, false))
        {
            transformers.addAll(project.getConfig().getCommitMessageTransformers().values());
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

    protected void pauseForDramaticEffect()
    {
        try
        {
            Thread.sleep(Constants.SECOND);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }

    public Viewport loadBuildNavViewport(long buildId)
    {
        Viewport viewport = new Viewport();

        BuildViewport buildViewport = objectFactory.buildBean(BuildViewport.class,
                new Class[]{Long.TYPE}, new Object[]{buildId}
        );

        List<BuildResult> builds = buildViewport.getVisibleBuilds();
        viewport.addAll(CollectionUtils.map(builds, new Mapping<BuildResult, Viewport.Data>()
        {
            public Viewport.Data map(BuildResult result)
            {
                return new Viewport.Data(result);
            }
        }));

        // provide the details for the links.
        BuildResult nextBrokenBuild = buildViewport.getNextBrokenBuild();
        if (nextBrokenBuild != null)
        {
            viewport.setNextBroken(new Viewport.Data(nextBrokenBuild));
        }
        BuildResult nextSuccessfulBuild = buildViewport.getNextSuccessfulBuild();
        if (nextSuccessfulBuild != null)
        {
            viewport.setNextSuccessful(new Viewport.Data(nextSuccessfulBuild));
        }
        BuildResult previousBrokenBuild = buildViewport.getPreviousBrokenBuild();
        if (previousBrokenBuild != null)
        {
            viewport.setPreviousBroken(new Viewport.Data(previousBrokenBuild));
        }
        BuildResult previousSuccessfulBuild = buildViewport.getPreviousSuccessfulBuild();
        if (previousSuccessfulBuild != null)
        {
            viewport.setPreviousSuccessful(new Viewport.Data(previousSuccessfulBuild));
        }
        BuildResult latestBuild = buildViewport.getLatestBuild();
        if (latestBuild != null && latestBuild.getId() != buildId)
        {
            viewport.setLatest(new Viewport.Data(latestBuild));
        }

        return viewport;
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
