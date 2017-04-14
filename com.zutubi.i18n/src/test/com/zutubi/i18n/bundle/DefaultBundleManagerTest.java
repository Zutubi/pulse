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

import com.zutubi.i18n.context.ClassContext;
import com.zutubi.i18n.context.ClassContextResolver;
import com.zutubi.i18n.context.DefaultContextCache;
import com.zutubi.i18n.types.TestSubClass;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DefaultBundleManagerTest extends ZutubiTestCase
{
    private DefaultBundleManager bundleManager;
    private Locale locale;

    protected void setUp() throws Exception
    {
        super.setUp();

        bundleManager = new DefaultBundleManager();
        bundleManager.setContextCache(new DefaultContextCache());
        locale = Locale.getDefault();
    }

    protected void tearDown() throws Exception
    {
        bundleManager = null;
        locale = null;

        super.tearDown();
    }

    public void testGetBundlesWithClassResolver()
    {
        bundleManager.addResolver(new ClassContextResolver());

        List<ResourceBundle> bundles = bundleManager.getBundles(new ClassContext(TestSubClass.class), locale);
        assertEquals(2, bundles.size());
    }
}
