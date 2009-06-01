package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.master.cleanup.config.CleanupWhat.*;
import static com.zutubi.pulse.master.cleanup.config.CleanupUnit.*;

import java.util.Arrays;

public class CleanupConfigurationFormatterTest extends PulseTestCase
{
    private CleanupConfigurationFormatter formatter;

    protected void setUp() throws Exception
    {
        super.setUp();

        formatter = new CleanupConfigurationFormatter();
    }

    public void testFormatAfterZeroBuilds()
    {
        assertEquals("never", formatter.getAfter(createConfig(0, BUILDS)));
    }

    public void testFormatAfterOneBuild()
    {
        assertEquals("1 build", formatter.getAfter(createConfig(1, BUILDS)));
    }

    public void testFormatAfterTwoBuilds()
    {
        assertEquals("2 builds", formatter.getAfter(createConfig(2, BUILDS)));
    }

    public void testFormatAfterZeroDays()
    {
        assertEquals("never", formatter.getAfter(createConfig(0, DAYS)));
    }

    public void testFormatAfterOneDay()
    {
        assertEquals("1 day", formatter.getAfter(createConfig(1, DAYS)));
    }

    public void testFormatAfterTwoDays()
    {
        assertEquals("2 days", formatter.getAfter(createConfig(2, DAYS)));
    }

    public void testFormatWhatAll()
    {
        assertEquals("whole builds", formatter.getWhat(createConfig()));
    }

    public void testFormatWhatWorkingDirectories()
    {
        assertEquals("working directories", formatter.getWhat(createConfig(WORKING_DIRECTORIES_ONLY)));
    }

    public void testFormatWhatAllOptions()
    {
        assertEquals("working directories, build artifacts,...", formatter.getWhat(createConfig(WORKING_DIRECTORIES_ONLY, BUILD_ARTIFACTS, REPOSITORY_ARTIFACTS)));
    }

    private CleanupConfiguration createConfig(CleanupWhat... whats)
    {
        CleanupConfiguration config = new CleanupConfiguration();
        config.setWhat(Arrays.asList(whats));
        config.setCleanupAll(whats.length == 0);
        return config;
    }

    private CleanupConfiguration createConfig(int retain, CleanupUnit unit)
    {
        CleanupConfiguration config = new CleanupConfiguration();
        config.setRetain(retain);
        config.setUnit(unit);
        return config;
    }
}
