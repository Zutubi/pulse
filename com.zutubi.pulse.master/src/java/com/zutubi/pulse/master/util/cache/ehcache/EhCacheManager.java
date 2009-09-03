package com.zutubi.pulse.master.util.cache.ehcache;

import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.io.IOUtils;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.hsqldb.lib.StringInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles creation and maintenance of database second-level caches.
 */
public class EhCacheManager implements com.zutubi.pulse.master.util.cache.CacheManager
{
    private static final String CONFIG_FILE = "/ehcache-template.xml";
    private static final String TEMP_DIR_TOKEN = "pulse.system.temp.dir";

    private CacheManager cacheManager;
    private SystemPaths systemPaths;

    public void init() throws IOException
    {
        String configContent;
        InputStream stream = getClass().getResourceAsStream(CONFIG_FILE);
        try
        {
            configContent = IOUtils.inputStreamToString(stream);
        }
        finally
        {
            IOUtils.close(stream);
            stream = null;
        }

        configContent = configContent.replace(TEMP_DIR_TOKEN, systemPaths.getTmpRoot().getAbsolutePath());
        try
        {
            stream = new StringInputStream(configContent);
            cacheManager = CacheManager.create(stream);
        }
        catch (CacheException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public synchronized EhCache getCache(String name)
    {
        if (!cacheManager.cacheExists(name))
        {
            try
            {
                cacheManager.addCache(name);
            }
            catch (CacheException e)
            {
                throw new RuntimeException(e);
            }
        }
        return new EhCache(cacheManager.getCache(name));
    }

    public void flushCaches()
    {
        for (int i = 0; i < cacheManager.getCacheNames().length; i++)
        {
            String cacheName = cacheManager.getCacheNames()[i];
            getCache(cacheName).removeAll();
        }
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
