package com.zutubi.i18n;

import junit.framework.TestCase;
import com.zutubi.i18n.bundle.DefaultBundleManager;
import com.zutubi.i18n.context.PackageContextResolver;
import com.zutubi.i18n.context.ClassContextResolver;
import com.zutubi.i18n.context.ClassContext;
import com.zutubi.i18n.context.DefaultContextCache;
import com.zutubi.i18n.mock.MockClass;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class DefaultMessageHandlerTest extends TestCase
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
        ClassContext context = new ClassContext(MockClass.class);
        assertEquals("This ARG is formatted.", handler.format(context, "message.key", "ARG"));
        handler.setLocale(new Locale("de"));
        assertEquals("This ARG is formatted in german.", handler.format(context, "message.key", "ARG"));
    }
}
