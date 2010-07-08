package com.zutubi.pulse.core.dependency.ivy;

import static com.zutubi.util.RandomUtils.randomString;
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
        if (cacheBase != null && configuration.getCacheBase() == null)
        {
            // Give each ivy client a unique cache base directory so that they
            // can be cleaned up without interferring with each other.
            File clientCacheBase = new File(cacheBase, randomString(5));
            configuration.setCacheBase(clientCacheBase);
        }
        return new IvyClient(configuration);
    }

    /**
     * Clean all of the local ivy caches.
     *
     * Note: Do not run this while ivy processing is active.  The caches may be in use.
     *
     * @throws IOException on error.
     * @throws ParseException on error.
     */
    public void cleanCaches() throws IOException, ParseException
    {
        IvyConfiguration configuration = new IvyConfiguration();
        configuration.setCacheBase(cacheBase);

        IvySettings settings = configuration.loadSettings();
        settings.getResolutionCacheManager().clean();

        RepositoryCacheManager[] caches = settings.getRepositoryCacheManagers();
        for (RepositoryCacheManager cache : caches)
        {
            cache.clean();
        }
    }
}
