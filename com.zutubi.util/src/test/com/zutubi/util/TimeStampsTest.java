package com.zutubi.util;

import junit.framework.TestCase;
import com.zutubi.util.Constants;
import com.zutubi.util.TimeStamps;

/**
 *
 *
 */
public class TimeStampsTest extends TestCase
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
