package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.Message;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * The ivy implementation of the dependency manager interface.
 */
public class IvyManager
{
    private File cacheBase;

    static
    {
        // redirect the default logging to our own logging system.
        Message.setDefaultLogger(new IvyMessageLoggerAdapter());
    }

    public void init()
    {
    }

    public void setDataDir(File dataDir)
    {
        this.cacheBase = new File(dataDir, "cache");
    }

    public IvyClient createIvyClient(String repositoryBase) throws Exception
    {
        return createIvyClient(new IvyConfiguration(repositoryBase));
    }

    public IvyClient createIvyClient(IvyConfiguration configuration) throws Exception
    {
        applyCacheSettings(configuration);

        return new IvyClient(configuration);
    }

    /**
     * Clean all of the local ivy caches.
     *
     * Note: Do not run this while ivy processing is active.  The caches may be in use.
     *
     * @param configuration     the configuration defining the caches.
     *
     * @throws IOException on error.
     * @throws ParseException on error.
     */
    public void cleanCaches(IvyConfiguration configuration) throws IOException, ParseException
    {
        // implementation note:  the problem with caching is that I can not find a way to turn it
        // off completely.  this means potentially large artifacts are being cached on potentially
        // resource limited systems.  cleaning caches regularly helps by requires synchronisation
        // so that caches that are in use are not cleaned.
        
        applyCacheSettings(configuration);
        
        IvySettings settings = configuration.loadSettings();

        settings.getResolutionCacheManager().clean();
        
        RepositoryCacheManager[] caches = settings.getRepositoryCacheManagers();
        for (RepositoryCacheManager cache : caches)
        {
            cache.clean();
        }
    }

    private void applyCacheSettings(IvyConfiguration configuration) throws IOException
    {
        if (cacheBase != null)
        {
            configuration.setCacheBase(cacheBase);
        }
    }
}
