package com.zutubi.pulse.acceptance;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The default selenium browser factory provides a new browser instance each
 * time a browser is requested via {@link #newBrowser()}.
 *
 * This factory will also track all browser instances that have been created
 * and ensure they are cleaned up on {@link #stop()} and {@link #cleanup()}
 */
public class DefaultSeleniumBrowserFactory implements SeleniumBrowserFactory
{
    private static final List<SeleniumBrowser> runningBrowsers = new LinkedList<SeleniumBrowser>();

    public synchronized SeleniumBrowser newBrowser()
    {
        SeleniumBrowser browser = new SeleniumBrowser();
        runningBrowsers.add(browser);
        browser.start();
        return browser;
    }

    public void cleanup()
    {
        Iterator<SeleniumBrowser> it = runningBrowsers.iterator();
        while (it.hasNext())
        {
            it.next().stop();
            it.remove();
        }
    }

    public void stop()
    {
        cleanup();
    }
}
