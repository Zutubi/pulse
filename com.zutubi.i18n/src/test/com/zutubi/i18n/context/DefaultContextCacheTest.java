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

import com.zutubi.i18n.bundle.BaseResourceBundle;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
public class DefaultContextCacheTest extends ZutubiTestCase
{
    private DefaultContextCache cache;
    private Context context;
    private Locale locale;
    private List<ResourceBundle> bundles;

    protected void setUp() throws Exception
    {
        super.setUp();

        cache = new DefaultContextCache();
        context = new ClassContext(DefaultContextCacheTest.class);
        locale = Locale.getDefault();
        bundles = Arrays.asList((ResourceBundle) new BaseResourceBundle(locale));
    }

    protected void tearDown() throws Exception
    {
        cache = null;
        context = null;

        super.tearDown();
    }

    public void testIsCached()
    {
        cache.addToCache(context, locale, bundles);
        assertTrue(cache.isCached(context, locale));
    }

    public void testNotCached()
    {
        assertTrue(!(cache.isCached(context, locale)));
    }

    public void testNotCachedAfterClear()
    {
        cache.addToCache(context, locale, bundles);
        cache.clear();
        assertTrue(!(cache.isCached(context, locale)));
    }

    public void testGetFromCache()
    {
        cache.addToCache(context, locale, this.bundles);
        List<ResourceBundle> bundles = cache.getFromCache(context, locale);
        assertEquals(1, bundles.size());
        assertEquals(this.bundles.get(0), bundles.get(0));
    }

    public void testGetFromCacheWithMiss()
    {
        assertTrue(cache.getFromCache(context, locale) == null);
    }

}
