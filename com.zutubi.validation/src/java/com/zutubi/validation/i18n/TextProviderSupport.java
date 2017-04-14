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

package com.zutubi.validation.i18n;

/**
 * <class-comment/>
 */
public abstract class TextProviderSupport implements TextProvider
{
    public String getText(String key)
    {
        return getText(key, new Object[0]);
    }

    public String getText(String key, Object... args)
    {
        if (args == null)
        {
            args = new Object[0];
        }
        return lookupText(key, args);
    }

    public TextProvider getTextProvider(Object context)
    {
        return this;
    }

    protected abstract String lookupText(String key, Object... args);
}
