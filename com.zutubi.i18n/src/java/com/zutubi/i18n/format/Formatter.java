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

package com.zutubi.i18n.format;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * The default formatter implementation.  This implementation delegates the
 * formatting to the MessageFormat object.
 * 
 */
public class Formatter
{
    public Formatter()
    {
    }

    public String format(ResourceBundle bundle, String key)
    {
        return formatArgs(bundle, key);
    }

    public String format(ResourceBundle bundle, String key, Object... args)
    {
        return formatArgs(bundle, key, args);
    }

    private String formatArgs(ResourceBundle bundle, String key, Object... args)
    {
        if (null != bundle)
        {
            try
            {
                return formatArgs(bundle.getString(key), args);
            }
            catch (MissingResourceException e)
            {
                // this key is not located in the specified bundle.
            }
        }
        return null;
    }

    private String formatArgs(String text, Object... args)
    {
        return MessageFormat.format(text, args);
    }

}