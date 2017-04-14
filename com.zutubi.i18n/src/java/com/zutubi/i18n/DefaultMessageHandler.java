/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.i18n;

import com.zutubi.i18n.bundle.BundleManager;
import com.zutubi.i18n.context.Context;
import com.zutubi.i18n.format.Formatter;
import com.zutubi.i18n.locale.LocaleManager;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The default implementation of the MessageHandler interface.
 *
 */
public class DefaultMessageHandler implements MessageHandler
{
    private Formatter formatter = new Formatter();

    protected BundleManager bundleManager;

    public DefaultMessageHandler(BundleManager bundleManager)
    {
        this.bundleManager = bundleManager;
    }

    public void setThreadLocale(Locale locale)
    {
        localeManager().setThreadLocale(locale);
    }

    public void setLocale(Locale locale)
    {
        localeManager().setLocale(locale);
    }

    public Locale getLocale()
    {
        return localeManager().getLocale();
    }

    public boolean isKeyDefined(Context context, String key)
    {
        for (ResourceBundle bundle : bundleManager.getBundles(context, getLocale()))
        {
            Enumeration<String> keys = bundle.getKeys();
            while(keys.hasMoreElements())
            {
                if(keys.nextElement().equals(key))
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    public String format(Context context, String key)
    {
        return format(context, getLocale(), key);
    }

    public String format(Context context, Locale locale, String key)
    {
        return internalFormat(context, locale, key);
    }

    public String format(Context context, String key, Object... args)
    {
        return internalFormat(context, getLocale(), key, args);
    }

    public String format(Context context, Locale locale, String key, Object... args)
    {
        return internalFormat(context, locale, key, args);
    }

    private String internalFormat(Context context, Locale locale, String key, Object... args)
    {
        for (ResourceBundle bundle : bundleManager.getBundles(context, locale))
        {
            String formattedText = formatter.format(bundle, key, args);
            if (formattedText != null)
            {
                return formattedText;
            }
        }
        return null;
    }

    public void clear()
    {
        bundleManager.clearContextCache();
    }

    private LocaleManager localeManager()
    {
        return LocaleManager.getManager();
    }
}
