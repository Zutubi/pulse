package com.zutubi.pulse.xwork.interceptor;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.util.LocalizedTextUtil;
import com.zutubi.pulse.license.LicenseAnnotationAttributes;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;

/**
 * <class-comment/>
 */
public class LicensedInterceptor implements Interceptor
{
    private static final String DEFAULT_MESSAGE = "no.message.found";

    public void init()
    {

    }

    public void destroy()
    {

    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        Object target = invocation.getAction();

        // if the target is a WizardAction, then use the WizardClass instead.
/*
        if (target instanceof WizardAction)
        {
            target = ((WizardAction)target).getWizard();
        }
*/

        LicenseAnnotationAttributes attributes = new LicenseAnnotationAttributes();
        // check the class level attributes.
        Collection<String> instanceAttribs = attributes.getAttributes(target.getClass());

        // check the execution method level attributes.
        String methodName = invocation.getProxy().getMethod();
        Method method = null;
        try
        {
            method = target.getClass().getMethod(methodName);
        }
        catch (NoSuchMethodException nsme)
        {
            // look for methodName or doMethodName
            try
            {
                method = target.getClass().getMethod(doXxxMethodName(methodName));
            }
            catch (NoSuchMethodException nsme2)
            {
                // noop.
            }
        }

        if (method != null)
        {
            instanceAttribs.addAll(attributes.getAttributes(method));
        }
        // else there will be problems further down the road.

        // check that all of the requested license attributes validate.
        if (!checkAuthorised(instanceAttribs))
        {
            ((ValidationAware)target).addActionError(getTextMessage("not.licensed"));
            return "notlicensed";
        }

        return invocation.invoke();
    }

    private boolean checkAuthorised(Collection<String> attribs) throws LicenseException
    {
        for (String attrib : attribs)
        {
            if (!LicenseHolder.hasAuthorization(attrib))
            {
                return false;
            }
        }
        return true;
    }

    private String doXxxMethodName(String methodName)
    {
        return "do" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }

    private String getTextMessage(String messageKey)
    {
        return getTextMessage(messageKey, new Object[0]);
    }

    private String getTextMessage(String messageKey, Object[] args)
    {
        return getTextMessage(messageKey, args, ActionContext.getContext().getLocale());
    }

    private String getTextMessage(String messageKey, Object[] args, Locale locale)
    {
        if (args == null || args.length == 0)
        {
            return LocalizedTextUtil.findText(this.getClass(), messageKey, locale);
        }
        else
        {
            return LocalizedTextUtil.findText(this.getClass(), messageKey, locale, DEFAULT_MESSAGE, args);
        }
    }
}
