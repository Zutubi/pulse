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

import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.context.ClassContext;
import com.zutubi.i18n.context.ClassContextResolver;
import com.zutubi.i18n.context.DefaultContextCache;
import com.zutubi.i18n.context.PackageContextResolver;
import com.zutubi.i18n.types.TestClass;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class DefaultMessageHandlerTest extends ZutubiTestCase
{
    private DefaultBundleManager manager;
    private DefaultMessageHandler handler;

    protected void setUp() throws Exception
    {
        super.setUp();

        manager = new DefaultBundleManager();
        manager.setContextCache(new DefaultContextCache());
        manager.addResolver(new PackageContextResolver());
        manager.addResolver(new ClassContextResolver());
        handler = new DefaultMessageHandler(manager);
    }

    protected void tearDown() throws Exception
    {
        manager = null;
        handler = null;
        super.tearDown();
    }

    // just make sure that everything is playing nicely.
    public void testKeyFormattedAsExpected()
    {
        ClassContext context = new ClassContext(TestClass.class);
        assertEquals("This ARG is formatted.", handler.format(context, "message.key", "ARG"));
        handler.setLocale(new Locale("de"));
        assertEquals("This ARG is formatted in german.", handler.format(context, "message.key", "ARG"));
    }
}
