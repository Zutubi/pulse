package com.zutubi.i18n.bundle;

import junit.framework.TestCase;
import com.zutubi.i18n.context.*;
import com.zutubi.i18n.mock.MockClass;
import com.zutubi.i18n.mock.MockSubClass;

import java.util.ResourceBundle;
import java.util.List;
import java.util.Locale;

/**
 * <class-comment/>
 */
public class DefaultBundleManagerTest extends TestCase
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
        resolver.addBundle(new IdContext("sample"), "com/zutubi/i18n/mock/message");
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

        List<ResourceBundle> bundles =  bundleManager.getBundles(new ClassContext(MockClass.class), locale);
        assertEquals(2, bundles.size());
    }

    public void testGetBundlesWithClassResolver()
    {
        bundleManager.addResolver(new ClassContextResolver());

        List<ResourceBundle> bundles =  bundleManager.getBundles(new ClassContext(MockSubClass.class), locale);
        assertEquals(2, bundles.size());
    }

    public void testLocaleSpecificBundlesAreLoaded()
    {
        IdContextResolver resolver = new IdContextResolver();
        resolver.addBundle(new IdContext("sample"), "com/zutubi/i18n/mock/german");
        bundleManager.addResolver(resolver);

        List<ResourceBundle> bundles =  bundleManager.getBundles(new IdContext("sample"), new Locale("de", "de", "ch"));
        assertEquals(1, bundles.size());

        bundles =  bundleManager.getBundles(new IdContext("sample"), new Locale("en"));
        assertEquals(0, bundles.size());
    }
}
