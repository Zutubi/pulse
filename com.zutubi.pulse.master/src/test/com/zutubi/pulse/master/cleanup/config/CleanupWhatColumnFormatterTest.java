package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class CleanupWhatColumnFormatterTest extends PulseTestCase
{
    private CleanupWhatColumnFormatter formatter;

    protected void setUp() throws Exception
    {
        super.setUp();

        formatter = new CleanupWhatColumnFormatter();
    }

    public void testFormatting()
    {
        assertEquals("build artifacts", formatter.format(CleanupWhat.BUILD_ARTIFACTS));
    }
}
