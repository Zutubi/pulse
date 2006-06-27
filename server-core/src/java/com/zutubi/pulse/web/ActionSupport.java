package com.zutubi.pulse.web;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.xwork.TextProviderSupport;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 
 *
 */
public class ActionSupport extends com.opensymphony.xwork.ActionSupport implements Cancelable
{
    protected static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    protected static final String ERROR_MESSAGE = "javax.servlet.error.message";
    protected static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    protected static final String ERROR_EXCEPTION = "javax.servlet.error.exception";

    /**
     * Use our own text provider implementation. It would be nice if there was a way to replace
     * the textProvider used by the default action support.
     */
    private transient final TextProvider textProvider = new TextProviderSupport(getClass(), this);

    private String cancel;

    public boolean isCancelled()
    {
        return cancel != null;
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
        return AcegiUtils.getLoggedInUser();
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
}
