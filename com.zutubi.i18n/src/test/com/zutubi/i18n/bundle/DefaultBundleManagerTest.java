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
