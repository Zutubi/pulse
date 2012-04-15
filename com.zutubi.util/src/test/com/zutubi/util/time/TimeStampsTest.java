package com.zutubi.util.time;

import com.zutubi.util.Constants;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 *
 *
 */
public class TimeStampsTest extends ZutubiTestCase
{
    public void testPrettyEstimated()
    {
        assertEquals("< 1 minute", TimeStamps.getPrettyEstimated(1));
        assertEquals("About 1 minute", TimeStamps.getPrettyEstimated(Constants.MINUTE));
        assertEquals("About 1 minute", TimeStamps.getPrettyEstimated(Constants.MINUTE * 1 + 1));
        assertEquals("About 2 minutes", TimeStamps.getPrettyEstimated(Constants.MINUTE * 2 + 1));
        assertEquals("About 54 minutes", TimeStamps.getPrettyEstimated(Constants.MINUTE * 54));
        assertEquals("< 1 hour", TimeStamps.getPrettyEstimated(Constants.MINUTE * 56));
    }
}
