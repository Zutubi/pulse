package com.zutubi.pulse.acceptance;

/**
 * A selenium browser factory provides the acceptance tests access
 * to selenium browser instances.  Each factory implementation is
 * responsible for the creating and cleaning up any browser instances
 * that it creates.
 */
public interface SeleniumBrowserFactory
{
    /**
     * Get a new selenium browser instance.
     *
     * @return a selenium browser instance
     */
    SeleniumBrowser newBrowser();

    /**
     * Cleanup any browser instances that have been returned by
     * calls to {@link #newBrowser()} since the last time cleanup
     * was called.
     *
     * Cleanup should be called whenever the client of this factory
     * no longer requires the browser instances.
     */
    void cleanup();

    /**
     * Stop should be called when the client of this factory no longer
     * needs it.  If any browsers have not been cleaned up, they will
     * also be cleaned up {@link #cleanup()}.
     */
    void stop();
}
