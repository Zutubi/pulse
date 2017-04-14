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

package com.zutubi.i18n.bundle;

import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class BaseResourceBundle extends BaseBundle
{
    private Map<String, String> messages;
    private Locale currentLocale;

    public Locale getLocale()
    {
        return currentLocale;
    }

    public BaseResourceBundle(Locale locale)
    {
        this.currentLocale = locale;
    }

    public BaseResourceBundle(InputStream stream, Locale locale) throws IOException
    {
        Properties properties = new Properties();
        properties.load(stream);
        messages = new HashMap(properties);
        this.currentLocale = locale;
    }

    protected Object handleGetObject(String key)
    {
        if (null == key)
        {
            throw new NullPointerException();
        }
        return messages.get(key);
    }

    public Enumeration<String> getKeys()
    {
        ResourceBundle parent = this.parent;
        // need to add parent keys
        return Collections.enumeration(messages.keySet());
    }
}