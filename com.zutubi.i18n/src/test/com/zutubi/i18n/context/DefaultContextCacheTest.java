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
