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

package com.zutubi.pulse.master.xwork;

import com.opensymphony.xwork.LocaleProvider;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This text provider extends the default text provider, and provides additional
 * functionality to help with I18N support in the WebUI.
 */
public class TextProviderSupport implements com.opensymphony.xwork.TextProvider
{
    private com.opensymphony.xwork.TextProviderSupport delegate;

    private static boolean showKeys = false;

    public static void setShowKeys(boolean b)
    {
        showKeys = b;
    }

    public static boolean isShowKeys()
    {
        return showKeys;
    }

    public TextProviderSupport(ResourceBundle bundle, LocaleProvider provider)
    {
        delegate = new com.opensymphony.xwork.TextProviderSupport(bundle, provider);
    }

    public TextProviderSupport(Class clazz, LocaleProvider provider)
    {
        delegate = new com.opensymphony.xwork.TextProviderSupport(clazz, provider);
    }

    public boolean hasKey(String key)
    {
        return delegate.hasKey(key);
    }

    public String getText(String key)
    {
        return getText(key, key);
    }

    public String getText(String key, List args)
    {
        return getText(key, key, args);
    }

    public String getText(String key, String[] args)
    {
        return getText(key, key, args);
    }

    public String getText(String key, String defaultValue)
    {
        return getText(key, defaultValue, (List) null);
    }

    public String getText(String key, String defaultValue, String obj)
    {
        return wrapText(key, delegate.getText(key, defaultValue, obj));
    }

    public String getText(String key, String defaultValue, List args)
    {
        return wrapText(key, delegate.getText(key, defaultValue, args));
    }

    public String getText(String key, String defaultValue, String[] args)
    {
        return wrapText(key, delegate.getText(key, defaultValue, args));
    }

    public String getText(String key, String defaultValue, List args, OgnlValueStack stack)
    {
        return wrapText(key, delegate.getText(key, defaultValue, args, stack));
    }

    public String getText(String key, String defaultValue, String[] args, OgnlValueStack stack)
    {
        return wrapText(key, delegate.getText(key, defaultValue, args, stack));
    }

    public ResourceBundle getTexts()
    {
        return new DebuggingResourceBundle(delegate.getTexts());
    }

    public ResourceBundle getTexts(String bundleName)
    {
        return new DebuggingResourceBundle(delegate.getTexts(bundleName));
    }

    private static String wrapText(String key, String str)
    {
        if (showKeys)
        {
            str = str + " [" + key + "]";
        }
        return str;
    }

    /**
     * Delegating resource bundle that allows us to wrap the bundle text.
     *
     */
    private class DebuggingResourceBundle extends ResourceBundle
    {
        private final ResourceBundle delegate;

        public DebuggingResourceBundle(ResourceBundle delegate)
        {
            this.delegate = delegate;
        }

        public Enumeration<String> getKeys()
        {
            return delegate.getKeys();
        }

        protected Object handleGetObject(String key)
        {
            // for some reason, we are not allowed to access the delegates handleGetObject??
            // obviously im missing something :|, anyway, call getString instead.
            return wrapText(key, delegate.getString(key));
        }
    }
}
