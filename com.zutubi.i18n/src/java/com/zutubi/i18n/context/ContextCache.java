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

package com.zutubi.i18n.context;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A ContextCache is a cache that holds mappings between a context, locale
 * and the associated list of resource bundles. 
 */
public interface ContextCache
{
    /**
     * Add bundle and context to cache
     *
     * @param context    context with the scope of the bundle
     * @param locale     locale for the bundle
     * @param bundle    factory which creates bundles form a locale
     */
    public void addToCache(Context context, Locale locale, List<ResourceBundle> bundle);

    /**
     * Retrieve a cached bundle
     *
     * @param context context for the wanted bundle
     * @param locale  locale for the bundle
     *
     * @return cached bundle
     */
    public List<ResourceBundle> getFromCache(Context context, Locale locale);

    /**
     * Check if a context is cached
     *
     * @param context context to check if it is cached
     * @param locale locale for the bundle
     * @return true if the context is cached
     */
    public boolean isCached(Context context, Locale locale);

    /**
     * Clear the cache
     */
    public void clear();
}
