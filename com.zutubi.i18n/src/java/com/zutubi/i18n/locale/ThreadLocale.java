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

package com.zutubi.i18n.locale;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class ThreadLocale
{
    // true if thread local storage should be used
    private boolean thread = true;

    private Locale locale;

    // store a locale for each thread with
    // a thread local object. Thread locals store
    // a different value for each thread.
    private ThreadLocal<Locale> threadLocal = new ThreadLocal<Locale>()
    {
        protected Locale initialValue()
        {
            return Locale.getDefault();
        }
    };

    /**
     * Set if the locale should be global or thread local
     *
     * @param thread true if the locale is thread local
     */
    public void setThread(boolean thread)
    {
        this.thread = thread;
    }

    /**
     * Return the current locale, either the global
     * locale or the thread locale
     *
     * @return current locale
     */
    public Locale get()
    {
        if (thread)
        {
            return threadLocal.get();
        }
        else
        {
            return locale;
        }
    }

    /**
     * Set the locale which should be used, either
     * the globale locale or the thread locale
     *
     * @param locale locale to use
     */
    public void set(Locale locale)
    {
        if (thread)
        {
            threadLocal.set(locale);
        }
        else
        {
            this.locale = locale;
        }
    }
}