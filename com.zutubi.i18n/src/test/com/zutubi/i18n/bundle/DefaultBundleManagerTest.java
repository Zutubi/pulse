package com.zutubi.i18n.bundle;

import com.zutubi.i18n.context.*;
import com.zutubi.i18n.types.TestClass;
import com.zutubi.i18n.types.TestSubClass;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <class-comment/>
 */
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

    public void testGetBundlesWithIdResolver()
    {
        IdContextResolver resolver = new IdContextResolver();
        resolver.addBundle(new IdContext("sample"), "com/zutubi/i18n/types/message");
        bundleManager.addResolver(resolver);

        List<ResourceBundle> bundles =  bundleManager.getBundles(new IdContext("sample"), locale);
        assertEquals(1, bundles.size());
        ResourceBundle bundle = bundles.get(0);
        assertEquals("message.text", bundle.getString("message.key"));
        assertEquals("message.key", bundle.getKeys().nextElement());
    }

    public void testGetBundlesWithXWorkResolver()
    {
        bundleManager.addResolver(new XWorkContextResolver());

        List<ResourceBundle> bundles =  bundleManager.getBundles(new ExtendedClassContext(TestClass.class), locale);
        assertEquals(2, bundles.size());
    }

    public void testGetBundlesWithClassResolver()
    {
        bundleManager.addResolver(new ClassContextResolver());

        List<ResourceBundle> bundles =  bundleManager.getBundles(new ClassContext(TestSubClass.class), locale);
        assertEquals(2, bundles.size());
    }

    public void testLocaleSpecificBundlesAreLoaded()
    {
        IdContextResolver resolver = new IdContextResolver();
        resolver.addBundle(new IdContext("sample"), "com/zutubi/i18n/types/german");
        bundleManager.addResolver(resolver);

        List<ResourceBundle> bundles =  bundleManager.getBundles(new IdContext("sample"), new Locale("de", "de", "ch"));
        assertEquals(1, bundles.size());

        bundles =  bundleManager.getBundles(new IdContext("sample"), new Locale("en"));
        assertEquals(0, bundles.size());
    }
}
